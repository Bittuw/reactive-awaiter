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

package com.bittuw.reactive.awaiter.support.lazy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 14.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class LazyFactory<I> {


    /**
     *
     */
    private final Class<I> interfaceClass;


    /**
     *
     */
    private final Supplier<? extends I> supplier;


    /**
     * @return
     */
    I getLazyObject() {
        return interfaceClass.cast(Proxy
                .newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                        new LazyFactory<I>.DynamicInvocationHandler()));
    }


    /**
     * @param interfaceClass
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> T getLazy(@NonNull Class<T> interfaceClass, @NonNull Supplier<? extends T> supplier) {
        return (new LazyFactory<T>(interfaceClass, supplier) {}).getLazyObject();
    }


    /**
     *
     */
    public class DynamicInvocationHandler implements InvocationHandler {


        /**
         *
         */
        @Getter(lazy = true)
        private final I internalObject = supplier.get();


        /**
         * {@inheritDoc}
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(getInternalObject(), args);
        }
    }
}
