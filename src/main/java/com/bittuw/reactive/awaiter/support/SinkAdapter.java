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

package com.bittuw.reactive.awaiter.support;

import org.springframework.lang.NonNull;
import reactor.core.Disposable;

import java.util.function.LongConsumer;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 03.03.2020
 */
public interface SinkAdapter {


    /**
     *
     */
    void complete();


    /**
     *
     */
    void error(Throwable throwable);


    /**
     * @param disposable
     */
    void onCancel(@NonNull Disposable disposable);


    /**
     * @param disposable
     */
    void onDispose(@NonNull Disposable disposable);


    /**
     * @param consumer
     */
    void onRequest(@NonNull LongConsumer consumer);
}
