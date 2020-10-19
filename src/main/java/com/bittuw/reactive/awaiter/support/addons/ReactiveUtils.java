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

package com.bittuw.reactive.awaiter.support.addons;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.function.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 10.01.2020
 */
@Slf4j
public abstract class ReactiveUtils {


    /**
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> Mono<T> deferGetFromContext(@NonNull String key, @NonNull Class<T> tClass) {
        return Mono.deferWithContext(ctx -> Mono.justOrEmpty(ctx.getOrEmpty(key))).cast(tClass);
    }


    /**
     * @param tuple2
     * @param function
     * @param <T1>
     * @param <T2>
     * @param <R>
     */
    public static <T1, T2, R> Mono<R> flatMapTuple2(@NonNull Tuple2<T1, T2> tuple2,
                                                    @NonNull BiFunction<T1, T2, Mono<R>> function)
    {
        return function.apply(tuple2.getT1(), tuple2.getT2());
    }


    /**
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> Mono<T> getFromContext(@NonNull String key, @NonNull Class<T> tClass) {
        return Mono.subscriberContext().flatMap(ctx -> Mono.justOrEmpty(ctx.getOrEmpty(key))).cast(tClass);
    }


    /**
     * @param mono
     * @return
     */
    public static Mono<Boolean> materializeToBoolean(@NonNull Flux<?> mono,
                                                     @NonNull Predicate<? super Signal<?>> predicate)
    {
        return mono.materialize().filter(predicate).hasElements();
    }


    /**
     * @param key
     * @param object
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> Function<Flux<V>, ? extends Publisher<V>> setToContextFlux(@NonNull String key,
                                                                                    @NonNull T object)
    {
        return flux -> flux.subscriberContext(context -> context.put(key, object));
    }


    /**
     * @param key
     * @param object
     * @param <T>
     * @return
     */
    public static <V> Function<Mono<V>, ? extends Publisher<V>> setToContextMono(@NonNull String key,
                                                                                 @NonNull Object object)
    {
        return mono -> mono.subscriberContext(context -> context.put(key, object));
    }
}
