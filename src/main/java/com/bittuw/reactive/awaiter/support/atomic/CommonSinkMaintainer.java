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

import com.bittuw.reactive.awaiter.sinks.CommonSink;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
@RequiredArgsConstructor(staticName = "create")
public class CommonSinkMaintainer implements GenericMaintainer<CommonSinkMaintainer, CommonSink> {


    /**
     *
     */
    private final static AtomicReferenceFieldUpdater<CommonSinkMaintainer, CommonSink> updater =
            AtomicReferenceFieldUpdater.newUpdater(CommonSinkMaintainer.class, CommonSink.class, "commonSink");


    /**
     *
     */
    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private volatile CommonSink commonSink;


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getMaintainedType() {
        return CommonSink.class;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommonSink> getMaintainedValue() {
        return Optional.of(commonSink);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AtomicReferenceFieldUpdater<CommonSinkMaintainer, CommonSink> getUpdater() {
        return updater;
    }

}
