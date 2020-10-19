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

package com.bittuw.reactive.awaiter.sinks;

import com.bittuw.reactive.awaiter.context.Context;
import com.bittuw.reactive.awaiter.support.Response;
import com.bittuw.reactive.awaiter.support.SinkAdapter;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 02.03.2020
 */
public interface CommonSink extends Disposable, Subscription {


    /**
     *
     */
    CommonSink NULL_SINK = new NullSink();


    /**
     * @return
     */
    Response execute();


    /**
     * @return
     */
    Context getContext();


    /**
     * @return
     */
    SinkAdapter getSinkAdapter();
}
