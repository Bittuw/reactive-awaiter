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
import org.springframework.lang.NonNull;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.function.LongConsumer;


/**
 * @param <T>
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
public class SinkMono<T> extends BaseSubscriber<T> implements CommonSink {


    /**
     *
     */
    private final Context context;


    /**
     *
     */
    private final Mono<T> publisher;


    /**
     *
     */
    private final MonoSink<T> sink;


    /**
     *
     */
    private final SinkAdapter sinkAdapter;


    /**
     * @param sink
     * @param publisher
     * @param context
     */
    protected SinkMono(@NonNull MonoSink<T> sink, @NonNull Mono<T> publisher, @NonNull Context context)
    {
        this.sink = sink;
        this.publisher = publisher;
        this.context = context;
        this.sinkAdapter = new SinkMonoAdapter<>(sink);
        this.sinkAdapter.onCancel(() -> {
            context.parent().cancel(context);
            this.cancel();
        });
        this.sinkAdapter.onDispose(() -> context.parent().close(context));
    }


    /**
     * @param sink
     * @param publisher
     * @param request
     * @param context
     * @param <T>
     * @return
     */
    public static <T> SinkMono<T> of(@NonNull MonoSink<T> sink, @NonNull Mono<T> publisher, @NonNull Context context)
    {
        return new SinkMono<>(sink, publisher, context);
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
     * {@inheritDoc}
     */
    @Override
    protected void hookOnComplete() {
        sink.success();
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
        sink.success(value);
    }


    /**
     *
     */
    @RequiredArgsConstructor
    public static class SinkMonoAdapter<T> implements SinkAdapter {


        /**
         *
         */
        private final MonoSink<T> sink;


        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            sink.success();
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
