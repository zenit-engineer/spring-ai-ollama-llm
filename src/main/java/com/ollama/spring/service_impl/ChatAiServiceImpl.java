package com.ollama.spring.service_impl;

import com.ollama.spring.service.ChatAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatAiServiceImpl implements ChatAiService {

    private final ChatClient chatClient;

    public ChatAiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String askToLlmAI(String question){
        return chatClient.prompt(question)
                .system("""
                    You are an expert chef and must only respond to recipe-related questions.
                    Only respond with final recipe ideas or polite food-related messages.
                    """)
                .call()
                .content();
    }

    public Flux<String> askToLlmAIWithStream(String question) {
        if (!isFoodRelated(question)) {
            return Flux.just("Sorry, I can only help with cooking and recipes.");
        }

        return chatClient.prompt(question)
                .system("""
                        You are an expert chef and must only respond to recipe-related questions.
                        Only respond with final recipe ideas or polite food-related messages.
                    """)
                .stream()
                .content()
//                .map(this::removeThinkingTags)         // Strip <think>...</think>
                .filter(this::isValidRecipeLine)       // Keep only valid recipe lines
                .switchIfEmpty(
                        Flux.just("Sorry, only recipe-related answers are allowed.")
                );
    }

    private boolean isFoodRelated(String input) {
        String lower = input.toLowerCase();
        return lower.contains("cook") || lower.contains("recipe") || lower.contains("ingredient")
                || lower.contains("meal") || lower.contains("food") || lower.contains("dish");
    }

    /*Contain only uppercase or lowercase letters (A-Za-z)
    Include spaces (\\s)
    May include hyphens (-)
    May include apostrophes (')*/
    private boolean isValidRecipeLine(String line) {
        // Very simple filter; customize for more precision
        return line.matches("[A-Za-z\\s\\-']+");
    }

    private String removeThinkingTags(String input) {
        return input.replaceAll("(?s)<think>.*?</think>", "").trim();
    }

}
