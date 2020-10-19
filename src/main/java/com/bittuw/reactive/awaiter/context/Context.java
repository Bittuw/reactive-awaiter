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


import com.bittuw.reactive.awaiter.sinks.CommonSink;
import lombok.Getter;
import org.reactivestreams.Subscription;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.lang.NonNull;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;


/**
 * Describe current depth in execution chain
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 04.03.2020
 */
public interface Context extends CoreSubscriber<CommonSink>, Subscription, Disposable {


    /**
     * Normal closing context
     */
    void close();


    /**
     * @return
     */
    int depth();


    /**
     * Generate context with `context` as parent
     *
     * @param context
     * @return
     */
    Context generateChildContext(@NonNull ContextOwner context);


    /**
     * @return
     */
    Scheduler getScheduler();


    /**
     * @return
     */
    Object hash();


    /**
     * @return
     */
    boolean isClosed();


    /**
     * @return
     */
    ContextOwner parent();


    /**
     *
     */
    class InvalidContext extends RuntimeException {


        /**
         *
         */
        @Getter
        private final Context newContext;


        /**
         *
         */
        @Getter
        private final Context oldContext;


        /**
         *
         */
        public InvalidContext(Context newContext, Context oldContext) {
            super(MessageFormatter
                    .format("Invalid context object:\ncurrent: {}\nnew {} ", oldContext, newContext)
                    .getMessage());
            this.newContext = newContext;
            this.oldContext = oldContext;
        }
    }
}
