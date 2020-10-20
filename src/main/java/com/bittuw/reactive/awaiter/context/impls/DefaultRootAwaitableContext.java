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

package com.bittuw.reactive.awaiter.context.impls;

import com.bittuw.reactive.awaiter.FallbackReactiveAwaiter;
import com.bittuw.reactive.awaiter.context.AbstractRootContext;
import com.bittuw.reactive.awaiter.context.ContextOwner;
import com.bittuw.reactive.awaiter.context.ContextProcessor;
import com.bittuw.reactive.awaiter.context.NestedAwaitableContext;
import com.bittuw.reactive.awaiter.sinks.CommonSink;
import com.bittuw.reactive.awaiter.sinks.NullSink;
import com.bittuw.reactive.awaiter.sinks.SinkFlux;
import com.bittuw.reactive.awaiter.sinks.SinkMono;
import com.bittuw.reactive.awaiter.support.addons.ReactiveUtils;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import com.bittuw.reactive.awaiter.support.lazy.SimpleReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;


/**
 * Provide some fallback features
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 30.04.2020
 */
@Slf4j
public final class DefaultRootAwaitableContext extends AbstractRootContext implements FallbackReactiveAwaiter {


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     */
    public DefaultRootAwaitableContext(@NonNull Object hash, @Nullable Scheduler scheduler,
                                       @Nullable String defaultKeyPrefix)
    {
        super(hash, scheduler, defaultKeyPrefix);
    }


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     * @return
     */
    public static DefaultRootAwaitableContext of(@NonNull Object hash,
                                                 @Nullable Scheduler scheduler,
                                                 @Nullable String defaultKeyPrefix)
    {
        return new DefaultRootAwaitableContext(hash, scheduler, defaultKeyPrefix);
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.fromCallable(() -> awaitContext(tMono, context)))
                .switchIfEmpty(Mono.fromCallable(() -> awaitFallbackContext(tMono, this)))
                .flatMap(Mono::from)
                .transformDeferred(ReactiveUtils.isolation(getScheduler()));
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.fromCallable(() -> awaitContext(tFlux, context)))
                .switchIfEmpty(Mono.fromCallable(() -> awaitFallbackContext(tFlux, this)))
                .flatMapMany(Flux::from)
                .transformDeferred(ReactiveUtils.isolation(getScheduler()));
    }


    /**
     * @param tMono
     * @param parentContext
     * @param <T>
     * @return
     */
    protected <T> Mono<T> awaitFallbackContext(@NonNull Mono<T> tMono,
                                               @NonNull DefaultRootAwaitableContext parentContext)
    {
        final var newContext = generateAwaitableContext(parentContext);
        if (log.isDebugEnabled())
            log.debug("Generate new context:\n New context: {}\n Parent context: {}", newContext.hash(),
                    parentContext.hash());
        return Mono.<T>create(sink -> specPush(SinkMono.of(sink, tMono, newContext, getScheduler())))
                .transform(ReactiveUtils.setToContextMono(getChainHashKeyName(), newContext));
    }


    /**
     * @param tFlux
     * @param parentContext
     * @param <T>
     * @return
     */
    protected <T> Flux<T> awaitFallbackContext(@NonNull Flux<T> tFlux,
                                               @NonNull DefaultRootAwaitableContext parentContext)
    {
        final var newContext = generateAwaitableContext(parentContext);
        if (log.isDebugEnabled())
            log.debug("Generate new context:\n New context: {}\n Parent context: {}", newContext.hash(),
                    parentContext.hash());
        return Flux.<T>create(sink -> specPush(SinkFlux.of(sink, tFlux, newContext, getScheduler())))
                .transform(ReactiveUtils.setToContextFlux(getChainHashKeyName(), newContext));
    }


    /**
     * @param tMono
     * @param fallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> awaitOrFallback(@NonNull Mono<T> tMono, @NonNull Mono<T> fallback) {
        return await(tMono).onErrorResume(Fallback.class, throwable -> fallback);
    }


    /**
     * @param tFlux
     * @param fallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> awaitOrFallback(@NonNull Flux<T> tFlux, @NonNull Flux<T> fallback) {
        return await(tFlux).onErrorResume(Fallback.class, throwable -> fallback);
    }


    /**
     * @param tMono
     * @param waitFallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> awaitOrWait(@NonNull Mono<T> tMono, @NonNull Mono<T> waitFallback) {
        return this.await(tMono).onErrorResume(Fallback.class,
                fallback -> this.generateWaiter(fallback.nestedAwaitableContext, waitFallback));
    }


    /**
     * @param tFlux
     * @param waitFallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> awaitOrWait(@NonNull Flux<T> tFlux, @NonNull Flux<T> waitFallback) {
        return this.await(tFlux).onErrorResume(Fallback.class,
                fallback -> this.generateWaiter(fallback.nestedAwaitableContext, waitFallback));
    }


    /**
     * @return
     */
    protected DefaultNestedAwaitableContext generateAwaitableContext(
            @NonNull
                    ContextOwner context)
    {
        return DefaultNestedAwaitableContext
                .of(Objects.isNull(getDefaultKeyPrefix()) ? StringGenerator.DEFAULT_32.generate() :
                                getDefaultKeyPrefix() + "_" + StringGenerator.DEFAULT_16.generate(), context, getScheduler(),
                        getDefaultKeyPrefix(), getChainHashKeyName());
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    protected <T> Flux<T> generateWaiter(@NonNull NestedAwaitableContext nestedAwaitableContext,
                                         @NonNull Flux<T> tFlux)
    {
        final var awaitableContext = generateAwaitableContext(this);
        return Flux.<T>create(sink -> {
            final CommonSink commonSink = SinkFlux.of(sink, tFlux, awaitableContext, getScheduler());
            if (nestedAwaitableContext.add(commonSink))
                commonSink.execute().response();
        });
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    protected <T> Mono<T> generateWaiter(@NonNull NestedAwaitableContext nestedAwaitableContext,
                                         @NonNull Mono<T> tMono)
    {
        final var awaitableContext = generateAwaitableContext(this);
        return Mono.<T>create(sink -> {
            final CommonSink commonSink = SinkMono.of(sink, tMono, awaitableContext, getScheduler());
            if (nestedAwaitableContext.add(commonSink))
                commonSink.execute().response();
        });
    }


    /**
     * @param value
     */
    @Override
    protected void hookOnNext(@NonNull CommonSink value) {
        if (log.isTraceEnabled())
            log.debug("Start executing sink inner context {} inside other {}", value.getContext().hash(),
                    this.hash());
        getCurrentCommonSink().getMaintainedValue()
                .filter(value::equals)
                .ifPresentOrElse(prevCommonSink -> value.execute().response(),
                        () -> {throw new RuntimeException("Some other context use this awaiter!");});

    }


    /**
     * @param commonSink
     */
    public void specPush(@NonNull CommonSink commonSink) {
        final var reference = new SimpleReference<NestedAwaitableContext>();
        if (getCurrentCommonSink().replaceIfExpected(commonSink, prev -> {
            if (!(prev instanceof NullSink) && !prev.getContext().isClosed()) {
                reference.setValue((NestedAwaitableContext) prev.getContext());
                return false;
            }
            return true;
        }))
            super.push(commonSink);
        else
            commonSink.getSinkAdapter().error(new Fallback(reference.getValue()));
    }


    /**
     *
     */
    public static class Fallback extends RuntimeException {


        /**
         *
         */
        final NestedAwaitableContext nestedAwaitableContext;


        /**
         * @param nestedAwaitableContext
         */
        public Fallback(NestedAwaitableContext nestedAwaitableContext) {
            this.nestedAwaitableContext = nestedAwaitableContext;
        }
    }
}
