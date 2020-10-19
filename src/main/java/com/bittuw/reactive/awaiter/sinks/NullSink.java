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


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 21.05.2020
 */
public final class NullSink implements CommonSink {


    /**
     *
     */
    @Override
    public void cancel() {

    }


    /**
     *
     */
    @Override
    public void dispose() {
        throw new UnsupportedOperationException();
    }


    /**
     * @return
     */
    @Override
    public Response execute() {
        throw new UnsupportedOperationException();
    }


    /**
     * @return
     */
    @Override
    public Context getContext() {
        throw new UnsupportedOperationException();
    }


    /**
     * @return
     */
    @Override
    public SinkAdapter getSinkAdapter() {
        throw new UnsupportedOperationException();
    }


    /**
     * @param n
     */
    @Override
    public void request(long n) {
        throw new UnsupportedOperationException();
    }
}
