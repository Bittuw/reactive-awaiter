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

package com.bittuw.reactive.awaiter.impls;

import com.bittuw.reactive.awaiter.SemaphoreReactiveAwaiter;
import com.bittuw.reactive.awaiter.context.impls.DefaultSemaphoreContext;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;


/**
 * Allow to execute multiple context simultaneously (But without nested context execution)
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 07.05.2020
 */
public class DefaultSemaphoreReactiveAwaiter implements SemaphoreReactiveAwaiter {


    /**
     *
     */
    protected final DefaultSemaphoreContext semaphoreContext;


    /**
     *
     */
    @Getter
    protected Scheduler scheduler;


    /**
     * @param threshold
     */
    public DefaultSemaphoreReactiveAwaiter(int threshold) {
        this(null, null, threshold);
    }


    /**
     * @param defaultPrefix
     * @param threshold
     */
    public DefaultSemaphoreReactiveAwaiter(@Nullable String defaultPrefix, int threshold) {
        this(defaultPrefix, null, threshold);
    }


    /**
     * @param scheduler
     * @param threshold
     */
    public DefaultSemaphoreReactiveAwaiter(@Nullable Scheduler scheduler, int threshold) {
        this(null, scheduler, threshold);
    }


    /**
     * @param defaultPrefix
     * @param scheduler
     * @param threshold
     */
    public DefaultSemaphoreReactiveAwaiter(@Nullable String defaultPrefix,
                                           @Nullable Scheduler scheduler, int threshold)
    {
        this.semaphoreContext =
                DefaultSemaphoreContext.of(StringGenerator.DEFAULT_32.generate(), scheduler, defaultPrefix, threshold);
        this.scheduler = scheduler;
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return semaphoreContext.await(tMono);
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return semaphoreContext.await(tFlux);
    }
}
