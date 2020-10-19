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

import com.bittuw.reactive.awaiter.context.Context;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
@RequiredArgsConstructor(staticName = "create")
public class ContextMaintainer implements GenericMaintainer<ContextMaintainer, Context> {


    /**
     *
     */
    private final static AtomicReferenceFieldUpdater<ContextMaintainer, Context> updater =
            AtomicReferenceFieldUpdater.newUpdater(ContextMaintainer.class, Context.class, "hash");


    /**
     *
     */
    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private volatile Context hash;


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getMaintainedType() {
        return Context.class;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Context> getMaintainedValue() {
        return Optional.ofNullable(hash);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AtomicReferenceFieldUpdater<ContextMaintainer, Context> getUpdater() {
        return updater;
    }
}
