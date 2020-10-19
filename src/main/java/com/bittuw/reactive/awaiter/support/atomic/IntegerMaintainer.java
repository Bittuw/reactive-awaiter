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

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 07.05.2020
 */
@RequiredArgsConstructor(staticName = "create")
public class IntegerMaintainer implements GenericMaintainer<IntegerMaintainer, Integer> {


    /**
     *
     */
    private final static AtomicReferenceFieldUpdater<IntegerMaintainer, Integer> integerView =
            AtomicReferenceFieldUpdater.newUpdater(IntegerMaintainer.class, Integer.class, "integer");


    /**
     *
     */
    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private volatile Integer integer;


    /**
     * @return
     */
    @Override
    public Class<?> getMaintainedType() {
        return Integer.class;
    }


    /**
     * @return
     */
    @Override
    public Optional<Integer> getMaintainedValue() {
        return Optional.of(integer);
    }


    /**
     * @return
     */
    @Override
    public AtomicReferenceFieldUpdater<IntegerMaintainer, Integer> getUpdater() {
        return integerView;
    }
}
