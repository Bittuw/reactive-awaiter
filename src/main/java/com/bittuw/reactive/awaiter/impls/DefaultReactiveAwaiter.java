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

import com.bittuw.reactive.awaiter.ReactiveAwaiter;
import com.bittuw.reactive.awaiter.context.impls.DefaultRootContext;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;


/**
 * Simple implementation of {@link ReactiveAwaiter}. Sync by {@link reactor.util.context.Context} in reactive context.
 * Allow to use {@link DefaultReactiveAwaiter#await(Flux)} and {@link DefaultReactiveAwaiter#await(Mono)}
 * in what ever context you want. Its means that only one enclosed {@link org.reactivestreams.Publisher}
 * will be executed concurrently. Does not provide of synchronization all internal reactive operators!.
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
@Slf4j
public class DefaultReactiveAwaiter implements ReactiveAwaiter {


    /**
     *
     */
    protected final DefaultRootContext rootContext;


    /**
     *
     */
    @Getter
    protected Scheduler scheduler;


    /**
     *
     */
    public DefaultReactiveAwaiter() {
        this(null, null);
    }


    /**
     * @param defaultPrefix
     */
    public DefaultReactiveAwaiter(@Nullable String defaultPrefix) {
        this(defaultPrefix, null);
    }


    /**
     * @param scheduler
     */
    public DefaultReactiveAwaiter(@Nullable Scheduler scheduler) {
        this(null, scheduler);
    }


    /**
     * @param defaultPrefix
     * @param scheduler
     */
    public DefaultReactiveAwaiter(@Nullable String defaultPrefix, @Nullable Scheduler scheduler)
    {
        this.rootContext = DefaultRootContext.of(StringGenerator.DEFAULT_32.generate()/*, scheduler, defaultPrefix*/);
        this.scheduler = scheduler;
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return rootContext.await(tMono);
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return rootContext.await(tFlux);
    }
}
