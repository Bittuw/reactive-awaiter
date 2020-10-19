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

import com.bittuw.reactive.awaiter.sinks.CommonSink;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
public abstract class AbstractNestedAwaitableContext extends AbstractNestedContext
        implements NestedAwaitableContext
{


    /**
     * This sink must be executed after close
     */
    @Getter(lazy = true)
    private final ConcurrentLinkedQueue<CommonSink> awaits = new ConcurrentLinkedQueue<>();


    /**
     * @param hash
     * @param parent
     * @param scheduler
     * @param defaultKeyPrefix
     * @param chainHashKeyName
     */
    public AbstractNestedAwaitableContext(@NonNull Object hash, @NonNull ContextOwner parent,
                                          @Nullable Scheduler scheduler, @Nullable String defaultKeyPrefix,
                                          @NonNull String chainHashKeyName)
    {
        super(hash, parent, scheduler, defaultKeyPrefix, chainHashKeyName);
    }


    /**
     * @param commonSink
     * @return
     */
    @Override
    public boolean add(@NonNull CommonSink commonSink) {
        getAwaits().add(commonSink);
        return isClosed();
    }


    /**
     *
     */
    @Override
    public void close() {
        super.close();
        getAwaits().forEach(commonSink -> commonSink.execute().response());
    }
}
