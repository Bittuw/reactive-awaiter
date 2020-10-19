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


import com.bittuw.reactive.awaiter.impls.DefaultReactiveAwaiter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;


/**
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Simple reactive executor suite")
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Tags({@Tag("reactive.awaiter"), @Tag("reactive")})
@SuppressWarnings("NonAtomicOperationOnVolatileField")
class DefaultReactiveAwaiterTest {


    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultReactiveAwaiterTest.class);


    /**
     *
     */
    private static final Scheduler scheduler = Schedulers.newParallel("under-test", 5);


    /**
     *
     */
    protected final ReactiveAwaiter underlying = new DefaultReactiveAwaiter();


    /**
     *
     */
    protected volatile int increased = 0;


    /**
     * @param lambda
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> T spyLambda(@NonNull T lambda) {
        Class<?>[] interfaces = lambda.getClass().getInterfaces();
        assertEquals(1, interfaces.length);
        return mock((Class<T>) interfaces[0], delegatesTo(lambda));
    }


    /**
     *
     */
    @AfterEach
    void clean() {
        increased = 0;
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
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute single Flux immediately in current thread")
    void testExecuteFluxImmediately() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var passed = Flux.concat(incMono, incMono, incMono);
        StepVerifier.create(underlying.await(passed))
                .expectNext(1, 2, 3)
                .verifyComplete();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute single Flux in current thread and immediately cancel")
    void testExecuteFluxImmediatelyCancel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var passed = Flux.concat(incMono, incMono, incMono);
        StepVerifier.create(underlying.await(passed))
                .thenCancel()
                .verify();
        assertEquals(0, this.increased);
        verify(inc, times(0)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute single Flux immediately in current thread")
    void testExecuteFluxImmediatelyWithException() {
        final var inc = spyLambda((Callable<Integer>) () -> {throw new RuntimeException("Test error");});
        final var incMono = Mono.fromCallable(inc);
        final var passed = Flux.concat(incMono, incMono, incMono);
        StepVerifier.create(underlying.await(passed))
                .verifyError(RuntimeException.class);
        assertEquals(0, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute single Flux in other thread")
    void testExecuteFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var passed = Flux.concat(incMono, incMono, incMono);
        StepVerifier.create(underlying.await(passed).subscribeOn(scheduler))
                .expectNext(1, 2, 3)
                .verifyComplete();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute single Mono immediately in current thread")
    void testExecuteMonoImmediately() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var passed = Mono.fromCallable(inc);
        StepVerifier.create(underlying.await(passed))
                .expectNext(1)
                .verifyComplete();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @Timeout(value = 5)
    @DisplayName("Execute single Mono in other thread")
    void testExecuteMonoParallel() {
        final var passed = Mono.fromCallable(() -> this.increased += 1);
        StepVerifier.create(underlying.await(passed).subscribeOn(scheduler))
                .expectNext(1)
                .verifyComplete();
        assertEquals(1, this.increased);
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute nested Flux immediately in current thread")
    void testExecuteNestedFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var passed = underlying.await(flux);
        StepVerifier.create(underlying.await(passed))
                .expectNext(1, 2, 3)
                .verifyComplete();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute nested Mono immediately in current thread")
    void testExecuteNestedMono() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var passed = underlying.await(incMono);
        StepVerifier.create(underlying.await(passed))
                .expectNext(1)
                .verifyComplete();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute one nested Mono and two upper layer Mono in different threads")
    void testExecuteOneNestedTwoUpperMonoParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var nested_one = underlying.await(incMono).subscribeOn(scheduler);
        final var nested = underlying.await(nested_one);
        final var upper_layer_one = underlying.await(incMono).subscribeOn(scheduler);
        final var upper_layer_two = underlying.await(incMono).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer_one, upper_layer_two, nested))
                .expectNext(1, 2, 3)
                .verifyComplete();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Flux immediately in current thread")
    void testExecuteTwoFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux);
        final var two = underlying.await(flux);
        StepVerifier.create(Flux.merge(one, two))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName(
            "Execute two Flux immediately in current thread and immediately cancel it after the 1 will be received")
    void testExecuteTwoFluxAndCancel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).delaySubscription(Duration.ofSeconds(2));
        final var two = underlying.await(flux);
        StepVerifier.create(Flux.merge(one, two))
                .expectNext(1)
                .thenCancel()
                .verify();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Flux in different threads")
    void testExecuteTwoFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono immediately in current thread")
    void testExecuteTwoMono() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono);
        final var two = underlying.await(incMono);
        StepVerifier.create(Flux.merge(one, two))
                .expectNext(1, 2)
                .verifyComplete();
        assertEquals(2, this.increased);
        verify(inc, times(2)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono in different threads")
    void testExecuteTwoMonoParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono).subscribeOn(scheduler);
        final var two = underlying.await(incMono).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two))
                .expectNext(1, 2)
                .verifyComplete();
        assertEquals(2, this.increased);
        verify(inc, times(2)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux immediately in current thread")
    void testExecuteTwoNestedFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux);
        final var two = underlying.await(flux);
        StepVerifier.create(this.underlying.await(Flux.merge(one, two)))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux in different threads")
    void testExecuteTwoNestedFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(this.underlying.await(Flux.merge(one, two)).subscribeOn(scheduler))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux in different threads")
    void testExecuteTwoNestedFluxParallelWithException() {
        final var inc = spyLambda((Callable<Integer>) () -> {throw new RuntimeException("Test error");});
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(this.underlying.await(Flux.merge(one, two)).subscribeOn(scheduler))
                .verifyError(RuntimeException.class);
        assertEquals(0, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Mono immediately in current thread")
    void testExecuteTwoNestedMono() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono);
        final var two = underlying.await(incMono);
        StepVerifier.create(underlying.await(Flux.merge(one, two)))
                .expectNext(1, 2)
                .verifyComplete();
        assertEquals(2, this.increased);
        verify(inc, times(2)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Mono in different threads")
    void testExecuteTwoNestedMonoParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono).subscribeOn(scheduler);
        final var two = underlying.await(incMono).subscribeOn(scheduler);
        StepVerifier.create(underlying.await(Flux.merge(one, two)).subscribeOn(scheduler))
                .expectNext(1, 2)
                .verifyComplete();
        assertEquals(2, this.increased);
        verify(inc, times(2)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux in different threads where one nested is delay for 2 seconds")
    void testExecuteTwoNestedOneSleepFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = Flux.defer(() -> underlying.await(flux)).subscribeOn(scheduler)
                .delaySubscription(Duration.ofSeconds(2));
        final var two = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(this.underlying.await(Flux.merge(one, two)).subscribeOn(scheduler))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux and one upper layer Flux in current thread")
    void testExecuteTwoNestedOneUpperFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux);
        final var two = underlying.await(flux);
        final var nested = underlying.await(Flux.concat(one, two));
        final var upper_layer = underlying.await(flux);
        StepVerifier.create(Flux.merge(upper_layer, nested))
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .verifyComplete();
        assertEquals(9, this.increased);
        verify(inc, times(9)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux and one upper layer Flux in different threads")
    void testExecuteTwoNestedOneUpperFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        final var nested = underlying.await(Flux.concat(one, two)).subscribeOn(scheduler);
        final var upper_layer = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer, nested))
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .verifyComplete();
        assertEquals(9, this.increased);
        verify(inc, times(9)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux and one upper layer Flux in different threads and immediately cancel it")
    void testExecuteTwoNestedOneUpperFluxParallelAndCancel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        final var nested = underlying.await(Flux.concat(one, two)).subscribeOn(scheduler);
        final var upper_layer = underlying.await(flux).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer, nested))
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .verifyComplete();
        verify(inc, times(9)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Mono and one upper layer Mono in current thread")
    void testExecuteTwoNestedOneUpperMono() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono);
        final var two = underlying.await(incMono);
        final var nested = underlying.await(Mono.zip(one, two));
        final var upper_layer = underlying.await(incMono);
        StepVerifier.create(Mono.zip(upper_layer, nested))
                .expectNext(Tuples.of(1, Tuples.of(2, 3)))
                .verifyComplete();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }
}
