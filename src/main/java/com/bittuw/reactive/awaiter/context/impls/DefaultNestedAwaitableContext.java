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

import com.bittuw.reactive.awaiter.context.AbstractNestedAwaitableContext;
import com.bittuw.reactive.awaiter.context.ContextOwner;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.scheduler.Scheduler;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
public final class DefaultNestedAwaitableContext extends AbstractNestedAwaitableContext {


    /**
     * @param hash
     * @param parent
     * @param scheduler
     * @param defaultKeyPrefix
     * @param chainHashKeyName
     */
    public DefaultNestedAwaitableContext(@NonNull Object hash,
                                         @NonNull ContextOwner parent, @Nullable Scheduler scheduler,
                                         @Nullable String defaultKeyPrefix, @NonNull String chainHashKeyName)
    {
        super(hash, parent, scheduler, defaultKeyPrefix, chainHashKeyName);
    }


    /**
     * @param hash
     * @param parent
     * @return
     */
    public static DefaultNestedAwaitableContext of(@NonNull Object hash, @NonNull ContextOwner parent,
                                                   @Nullable Scheduler scheduler, @Nullable String defaultKeyPrefix,
                                                   @NonNull String chainHashKeyName)
    {
        return new DefaultNestedAwaitableContext(hash, parent, scheduler, defaultKeyPrefix, chainHashKeyName);
    }
}
