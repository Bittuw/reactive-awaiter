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

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Function;


/**
 * in the first execution if {@link #lazy} if null, try to construct it, else return already constructed
 *
 * @param <T>
 * @param <R>
 * @author Nikita Dmitriev {@literal <ndmitirev@topcon.com>}
 * @since 15.10.2019
 */
@RequiredArgsConstructor(staticName = "create")
public class Lazy<T, R> implements Function<T, R> {


    /**
     *
     */
    @NonNull
    private final Function<T, R> generator;


    /**
     *
     */
    @Nullable
    private R lazy;


    /**
     * @param t
     * @return
     */
    @Override
    public R apply(T t) {
        return Objects.isNull(lazy) ? lazy = generator.apply(t) : lazy;
    }
}
