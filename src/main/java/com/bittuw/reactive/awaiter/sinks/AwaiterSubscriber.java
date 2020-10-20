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
import com.bittuw.reactive.awaiter.support.SinkAdapter;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 20.10.2020
 */
public abstract class AwaiterSubscriber<T> extends BaseSubscriber<T> {


    /**
     *
     */
    @SuppressWarnings("rawtypes")
    private static final AtomicLongFieldUpdater<AwaiterSubscriber> MISSING_INITIAL_REQUEST =
            AtomicLongFieldUpdater.newUpdater(AwaiterSubscriber.class, "missingInitialRequest");


    /**
     *
     */
    Context context;


    /**
     *
     */
    Scheduler scheduler;


    /**
     *
     */
    SinkAdapter sinkAdapter;


    /**
     *
     */
    private volatile long missingInitialRequest = 0L;


    /**
     * @param context
     * @param scheduler
     * @param sinkAdapter
     */
    public AwaiterSubscriber(Context context, Scheduler scheduler,
                             SinkAdapter sinkAdapter)
    {
        this.context = context;
        this.scheduler = scheduler;
        this.sinkAdapter = sinkAdapter;
        this.sinkAdapter.onRequest(this::handleRequest);
        this.sinkAdapter.onCancel(() -> {
            context.parent().cancel(context);
            this.cancel();
        });
        this.sinkAdapter.onDispose(() -> context.parent().close(context));
    }


    /**
     * @param value
     */
    private void handleRequest(long value) {
        MISSING_INITIAL_REQUEST.getAndUpdate(this, prev -> {
            if (prev != -1) return Operators.addCap(prev, value);
            else request(value); return prev;
        });
    }


    /**
     * @param subscription
     */
    @Override
    protected void hookOnSubscribe(@NonNull Subscription subscription) {
        final var missing = MISSING_INITIAL_REQUEST.getAndUpdate(this, prev -> -1L);
        if (missing != 0) request(missing);
    }
}
