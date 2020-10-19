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

package com.bittuw.reactive.awaiter.support.atomic;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Using as field container in Json/Xml serializable objects if the field can be changed in other thread
 *
 * @author Nikita Dmitriev {@literal <ndmitirev@topcon.com>}
 * @since 22.11.2019
 */
@SuppressWarnings("unchecked")
public interface GenericMaintainer<P extends GenericMaintainer<P, T>, T> {


    /**
     * @param expected
     * @param consumer
     */
    default void doIfExpected(@NonNull T expected, @NonNull Consumer<? super T> consumer) {
        var optional = getMaintainedValue();
        if (optional.isPresent() && expected.equals(optional.get()))
            consumer.accept(optional.get());
    }


    /**
     * @param predicate
     * @param runnable
     */
    default boolean doIfMatch(@NonNull Predicate<? super T> predicate, @NonNull Runnable runnable) {
        var optional = getMaintainedValue();
        if (optional.isPresent() && predicate.test(optional.get())) {
            runnable.run();
            return true;
        }
        return false;
    }


    /**
     * @param predicate
     * @param consumer
     */
    default void doWithValueIfMatch(@NonNull Predicate<? super T> predicate, @NonNull Consumer<? super T> consumer) {
        var optional = getMaintainedValue();
        if (optional.isPresent() && predicate.test(optional.get()))
            consumer.accept(optional.get());
    }


    /**
     * @return
     */
    Class<?> getMaintainedType();


    /**
     * @return
     */
    Optional<T> getMaintainedValue();


    /**
     * @return
     */
    AtomicReferenceFieldUpdater<P, T> getUpdater();


    /**
     * Check if maintainedValue has expected value
     *
     * @param expected
     * @return
     */
    default boolean isExpected(@NonNull T expected) {
        var optional = getMaintainedValue();
        return optional.isPresent() && expected.equals(optional.get());
    }


    /**
     * Check if maintainedValue corresponds to condition
     *
     * @param predicate
     * @return
     */
    default boolean isMatch(@NonNull Predicate<? super T> predicate) {
        var optional = getMaintainedValue();
        return optional.isPresent() && predicate.test(optional.get());
    }


    /**
     * @return
     */
    default boolean isNullable() {
        return getMaintainedValue().isEmpty();
    }


    /**
     * @return
     */
    default Mono<T> lazyGetMaintainedValue() {
        return Mono.justOrEmpty(getMaintainedValue());
    }


    /**
     * Replace prev and return it
     *
     * @param function
     */
    default T replace(@NonNull Function<T, T> function) {
        var updater = getUpdater();
        T prev, next;
        do {
            prev = updater.get((P) this);
            next = function.apply(prev);
        } while (!updater.compareAndSet((P) this, prev, next));
        return prev;
    }


    /**
     * Try replace internal value while value is expected
     *
     * @param expected
     * @param next
     * @return
     */
    default boolean replaceIfExpected(@NonNull T expected, @NonNull T next) {
        return replaceIfExpected(next, prev -> prev.equals(expected));
    }


    /**
     * Try replace internal value while value is expected
     *
     * @param next
     * @param predicate
     * @return
     */
    default boolean replaceIfExpected(@Nullable T next, @NonNull Predicate<? super T> predicate) {
        P current = (P) this;
        final var updater = getUpdater();
        T prev;
        do {
            prev = updater.get(current);
            if (!predicate.test(prev)) return false;
        } while (!updater.compareAndSet(current, prev, next));
        return true;
    }


    /**
     * @param update
     * @param isExpected
     * @return
     */
    default boolean replaceIfExpected(@NonNull Function<? super T, ? extends T> update,
                                      @NonNull Predicate<? super T> isExpected)
    {
        P current = (P) this;
        var updater = getUpdater();
        T prev, next;
        do {
            prev = updater.get(current);
            if (!isExpected.test(prev)) return false;
            next = update.apply(prev);
        } while (!updater.compareAndSet(current, prev, next));
        return true;
    }


    /**
     * @param value
     */
    default void update(@NonNull T value) {
        getUpdater().set((P) this, value);
    }


    /**
     * @param next
     * @param expected
     * @return
     */
    default boolean updateExpected(@Nullable T next, @NonNull T expected) {
        return getUpdater().compareAndSet((P) this, expected, next);
    }
}
