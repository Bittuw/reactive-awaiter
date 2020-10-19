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

import com.bittuw.reactive.awaiter.SemaphoreReactiveAwaiter;
import com.bittuw.reactive.awaiter.context.AbstractRootContext;
import com.bittuw.reactive.awaiter.context.Context;
import com.bittuw.reactive.awaiter.context.ContextProcessor;
import com.bittuw.reactive.awaiter.sinks.CommonSink;
import com.bittuw.reactive.awaiter.support.addons.ReactiveUtils;
import com.bittuw.reactive.awaiter.support.atomic.GenericMaintainer;
import com.bittuw.reactive.awaiter.support.atomic.IntegerMaintainer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.text.MessageFormat;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
@Slf4j
public final class DefaultSemaphoreContext extends AbstractRootContext implements SemaphoreReactiveAwaiter {


    /**
     *
     */
    private final GenericMaintainer<?, Integer> rate = IntegerMaintainer.create(0);


    /**
     *
     */
    private final int threshold;


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     * @param threshold
     */
    public DefaultSemaphoreContext(@NonNull Object hash, @Nullable Scheduler scheduler, @Nullable String defaultKeyPrefix,
                                   int threshold)
    {
        super(hash, scheduler, defaultKeyPrefix);
        if (threshold < 0)
            throw new RuntimeException("Threshold cannot be less then zero");
        this.threshold = threshold;
    }


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     * @param threshold
     * @return
     */
    public static DefaultSemaphoreContext of(@NonNull Object hash, @Nullable Scheduler scheduler,
                                             @Nullable String defaultKeyPrefix, int threshold)
    {
        return new DefaultSemaphoreContext(hash, scheduler, defaultKeyPrefix, threshold);
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.just(context)
                        .flatMap(ignored -> Mono.fromCallable(() -> Mono.<T>error(new NestedContextDenied()))))
                .switchIfEmpty(Mono.fromCallable(() -> awaitContext(tMono, this)))
                .flatMap(Mono::from)
                .subscribeOn(getScheduler())
                .doFirst(() -> rate.replace(integer -> integer + 1));
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return ReactiveUtils.getFromContext(getChainHashKeyName(), ContextProcessor.class)
                .flatMap(context -> Mono.just(context)
                        .flatMap(ignored -> Mono.fromCallable(() -> Flux.<T>error(new NestedContextDenied()))))
                .switchIfEmpty(Mono.fromCallable(() -> awaitContext(tFlux, this)))
                .flatMapMany(Flux::from)
                .subscribeOn(getScheduler())
                .doFirst(() -> rate.replace(integer -> integer + 1));
    }


    /**
     * @param context
     */
    @Override
    public void close(@NonNull Context context) {
        super.close(context);
        rate.replace(prev -> prev - 1);
    }


    /**
     * @param value
     */
    @Override
    protected void hookOnNext(@NonNull CommonSink value) {
        if (log.isTraceEnabled())
            log.debug("Start executing sink inner context {} inside other {}", value.getContext().hash(),
                    this.hash());
        value.execute().response();
    }


    /**
     * @param subscription
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(threshold);
    }


    /**
     *
     */
    public static class InvalidRateValue extends RuntimeException {


        /**
         * @param expected
         * @param actual
         */
        public InvalidRateValue(@NonNull Integer expected, @NonNull Integer actual) {
            super(MessageFormat.format("Invalid rate value: expected {}, actual {}", expected, actual));
        }
    }


    /**
     *
     */
    public static class NestedContextDenied extends RuntimeException {}
}
