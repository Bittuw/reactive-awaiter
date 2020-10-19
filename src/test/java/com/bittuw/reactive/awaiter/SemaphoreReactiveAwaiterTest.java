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

package com.bittuw.reactive.awaiter;

import com.bittuw.reactive.awaiter.impls.DefaultSemaphoreReactiveAwaiter;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * Not need to test concurrent version because this kind of usage degenerates into ordinary usage of fluxSink
 * with manually call {@link org.reactivestreams.Subscription#request(long)} into {@link Flux#doFinally(Consumer)}
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Tags({@Tag("reactive"), @Tag("reactive.awaiter")})
@DisplayName("Test semaphore style context execution")
class SemaphoreReactiveAwaiterTest {


    /**
     *
     */
    protected final static AtomicInteger increased = new AtomicInteger(0);


    /**
     *
     */
    protected static final Scheduler scheduler = Schedulers.newParallel("under-test", 6);


    /**
     *
     */
    private final static Integer THRESHOLD = 2;


    /**
     *
     */
    private final Logger log = LoggerFactory.getLogger(SemaphoreReactiveAwaiterTest.class);


    /**
     *
     */
    private final SemaphoreReactiveAwaiter underlying = new DefaultSemaphoreReactiveAwaiter(THRESHOLD) {};


    /**
     *
     */
    @AfterEach
    void clean() {
        increased.set(0);
    }


    /**
     * @param testInfo
     */
    @BeforeEach
    void init(TestInfo testInfo) {
        log.info("Starting '{}' in method {}", testInfo.getDisplayName(), testInfo.getTestMethod());
    }


    /**
     *
     */
    @Test
    @DisplayName("Test that executor may execute 1 context in current thread")
    void oneContextExecutionInCurrentThread() {
        final var dummy = Mono.fromCallable(increased::incrementAndGet);
        final var flux = Flux.merge(dummy, dummy, dummy, dummy, dummy);
        StepVerifier.create(underlying.await(flux))
                .expectNextCount(5).verifyComplete();
    }


    /**
     *
     */
    @Test
    @DisplayName("Test that executor may execute overflowed count of contexts in current thread")
    void overflowedContextExecutionInCurrentThread() {
        final var dummy = underlying.await(Mono.fromCallable(increased::incrementAndGet));
        final var flux = Flux.merge(dummy, dummy, dummy, dummy, dummy, dummy);
        StepVerifier.create(flux)
                .expectNextCount(6).verifyComplete();
    }


    /**
     *
     */
    @Test
    @DisplayName("Test that executor may execute overflowed count of contexts in different threads")
    void overflowedContextExecutionInDifferentThreads() {
        final var dummy = underlying.await(Mono.fromCallable(() -> {
            final var r = increased.incrementAndGet();
            Assertions.assertTrue(r < 3);
            return r;
        }).doOnTerminate(increased::decrementAndGet).delaySubscription(Duration.ofSeconds(1)));
        final var flux = Flux.merge(
                dummy.subscribeOn(scheduler),
                dummy.subscribeOn(scheduler),
                dummy.subscribeOn(scheduler),
                dummy.subscribeOn(scheduler),
                dummy.subscribeOn(scheduler),
                dummy.subscribeOn(scheduler));
        StepVerifier.create(flux)
                .expectNextCount(6).verifyComplete();
    }
}
