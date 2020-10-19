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

import com.bittuw.reactive.awaiter.context.ContextProcessor;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Context-execution-safety.
 * Context-cancel-safety.
 * Guarantees that only one wrapped publisher will executed at time.
 * Support nested calls chain by using {@link reactor.util.context.Context}
 * to propagate special {@link ContextProcessor}
 * instance with internal {@link reactor.core.publisher.FluxProcessor}.
 * Without deadlocks.
 * Similar to recursive mutex or semaphore with single value.
 *
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
public interface ReactiveAwaiter {


    /**
     * @param tMono
     * @param <T>
     * @return
     */
    <T> Mono<T> await(@NonNull Mono<T> tMono);


    /**
     * @param tFlux
     * @param <T>
     * @return
     */
    <T> Flux<T> await(@NonNull Flux<T> tFlux);
}
