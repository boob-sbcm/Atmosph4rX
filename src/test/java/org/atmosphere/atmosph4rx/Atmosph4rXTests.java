/**
 * Copyright 2018 Async-IO.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.atmosph4rx;

import org.atmosphere.atmosph4rx.annotation.ReactTo;
import org.atmosphere.atmosph4rx.annotation.Topic;
import org.atmosphere.atmosph4rx.core.MultiLinkProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Atmosph4rXApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
public class Atmosph4rXTests {

    @ReactTo("/test1")
    public final static class RxTest1 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Override
        public void onSubscribe(Subscription s) {
            onSubscribe = true;

            latch.countDown();
        }

        @Override
        public void onNext(String s) {
            onNext = true;
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @LocalServerPort
    private int port;


    @Test
    public void testRxTest1() throws URISyntaxException, InterruptedException {

        WebSocketClient client = new ReactorNettyWebSocketClient();

        URI url = new URI("ws://127.0.0.1:" + port + "/test1");
        client.execute(url, session ->
                session.receive()
                        .doOnNext(System.out::println)
                        .then()).subscribe();

        RxTest1.latch.await();

        assertTrue(RxTest1.onSubscribe);
        assertTrue(!RxTest1.onNext);
        assertTrue(!RxTest1.onComplete);
        assertTrue(!RxTest1.onError);
    }

    @ReactTo("/test2")
    public final static class RxTest2 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
            latch.countDown();
        }

        @Override
        public void onNext(String s) {
            onNext = true;
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest2() throws URISyntaxException, InterruptedException {

        WebSocketClient client = new ReactorNettyWebSocketClient();

        URI url = new URI("ws://127.0.0.1:" + port + "/test2");
        client.execute(url, session ->
                session.receive()
                        .doOnNext(System.out::println)
                        .then()).subscribe();

        RxTest2.latch.await();

        assertTrue(RxTest2.onSubscribe);
        assertTrue(!RxTest2.onNext);
        assertTrue(!RxTest2.onComplete);
        assertTrue(!RxTest2.onError);
    }

    @ReactTo("/test3")
    public final static class RxTest3 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
        }

        @Override
        public void onNext(String s) {
            onNext = true;
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest3() throws URISyntaxException, InterruptedException {

        WebSocketClient client = new ReactorNettyWebSocketClient();

        URI url = new URI("ws://127.0.0.1:" + port + "/test3");
        client.execute(url, session ->
                session.send(Flux.just("test3").map(session::textMessage)))
                .subscribe();

        RxTest3.latch.await();

        assertTrue(RxTest3.onSubscribe);
        assertTrue(RxTest3.onNext);
        assertTrue(!RxTest3.onComplete);
        assertTrue(!RxTest3.onError);
    }

    @ReactTo("/test4")
    public final static class RxTest4 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);
        static CountDownLatch latch2 = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        private AxSubscription sub;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
            this.sub = s;
        }

        @Override
        public void onNext(String s) {
            onNext = true;
            sub.cancel();
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
            latch2.countDown();
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest4() throws URISyntaxException, InterruptedException {

        WebSocketClient client = new ReactorNettyWebSocketClient();

        URI url = new URI("ws://127.0.0.1:" + port + "/test4");
        Disposable d = client.execute(url, session ->
                session.send(Flux.just("test4").map(session::textMessage)))
                .subscribe();

        RxTest4.latch.await(10, TimeUnit.SECONDS);
        RxTest4.latch2.await(10, TimeUnit.SECONDS);

        assertTrue(RxTest4.onSubscribe);
        assertTrue(RxTest4.onNext);
        assertTrue(RxTest4.onComplete);
        assertTrue(!RxTest4.onError);
    }

    @ReactTo("/test5")
    public final static class RxTest5 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);
        static CountDownLatch latch2 = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
        }

        @Override
        public void onNext(String s) {
            onNext = true;
            latch.countDown();
            throw new NullPointerException();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
            latch2.countDown();
        }

    }

    //    @Test
    public void testRxTest5() throws URISyntaxException, InterruptedException {

        WebSocketClient client = new ReactorNettyWebSocketClient();

        URI url = new URI("ws://127.0.0.1:" + port + "/test5");
        Disposable d = client.execute(url, session ->
                session.send(Flux.just("test4").map(session::textMessage)))
                .subscribe();

        RxTest5.latch.await();
        d.dispose();

        RxTest5.latch2.await();

        assertTrue(RxTest5.onSubscribe);
        assertTrue(RxTest5.onNext);
        assertTrue(!RxTest5.onComplete);
        assertTrue(RxTest5.onError);
    }

    @ReactTo("/test6")
    public final static class RxTest6 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
        }

        @Override
        public <U extends Processor<? super String, ? super String>, V> void onNext(Link<U, V> single) {
            onNext = true;
            single.toProcessor().onNext("test6-ping");
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    private static final Duration TIMEOUT = Duration.ofMillis(5000);

    @Test
    public void testRxTest6() throws URISyntaxException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger("test");
        WebSocketClient client = new ReactorNettyWebSocketClient();
        Flux<String> input = Flux.just("test6-ping");

        ReplayProcessor<Object> output = ReplayProcessor.create(1);

        URI url = new URI("ws://127.0.0.1:" + port + "/test6");
        client.execute(url, session ->
                session
                        .send(input.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        assertEquals(input.collectList().block(TIMEOUT), output.collectList().block(TIMEOUT));

        RxTest6.latch.await();

        assertTrue(RxTest6.onSubscribe);
        assertTrue(RxTest6.onNext);
        assertTrue(!RxTest6.onComplete);
        assertTrue(!RxTest6.onError);

    }

    @ReactTo("/test7")
    public final static class RxTest7 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(1);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Topic("test")
        private MultiLinkProcessor<String> broadcaster;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
            broadcaster.subscribe(s.link());
        }

        @Override
        public <U extends Processor<? super String, ? super String>, V> void onNext(Link<U, V> single) {
            onNext = true;
            broadcaster.publish("test7-ping");
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest7() throws URISyntaxException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger("test");
        WebSocketClient client = new ReactorNettyWebSocketClient();
        Flux<String> input = Flux.just("test7-ping");

        ReplayProcessor<Object> output = ReplayProcessor.create(1);

        URI url = new URI("ws://127.0.0.1:" + port + "/test7");
        client.execute(url, session ->
                session
                        .send(input.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        assertEquals(input.collectList().block(TIMEOUT), output.collectList().block(TIMEOUT));

        RxTest7.latch.await();

        assertTrue(RxTest7.onSubscribe);
        assertTrue(RxTest7.onNext);
        assertTrue(!RxTest7.onComplete);
        assertTrue(!RxTest7.onError);

    }

    @ReactTo("/test8")
    public final static class RxTest8 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(2);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Topic("test-8")
        private MultiLinkProcessor<String> broadcaster;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
            broadcaster.subscribe(s.link());
        }

        @Override
        public void onNext(String next) {
            onNext = true;
            broadcaster.publish(next);
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest8() throws URISyntaxException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger("test");
        WebSocketClient client = new ReactorNettyWebSocketClient();
        Flux<String> input = Flux.just("test8-ping");
        Flux<String> input2 = Flux.just("test8");

        ReplayProcessor<Object> output = ReplayProcessor.create(1);
        ReplayProcessor<Object> output2 = ReplayProcessor.create(1);


        URI url = new URI("ws://127.0.0.1:" + port + "/test8");
        client.execute(url, session ->
                session
                        .send(input.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        client.execute(url, session ->
                session
                        .send(input2.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output2)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        RxTest8.latch.await();

        assertEquals(input.mergeWith(input2).collectList().block(TIMEOUT), output.mergeWith(output2).collectList().block(TIMEOUT));
        
        assertTrue(RxTest8.onSubscribe);
        assertTrue(RxTest8.onNext);
        assertTrue(!RxTest8.onComplete);
        assertTrue(!RxTest8.onError);

    }

    @ReactTo("/test9")
    public final static class RxTest9 implements AxSubscriber<String> {

        static CountDownLatch latch = new CountDownLatch(2);

        static boolean onSubscribe;
        static boolean onNext;
        static boolean onComplete;
        static boolean onError;

        @Topic("test-8")
        private MultiLinkProcessor<String> broadcaster;

        @Override
        public void onSubscribe(AxSubscription s) {
            onSubscribe = true;
            broadcaster.subscribe(s.link());
        }

        @Override
        public void onNext(String next) {
            onNext = true;
            broadcaster.publish(next);
            latch.countDown();
        }

        @Override
        public void onComplete() {
            onComplete = true;
        }

        @Override
        public void onError(Throwable throwable) {
            onError = true;
        }

    }

    @Test
    public void testRxTest9() throws URISyntaxException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger("test");
        WebSocketClient client = new ReactorNettyWebSocketClient();
        Flux<String> input = Flux.just("test9-ping");
        Flux<String> input2 = Flux.just("test9");

        ReplayProcessor<Object> output = ReplayProcessor.create(1);
        ReplayProcessor<Object> output2 = ReplayProcessor.create(1);


        URI url = new URI("ws://127.0.0.1:" + port + "/test8");
        client.execute(url, session ->
                session
                        .send(input.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        client.execute(url, session ->
                session
                        .send(input2.doOnNext(s -> logger.debug("outbound " + s)).map(session::textMessage))
                        .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output2)
                        .doOnNext(s -> logger.debug("inbound " + s))
                        .then())
                .doOnSuccessOrError((aVoid, ex) -> logger.debug("Done: " + (ex != null ? ex.getMessage() : "success")))
                .block(TIMEOUT);

        RxTest8.latch.await();

        assertEquals(input.mergeWith(input2).collectList().block(TIMEOUT), output.mergeWith(output2).collectList().block(TIMEOUT));

        assertTrue(RxTest9.onSubscribe);
        assertTrue(RxTest9.onNext);
        assertTrue(!RxTest9.onComplete);
        assertTrue(!RxTest9.onError);

    }

}
