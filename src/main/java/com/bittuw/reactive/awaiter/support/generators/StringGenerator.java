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

package com.bittuw.reactive.awaiter.support.generators;

import com.bittuw.reactive.awaiter.support.lazy.LazyFactory;

import java.util.Random;


/**
 * @author Nikita Dmitriev {@literal <bittumworker@mail.ru>}
 * @since 03.03.2020
 */
@SuppressWarnings("unchecked")
public class StringGenerator implements Generator<String> {


    /**
     *
     */
    public static final Generator<String> DEFAULT_128 =
            LazyFactory.getLazy(Generator.class, () -> new StringGenerator(128));


    /**
     *
     */
    public static final Generator<String> DEFAULT_16 =
            LazyFactory.getLazy(Generator.class, () -> new StringGenerator(16));


    /**
     *
     */
    public static final Generator<String> DEFAULT_32 =
            LazyFactory.getLazy(Generator.class, () -> new StringGenerator(32));


    /**
     *
     */
    public static final Generator<String> DEFAULT_64 =
            LazyFactory.getLazy(Generator.class, () -> new StringGenerator(64));


    /**
     *
     */
    private final static int leftLimit = 48;


    /**
     *
     */
    private final static int rightLimit = 122;


    /**
     *
     */
    private final Random random = new Random();


    /**
     *
     */
    private int length;


    /**
     * @param length
     */
    public StringGenerator(int length) {
        if (length < 0)
            throw new RuntimeException("Length cannot be less then zero");
        this.length = length;
    }


    /**
     * @return
     */
    @Override
    public String generate() {
        return random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
