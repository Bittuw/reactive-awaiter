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

import org.springframework.lang.NonNull;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
public interface ContextOwner extends Context {


    /**
     * Invoking on cancel signal
     *
     * @param context
     */
    void cancel(@NonNull Context context);


    /**
     * Invoking on complete signal
     *
     * @param context
     */
    void close(@NonNull Context context);


    /**
     * @param throwable
     */
    void propagate(@NonNull Context context, @NonNull Throwable throwable);
}
