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

package com.bittuw.reactive.awaiter.context.impls;

import com.bittuw.reactive.awaiter.context.AbstractRootContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.scheduler.Scheduler;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
public final class DefaultRootContext extends AbstractRootContext {


    /**
     * @param hash
     */
    public DefaultRootContext(@NonNull Object hash) {
        this(hash, null, null);
    }


    /**
     * @param hash
     * @param scheduler
     */
    public DefaultRootContext(@NonNull Object hash, @NonNull Scheduler scheduler) {
        this(hash, scheduler, null);
    }


    /**
     * @param hash
     * @param defaultKeyPrefix
     */
    public DefaultRootContext(@NonNull Object hash, @NonNull String defaultKeyPrefix) {
        this(hash, null, defaultKeyPrefix);
    }


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     */
    public DefaultRootContext(@NonNull Object hash, @Nullable Scheduler scheduler, @Nullable String defaultKeyPrefix)
    {
        super(hash, scheduler, defaultKeyPrefix);
    }


    /**
     * @param hash
     * @return
     */
    public static DefaultRootContext of(@NonNull Object hash) {
        return new DefaultRootContext(hash);
    }
}
