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


import com.bittuw.reactive.awaiter.FallbackReactiveAwaiter;
import com.bittuw.reactive.awaiter.context.impls.DefaultRootAwaitableContext;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;


/**
 * Provide unique access to only one root context, for other ones {@link DefaultRootAwaitableContext.Fallback} will be raised
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 27.04.2020
 */
@Slf4j
public class DefaultFallbackReactiveAwaiter implements FallbackReactiveAwaiter {


    /**
     *
     */
    protected final DefaultRootAwaitableContext awaitableContext;


    /**
     *
     */
    @Getter
    protected Scheduler scheduler;


    /**
     *
     */
    public DefaultFallbackReactiveAwaiter() {
        this(null, null);
    }


    /**
     * @param defaultPrefix
     */
    public DefaultFallbackReactiveAwaiter(@Nullable String defaultPrefix) {
        this(defaultPrefix, null);
    }


    /**
     * @param scheduler
     */
    public DefaultFallbackReactiveAwaiter(@Nullable Scheduler scheduler) {
        this(null, scheduler);
    }


    /**
     * @param defaultPrefix
     * @param scheduler
     */
    public DefaultFallbackReactiveAwaiter(@Nullable String defaultPrefix, @Nullable Scheduler scheduler)
    {
        this.awaitableContext = DefaultRootAwaitableContext
                .of(StringGenerator.DEFAULT_32.generate(), scheduler, defaultPrefix);
        this.scheduler = scheduler;
    }


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> await(@NonNull Mono<T> tMono) {
        return awaitableContext.await(tMono);
    }


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> await(@NonNull Flux<T> tFlux) {
        return awaitableContext.await(tFlux);
    }


    /**
     * @param tMono
     * @param fallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> awaitOrFallback(@NonNull Mono<T> tMono, @NonNull Mono<T> fallback) {
        return awaitableContext.awaitOrFallback(tMono, fallback);
    }


    /**
     * @param tFlux
     * @param fallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> awaitOrFallback(@NonNull Flux<T> tFlux, @NonNull Flux<T> fallback) {
        return awaitableContext.awaitOrFallback(tFlux, fallback);
    }


    /**
     * @param tMono
     * @param waitFallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Mono<T> awaitOrWait(@NonNull Mono<T> tMono, @NonNull Mono<T> waitFallback) {
        return awaitableContext.awaitOrWait(tMono, waitFallback);
    }


    /**
     * @param tFlux
     * @param waitFallback
     * @param <T>
     * @return
     */
    @Override
    public <T> Flux<T> awaitOrWait(@NonNull Flux<T> tFlux, @NonNull Flux<T> waitFallback) {
        return awaitableContext.awaitOrWait(tFlux, waitFallback);
    }
}
