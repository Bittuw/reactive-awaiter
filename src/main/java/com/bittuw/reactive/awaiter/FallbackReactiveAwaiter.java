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

package com.bittuw.reactive.awaiter;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 07.05.2020
 */
public interface FallbackReactiveAwaiter extends ReactiveAwaiter {


    /**
     * @param tMono
     * @param fallback
     * @param <T>
     * @return
     */
    <T> Mono<T> awaitOrFallback(@NonNull Mono<T> tMono, @NonNull Mono<T> fallback);


    /**
     * @param tFlux
     * @param fallback
     * @param <T>
     * @return
     */
    <T> Flux<T> awaitOrFallback(@NonNull Flux<T> tFlux, @NonNull Flux<T> fallback);


    /**
     * Execute waitFallback only after root context will be closed (Queue empty signal)
     * This is very similar with {@link ReactiveAwaiter#await(Mono)} but waitFallback
     * execution is not synchronized with other tasks.
     *
     * @param tMono
     * @param <T>
     * @return
     */
    <T> Mono<T> awaitOrWait(@NonNull Mono<T> tMono, @NonNull Mono<T> waitFallback);


    /**
     * Similar as {@link #awaitOrWait(Mono, Mono)} only for {@link Flux}
     *
     * @param tFlux
     * @param waitFallback
     * @param <T>
     * @return
     */
    <T> Flux<T> awaitOrWait(@NonNull Flux<T> tFlux, @NonNull Flux<T> waitFallback);
}
