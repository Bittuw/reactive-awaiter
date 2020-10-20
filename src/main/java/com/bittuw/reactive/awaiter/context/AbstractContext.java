/*
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.bittuw.reactive.awaiter.context;

import com.bittuw.reactive.awaiter.ReactiveAwaiter;
import com.bittuw.reactive.awaiter.sinks.CommonSink;
import com.bittuw.reactive.awaiter.sinks.NullSink;
import com.bittuw.reactive.awaiter.sinks.SinkFlux;
import com.bittuw.reactive.awaiter.sinks.SinkMono;
import com.bittuw.reactive.awaiter.support.addons.ReactiveUtils;
import com.bittuw.reactive.awaiter.support.atomic.CommonSinkMaintainer;
import com.bittuw.reactive.awaiter.support.atomic.GenericMaintainer;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import com.bittuw.reactive.awaiter.support.lazy.SimpleReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 04.03.2020
 */
@Slf4j
public abstract class AbstractContext extends BaseSubscriber<CommonSink>
        implements ContextOwner, ContextProcessor, ReactiveAwaiter
{


    /**
     *
     */
    final static Integer REQUEST_ONE = 1;


    /**
     *
     */
    protected final Object hash;


    /**
     *
     */
    protected final ContextOwner parent;


    /**
     *
     */
    @Nullable
    protected final Scheduler scheduler;


    /**
     *
     */
    @Getter
    private final String chainHashKeyName;


    /**
     *
     */
    @Getter
    private final GenericMaintainer<?, CommonSink> currentCommonSink =
            CommonSinkMaintainer.create(CommonSink.NULL_SINK);


    /**
     *
     */
    @Getter
    @Nullable
    private final String defaultKeyPrefix;


    /**
     *
     */
    private final int depth;


    /**
     *
     */
    @Getter(lazy = true)
    private final FluxSink<CommonSink> fluxSink = init();


    /**
     *
     */
    protected volatile boolean isClosed = false;


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     */
    protected AbstractContext(@NonNull Object hash, @Nullable Scheduler scheduler, @Nullable String defaultKeyPrefix) {
        this.hash = hash;
        this.depth = 1;
        this.parent = this;
        this.scheduler = scheduler;
        this.chainHashKeyName = StringGenerator.DEFAULT_16.generate();
        this.defaultKeyPrefix = defaultKeyPrefix;
    }


    /**
     * @param hash
     * @param parent
     * @param scheduler
     * @param defaultKeyPrefix
     */
    protected AbstractContext(@NonNull Object hash, @NonNull ContextOwner parent, @Nullable Scheduler scheduler,
                              @Nullable String defaultKeyPrefix, @NonNull String chainHashKeyName)
    {
        this.hash = hash;
        this.depth = parent.depth() + 1;
        this.parent = parent;
        this.scheduler = scheduler;
        this.chainHashKeyName = chainHashKeyName;
        this.defaultKeyPrefix = defaultKeyPrefix;
    }


    /**
     * @param tMono
     * @param <T>
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.fromCallable(() -> awaitContext(tMono, context)))
                .switchIfEmpty(Mono.fromCallable(() -> awaitContext(tMono, this)))
                .flatMap(Mono::from)
                .transformDeferred(ReactiveUtils.isolation(getScheduler()));
    }


    /**
     * @param tFlux
     * @param <T>
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.fromCallable(() -> awaitContext(tFlux, context)))
                .switchIfEmpty(Mono.fromCallable(() -> awaitContext(tFlux, this))).flatMapMany(Flux::from)
                .transformDeferred(ReactiveUtils.isolation(getScheduler()));
    }


    /**
     * @param tMono
     * @param parentContext
     * @param <T>
     * @return
     */
    protected <T> Mono<T> awaitContext(@NonNull Mono<T> tMono, @NonNull ContextProcessor parentContext)
    {
        final var newContext = generateChildContext(parentContext);
        if (log.isTraceEnabled())
            log.debug("Generate new context: NEW CONTEXT {}, PARENT CONTEXT {}", newContext.hash(),
                    parentContext.hash());
        return Mono.<T>create(
                sink -> parentContext.push(SinkMono.of(sink, tMono, newContext, getScheduler())))
                .transform(ReactiveUtils.setToContextMono(getChainHashKeyName(), newContext));
    }


    /**
     * @param tFlux
     * @param parentContext
     * @param <T>
     * @return
     */
    protected <T> Flux<T> awaitContext(@NonNull Flux<T> tFlux, @NonNull ContextProcessor parentContext)
    {
        final var newContext = generateChildContext(parentContext);
        if (log.isTraceEnabled())
            log.debug("Generate new context: NEW CONTEXT {}, PARENT CONTEXT {}", newContext.hash(),
                    parentContext.hash());
        return Flux.<T>create(sink -> parentContext.push(SinkFlux.of(sink, tFlux, newContext, getScheduler())))
                .transform(ReactiveUtils.setToContextFlux(getChainHashKeyName(), newContext));
    }


    /**
     * @param context
     */
    @Override
    public void cancel(@NonNull Context context) {
        log.debug("Canceling {} from {}", context.hash(), this.hash());
        context.cancel();
    }


    /**
     * @param context
     */
    @Override
    public void close(@NonNull Context context) {
        log.debug("Closing {} from {}", context.hash(), this.hash());
        context.close();
        upstream().request(REQUEST_ONE);
    }


    /**
     *
     */
    @Override
    public void close() {
        log.debug("Normal closing {}", hash());
        isClosed = true;
        getFluxSink().complete();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int depth() {
        return depth;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractContext)) return false;
        AbstractContext that = (AbstractContext) o;
        return hash.equals(that.hash);
    }


    /**
     * @return
     */
    @Override
    @Nullable
    public Scheduler getScheduler() {
        return scheduler;
    }


    /**
     * @return
     */
    @Override
    public Object hash() {
        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }


    /**
     * @param type
     */
    @Override
    protected void hookFinally(@NonNull SignalType type) {
        if (log.isTraceEnabled())
            log.debug("Clean up {} into {}", this.hash(), parent.hash());
    }


    /**
     *
     */
    @Override
    protected void hookOnCancel() {
        if (log.isTraceEnabled())
            log.debug("Canceling {} into {}", this.hash(), parent.hash());
    }


    /**
     * @param throwable
     */
    @Override
    protected void hookOnError(@NonNull Throwable throwable) {
        if (log.isTraceEnabled())
            log.error("Catch exception into {}", this.hash(), throwable);
        if (this.depth() > 1)
            parent().propagate(this, throwable);
    }


    /**
     * @param value
     */
    @Override
    protected void hookOnNext(@NonNull CommonSink value) {
        if (log.isTraceEnabled())
            log.debug("Start executing sink inner context {} inside other {}", value.getContext().hash(), this.hash());
        getCurrentCommonSink().getMaintainedValue()
                .filter(prev -> getCurrentCommonSink()
                        .replaceIfExpected(value, prev1 -> prev1 instanceof NullSink || prev1.getContext().isClosed()))
                .ifPresentOrElse(prevCommonSink -> value.execute().response(),
                        () -> getFluxSink().error(new InvalidContext(value.getContext(),
                                getCurrentCommonSink().getMaintainedValue().get().getContext())));

    }


    /**
     * @param subscription
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(REQUEST_ONE);
    }


    /**
     * @return
     */
    protected FluxSink<CommonSink> init() {
        final SimpleReference<FluxSink<CommonSink>> ref = new SimpleReference<>();
        Flux.create(ref::setValue).subscribe(this);
        return ref.getValue();
    }


    /**
     * @return
     */
    @Override
    public boolean isClosed() {
        return isClosed;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ContextOwner parent() {
        return parent;
    }


    /**
     * @param throwable
     */
    @Override
    public void propagate(@NonNull Context context, @NonNull Throwable throwable) {
        log.error("Propagate throwable from {}", context.hash(), throwable);
        getFluxSink().error(throwable);
    }


    /**
     * @param commonSink
     */
    @Override
    public void push(@NonNull CommonSink commonSink) {
        if (log.isTraceEnabled())
            log.debug("Pushing a new sink with {} into {}", commonSink.getContext().hash(), this.hash());
        getFluxSink().next(commonSink);
    }
}
