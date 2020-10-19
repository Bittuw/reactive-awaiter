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

import com.bittuw.reactive.awaiter.context.impls.DefaultNestedContext;
import com.bittuw.reactive.awaiter.support.generators.StringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.scheduler.Scheduler;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
@Slf4j
public abstract class AbstractRootContext extends AbstractContext implements RootContext, ContextOwner {


    /**
     * @param hash
     * @param scheduler
     * @param defaultKeyPrefix
     */
    public AbstractRootContext(@NonNull Object hash,
                               @Nullable Scheduler scheduler,
                               @Nullable String defaultKeyPrefix)
    {
        super(hash, scheduler, defaultKeyPrefix);
    }


    /**
     * @param parent
     * @return
     */
    @Override
    public NestedContext generateChildContext(@NonNull ContextOwner parent)
    {
        return DefaultNestedContext
                .of(StringGenerator.DEFAULT_32.generate(), parent, getScheduler(), getDefaultKeyPrefix(),
                        getChainHashKeyName());
    }
}
