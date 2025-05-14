package com.ollama.spring.service;

import reactor.core.publisher.Flux;

public interface ChatAiService {
    public String askToLlmAI(String question);
    public Flux<String> askToLlmAIWithStream(String question);
}
