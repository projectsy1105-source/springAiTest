package com.back;

import com.back.ai.chat.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class BackApplicationTests {

    @Autowired
    private ChatService chatService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("ollama chat 클라이언트 테스트")
    void t1() {
        assertDoesNotThrow(()->{
            String response = chatService.streamChatResponse("안녕 지금 모델이뭐야")
                    .collectList()
                    .map(list -> String.join("", list))
                    .block();
            assertNotNull(response);
            assertFalse(response.isEmpty());
            System.out.println("Ollama Response: " + response);
        });
    }

    @Test
    @DisplayName("SSE 스트리밍 테스트")
    void t2(){
        assertDoesNotThrow(()->{
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/ai/chat")
                            .queryParam("message", "Hello, SSE!")
                            .build())
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(String.class)
                    .getResponseBody()
                    .collectList()
                    .map(list -> String.join("", list))
                    .doOnNext(response -> {
                        assertNotNull(response);
                        assertFalse(response.isEmpty());
                        System.out.println("SSE Response: " + response);
                    })
                    .block();
        });
    }

}
