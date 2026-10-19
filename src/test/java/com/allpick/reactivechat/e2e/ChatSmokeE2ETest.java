package com.allpick.reactivechat.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

/**
 * 브라우저 스모크 테스트 — 실제 chat-client.html 을 열어 사용자가 하는 일을 그대로 해 본다.
 *
 * <p>앱은 내장 Kafka 와 함께 임의 포트로 뜬다. 메시지는 실제 경로(STOMP → Kafka → 컨슈머 →
 * /topic/public)를 지나므로, 화면에 보이면 전 구간이 동작한 것이다.
 *
 * <p>개선 의견 폼의 {@code POST /cf/feedback} 은 운영에서 nginx 가 팩토리로 넘기는 경로라
 * 앱에는 없다. 여기서는 브라우저 단에서 가로채 요청 내용만 검사한다 — 실제 티켓을 만들지 않는다.
 *
 * <p>실행: {@code ./gradlew e2eTest} (기본 {@code test} 에서는 빠진다)
 */
@Tag("e2e")
@EmbeddedKafka(partitions = 1, topics = "chat-message")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                // 임의 포트로 뜨므로 운영값(localhost:8080 고정)으로는 SockJS 핸드셰이크가 막힌다
                "app.websocket.allowed-origins=http://localhost:*"
        })
class ChatSmokeE2ETest {

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListeners;

    private BrowserContext alice;
    private BrowserContext bob;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() {
        // 컨슈머가 파티션을 받기 전에 보낸 메시지는 이 테스트의 기다림 안에 안 올 수 있다
        for (MessageListenerContainer container : kafkaListeners.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
        alice = browser.newContext();
        bob = browser.newContext();
    }

    @AfterEach
    void tearDown() {
        alice.close();
        bob.close();
    }

    private Page openChat(BrowserContext context) {
        Page page = context.newPage();
        page.navigate("http://localhost:" + port + "/chat-client.html");
        return page;
    }

    private void join(Page page, String name) {
        page.locator("#username-input").fill(name);
        page.locator("#connect-btn").click();
        assertThat(page.locator("#message-input-area")).isVisible();
        assertThat(page.locator("#connection-status")).hasClass(Pattern.compile("\\bconnected\\b"));
    }

    @Test
    void pageLoadsDisconnected() {
        Page page = openChat(alice);

        assertThat(page.locator("#connection-status")).hasClass(Pattern.compile("\\bdisconnected\\b"));
        assertThat(page.locator("#username-input")).isVisible();
        assertThat(page.locator("#message-input-area")).isHidden();
        assertThat(page.locator("#feedback-open-btn")).isVisible();
    }

    @Test
    void messageReachesOtherUserThroughKafka() {
        Page alicePage = openChat(alice);
        Page bobPage = openChat(bob);
        join(alicePage, "alice");
        join(bobPage, "bob");

        String text = "안녕하세요 smoke " + System.nanoTime();
        alicePage.locator("#message-input").fill(text);
        alicePage.locator("#send-btn").click();

        // 보낸 쪽은 자기 메시지(own), 받는 쪽은 상대 메시지(other)로 그린다
        assertThat(bobPage.locator(".message.other .message-content", new Page.LocatorOptions().setHasText(text)))
                .isVisible();
        assertThat(alicePage.locator(".message.own .message-content", new Page.LocatorOptions().setHasText(text)))
                .isVisible();
        assertThat(bobPage.locator(".message.other .message-sender").last()).hasText("alice");
    }

    @Test
    void messageContentIsEscaped() {
        Page alicePage = openChat(alice);
        Page bobPage = openChat(bob);
        join(alicePage, "alice");
        join(bobPage, "bob");

        String text = "<img src=x onerror=window.__xss=1>" + System.nanoTime();
        alicePage.locator("#message-input").fill(text);
        alicePage.locator("#send-btn").click();

        assertThat(bobPage.locator(".message.other .message-content", new Page.LocatorOptions().setHasText(text)))
                .isVisible();
        assertThat(bobPage.locator(".message-content img")).hasCount(0);
        assertThat(bobPage.evaluate("() => window.__xss === undefined")).isEqualTo(true);
    }

    @Test
    void feedbackFormValidatesAndPostsExpectedBody() throws Exception {
        Page page = openChat(alice);
        AtomicReference<String> posted = new AtomicReference<>();
        page.route("**/cf/feedback", route -> {
            posted.set(route.request().postData());
            route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody("{\"ok\":true,\"ticket_id\":4242}"));
        });

        page.locator("#feedback-open-btn").click();
        Locator overlay = page.locator("#feedback-overlay");
        Locator status = page.locator("#feedback-status");
        assertThat(overlay).hasClass(Pattern.compile("\\bshow\\b"));

        // 빈 내용 → 보내지 않는다
        page.locator("#feedback-submit-btn").click();
        assertThat(status).hasText("내용을 입력해주세요.");

        // 잘못된 이메일 → 보내지 않는다
        page.locator("#feedback-content").fill("다크 모드가 있으면 좋겠어요");
        page.locator("#feedback-email").fill("not-an-email");
        page.locator("#feedback-submit-btn").click();
        assertThat(status).hasText("이메일 형식을 확인해주세요.");
        assertThat(posted.get()).isNull();

        // 올바른 입력 → 팩토리가 기대하는 모양으로 보낸다
        page.locator("#feedback-email").fill("user@example.com");
        page.locator("#feedback-submit-btn").click();
        assertThat(status).containsText("티켓 #4242");

        JsonNode body = new ObjectMapper().readTree(posted.get());
        assertThat(body.get("subject").asText()).startsWith("[chat-demo]");
        assertThat(body.get("content").asText())
                .startsWith("reactive-chat 채팅 데모 웹 페이지에서 접수된 사용자 의견입니다.")
                .endsWith("다크 모드가 있으면 좋겠어요");
        assertThat(body.get("requester").asText()).isEqualTo("user@example.com");
    }
}
