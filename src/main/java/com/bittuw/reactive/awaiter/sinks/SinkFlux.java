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

package com.bittuw.reactive.awaiter.sinks;

import com.bittuw.reactive.awaiter.context.Context;
import com.bittuw.reactive.awaiter.support.Response;
import com.bittuw.reactive.awaiter.support.SinkAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.util.function.LongConsumer;


/**
 * @param <T>
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
@Slf4j
public class SinkFlux<T> extends AwaiterSubscriber<T> implements CommonSink {


    /**
     *
     */
    private final Flux<T> publisher;


    /**
     *
     */
    private final FluxSink<T> sink;


    /**
     * @param sink
     * @param publisher
     * @param context
     * @param scheduler
     */
    protected SinkFlux(@NonNull FluxSink<T> sink, @NonNull Flux<T> publisher, @NonNull Context context,
                       @Nullable Scheduler scheduler)
    {
        super(context, scheduler, new SinkFluxAdapter<>(sink));
        this.sink = sink;
        this.publisher = publisher;
    }


    /**
     * @param sink
     * @param publisher
     * @param context
     * @param scheduler
     * @param <T>
     * @return
     */
    public static <T> SinkFlux<T> of(@NonNull FluxSink<T> sink, @NonNull Flux<T> publisher, @NonNull Context context,
                                     @Nullable Scheduler scheduler)
    {
        return new SinkFlux<>(sink, publisher, context, scheduler);
    }


    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public reactor.util.context.Context currentContext() {
        return sink.currentContext();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response execute() {
        return () -> this.publisher.subscriberContext(currentContext()).subscribe(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext() {
        return context;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SinkAdapter getSinkAdapter() {
        return sinkAdapter;
    }


    /**
     *
     */
    @Override
    protected void hookOnCancel() {
        log.debug("Flux sink cancel signal into {}", getContext().hash());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnComplete() {
        log.debug("Complete signal into {}", getContext().hash());
        sink.complete();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(@NonNull Throwable throwable) {
        sink.error(throwable);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnNext(@NonNull T value) {
        log.debug("Propagate {} into {}", value.toString(), getContext().hash());
        sink.next(value);
    }


    /**
     * @param <T>
     */
    @RequiredArgsConstructor
    public static class SinkFluxAdapter<T> implements SinkAdapter {


        /**
         *
         */
        private final FluxSink<T> sink;


        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            sink.complete();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable throwable) {
            sink.error(throwable);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void onCancel(@NonNull Disposable disposable) {
            sink.onCancel(disposable);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void onDispose(@NonNull Disposable disposable) {
            sink.onDispose(disposable);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequest(@NonNull LongConsumer consumer) {
            sink.onRequest(consumer);
        }
    }
}
