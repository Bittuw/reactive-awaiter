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


import com.bittuw.reactive.awaiter.context.impls.DefaultRootAwaitableContext;
import com.bittuw.reactive.awaiter.impls.DefaultFallbackReactiveAwaiter;
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
import reactor.test.publisher.PublisherProbe;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;


/**
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fallback reactive awaiter suite")
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Tags({@Tag("reactive.awaiter"), @Tag("reactive")})
@SuppressWarnings("NonAtomicOperationOnVolatileField")
class FallbackReactiveAwaiterTest {


    /**
     *
     */
    protected final static Scheduler scheduler = Schedulers.newParallel("under-test", 5);


    /**
     *
     */
    private final Logger log = LoggerFactory.getLogger(FallbackReactiveAwaiterTest.class);


    /**
     *
     */
    private final FallbackReactiveAwaiter underlying = new DefaultFallbackReactiveAwaiter() {};


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
        final var incMono = Mono.fromCallable(inc);
        StepVerifier.create(underlying.await(incMono))
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
    @DisplayName("Execute single Mono in other thread")
    void testExecuteMonoParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        StepVerifier.create(underlying.await(incMono).subscribeOn(scheduler))
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
    @DisplayName("Execute nested Flux immediately in current thread")
    void testExecuteNestedFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.merge(incMono, incMono, incMono);
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
        final var upper_layer_one = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        final var upper_layer_two = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
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
    @DisplayName("Execute one nested Mono and two upper layer Mono in different threads (Fallback)")
    void testExecuteOneNestedTwoUpperMonoParallelFallback() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var nested_one = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(2));
        final var nested = underlying.await(nested_one).subscribeOn(scheduler);
        final var upper_layer_one =
                Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        final var upper_layer_two =
                Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer_one, upper_layer_two, nested))
                .verifyError(DefaultRootAwaitableContext.Fallback.class);
        assertEquals(0, this.increased);
        verify(inc, times(0)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 10)
    @DisplayName("Execute one nested Mono and two upper layer Mono in different threads (Fallback check)")
    void testExecuteOneNestedTwoUpperMonoParallelFallbackCheck() {
        final var probe_one = PublisherProbe.<Integer>empty();
        final var probe_two = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var nested_one = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
        final var nested = underlying.await(nested_one);
        final var upper_layer_one =
                Mono.defer(() -> underlying.awaitOrFallback(incMono, probe_one.mono()))
                        .delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        final var upper_layer_two =
                Mono.defer(() -> underlying.awaitOrFallback(incMono, probe_two.mono()))
                        .delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        Flux.merge(upper_layer_one, upper_layer_two, nested).doOnNext(next -> log.debug("Passed integer: {}", next))
                .blockLast();
        probe_one.assertWasSubscribed();
        probe_one.assertWasRequested();
        probe_two.assertWasSubscribed();
        probe_two.assertWasRequested();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute one nested Mono and two upper layer Mono in different threads (Wait)")
    void testExecuteOneNestedTwoUpperMonoParallelWait() {
        final var probe_one = PublisherProbe.<Integer>empty();
        final var probe_two = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var nested_one = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
        final var nested = underlying.await(nested_one);
        final var upper_layer_one =
                Mono.defer(() -> underlying.awaitOrWait(incMono, probe_one.mono()))
                        .delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        final var upper_layer_two =
                Mono.defer(() -> underlying.awaitOrWait(incMono, probe_two.mono()))
                        .delaySubscription(Duration.ofSeconds(1))
                        .subscribeOn(scheduler);
        Flux.merge(upper_layer_one, upper_layer_two, nested).doOnNext(next -> log.debug("Passed integer: {}", next))
                .blockLast();
        probe_one.assertWasSubscribed();
        probe_one.assertWasRequested();
        probe_two.assertWasSubscribed();
        probe_two.assertWasRequested();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
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
        final var flux = Flux.merge(incMono, incMono, incMono);
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
    @DisplayName("Execute two Flux in different threads")
    void testExecuteTwoFluxParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = Flux.defer(() -> underlying.await(flux)).delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
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
    @DisplayName("Execute two Flux in different threads (Fallback)")
    void testExecuteTwoFluxParallelFallback() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.merge(incMono, incMono, incMono);
        final var one = underlying.await(flux.delaySubscription(Duration.ofSeconds(2))).subscribeOn(scheduler);
        final var two = Flux.defer(() -> underlying.await(flux)).delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two))
                .verifyError(DefaultRootAwaitableContext.Fallback.class);
        assertEquals(0, this.increased);
        verify(inc, times(0)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Flux in different threads (Fallback check)")
    void testExecuteTwoFluxParallelFallbackCheck() {
        final var probe = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux =
                Flux.defer(() -> Flux.concat(incMono, incMono, incMono)).delaySubscription(Duration.ofSeconds(2));
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = Flux.defer(() -> underlying.awaitOrFallback(flux, probe.flux()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two).doOnNext(next -> log.debug("Passed integer: {}", next)))
                .expectNext(1, 2, 3)
                .verifyComplete();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Flux in different threads (Wait)")
    void testExecuteTwoFluxParallelWait() {
        final var probe = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux =
                Flux.defer(() -> Flux.concat(incMono, incMono, incMono)).delaySubscription(Duration.ofSeconds(2));
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = Flux.defer(() -> underlying.awaitOrWait(flux, probe.flux()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        Flux.merge(one, two).doOnNext(next -> log.debug("Passed integer: {}", next)).blockLast();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono in different threads with one will be delayed for 2 seconds")
    void testExecuteTwoMonoParallel() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = Mono.defer(() -> underlying.await(incMono)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
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
    @DisplayName("Execute two Mono in different threads (Fallback)")
    void testExecuteTwoMonoParallelFallback() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.defer(() -> Mono.fromCallable(inc)).delaySubscription(Duration.ofSeconds(1));
        final var one = underlying.await(incMono).subscribeOn(scheduler);
        final var two = underlying.await(incMono).subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two))
                .verifyError(DefaultRootAwaitableContext.Fallback.class);
        assertEquals(0, this.increased);
        verify(inc, times(0)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono in different threads (Fallback check)")
    void testExecuteTwoMonoParallelFallbackCheck() {
        final PublisherProbe<Integer> probe = PublisherProbe.empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono.delaySubscription(Duration.ofSeconds(2))).subscribeOn(scheduler);
        final var two = Mono.defer(() -> underlying.awaitOrFallback(incMono, probe.mono()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two).doOnNext(next -> log.debug("Passed integer: {}", next)))
                .expectNext(1)
                .verifyComplete();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono in different threads (Wait)")
    void testExecuteTwoMonoParallelWait() {
        final PublisherProbe<Integer> probe = PublisherProbe.empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono.delaySubscription(Duration.ofSeconds(2))).subscribeOn(scheduler);
        final var two = Mono.defer(() -> underlying.awaitOrWait(incMono, probe.mono()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(one, two).doOnNext(next -> log.debug("Passed integer: {}", next)))
                .expectNext(1)
                .verifyComplete();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(1, this.increased);
        verify(inc, times(1)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two Mono immediately bu delay subscription in one in current thread")
    void testExecuteTwoMonoWithDelaySubscription() {
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
    @DisplayName("Execute two nested Mono immediately in current thread")
    void testExecuteTwoNestedMono() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var one = underlying.await(incMono);
        final var two = underlying.await(incMono);
        StepVerifier.create(underlying.await(Flux.concat(one, two)))
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
    @DisplayName("Execute two nested Flux and one upper layer Flux in current thread")
    void testExecuteTwoNestedOneUpperFlux() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux);
        final var two = underlying.await(flux);
        final var nested = underlying.await(Flux.merge(one, two));
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
        final var nested = underlying.await(Flux.merge(one, two)).subscribeOn(scheduler);
        final var upper_layer = Flux.defer(() -> underlying.await(flux)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
        StepVerifier
                .create(Flux.merge(upper_layer, nested).doOnNext(integer -> log.info("Passed integer: {}", integer)))
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
    @DisplayName("Execute two nested Flux and one upper layer Flux in different threads (Fallback)")
    void testExecuteTwoNestedOneUpperFluxParallelFallback() {
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = Flux.defer(() -> underlying.await(flux)).delaySubscription(Duration.ofSeconds(2))
                .subscribeOn(scheduler);
        final var nested = underlying.await(Flux.merge(one, two)).subscribeOn(scheduler);
        final var upper_layer = Flux.defer(() -> underlying.await(flux)).delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer, nested))
                .expectNext(1, 2, 3)
                .verifyError(DefaultRootAwaitableContext.Fallback.class);
        assertEquals(3, this.increased);
        verify(inc, times(3)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux and one upper layer Flux in different threads (Fallback check)")
    void testExecuteTwoNestedOneUpperFluxParallelFallbackCheck() {
        final var probe = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux).subscribeOn(scheduler);
        final var nested = underlying.await(Flux.merge(one, two).delaySubscription(Duration.ofSeconds(2)));
        final var upper_layer = Flux.defer(() -> underlying.awaitOrFallback(flux, probe.flux()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        StepVerifier.create(Flux.merge(upper_layer, nested).doOnNext(next -> log.debug("Passed integer: {}", next)))
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }


    /**
     *
     */
    @Test
    @SneakyThrows
    @Timeout(value = 5)
    @DisplayName("Execute two nested Flux and one upper layer Flux in different threads (Wait)")
    void testExecuteTwoNestedOneUpperFluxParallelWait() {
        final var probe = PublisherProbe.<Integer>empty();
        final var inc = spyLambda((Callable<Integer>) () -> increased += 1);
        final var incMono = Mono.fromCallable(inc);
        final var flux = Flux.concat(incMono, incMono, incMono);
        final var one = underlying.await(flux).subscribeOn(scheduler);
        final var two = underlying.await(flux.delaySubscription(Duration.ofSeconds(2))).subscribeOn(scheduler);
        final var nested = underlying.await(Flux.merge(one, two));
        final var upper_layer = Flux.defer(() -> underlying.awaitOrWait(flux, probe.flux()))
                .delaySubscription(Duration.ofSeconds(1))
                .subscribeOn(scheduler);
        Flux.merge(upper_layer, nested).doOnNext(next -> log.debug("Passed integer: {}", next)).blockLast();
        probe.assertWasSubscribed();
        probe.assertWasRequested();
        assertEquals(6, this.increased);
        verify(inc, times(6)).call();
    }
}
