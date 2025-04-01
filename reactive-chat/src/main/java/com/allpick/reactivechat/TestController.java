package com.allpick.reactivechat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/api/health")
    public Mono<String> health() {
        return Mono.just("Server is up and running!");
    }
}
