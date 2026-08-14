package com.hudi.springai.more.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("client")
public class MorPlatformAndModelController {

    private final HashMap<String, ChatModel> platforms = new HashMap<>();

    private Map<String, ChatClient> chatClientMap;

    public MorPlatformAndModelController(
            DashScopeChatModel dashScopeChatModel,
            DeepSeekChatModel deepSeekChatModel,
            OllamaChatModel ollamaChatModel,
            ObjectProvider<AnthropicChatModel> anthropicChatModelProvider,
            @Value("${spring.ai.anthropic.api-key:}") String anthropicApiKey
    ) {
        platforms.put("dashscope", dashScopeChatModel);
        platforms.put("ollama", ollamaChatModel);
        platforms.put("deepseek", deepSeekChatModel);

        // 可选启用：仅当配置了非空 api-key 且存在 AnthropicChatModel Bean 时注册
        if (StringUtils.hasText(anthropicApiKey)) {
            AnthropicChatModel anthropicChatModel = anthropicChatModelProvider.getIfAvailable();
            if (anthropicChatModel != null) {
                platforms.put("anthropic", anthropicChatModel);
            }
        }
    }

    @Value("classpath:files/prompt.st")
    private Resource resource;

    @Autowired
    private ChatMemory chatMemory;

    @RequestMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public String chat(
            @RequestParam(defaultValue = "你好，你是谁") String message,
            @RequestParam(defaultValue = "dashscope") String platform,
            @RequestParam(defaultValue = "1") String id) {
        ChatClient chatClient = this.initChatClient(platform);
        return chatClient.prompt()
                .advisors(i -> i.param(ChatMemory.CONVERSATION_ID, id))
                .user(message).call().content();
    }

    @RequestMapping(value = "/stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> stream(
            @RequestParam(defaultValue = "你好，你是谁") String message,
            @RequestParam(defaultValue = "dashscope") String platform,
            @RequestParam(defaultValue = "1") String id) {
        ChatClient chatClient = this.initChatClient(platform);
        return chatClient.prompt()
                .advisors(i -> i.param(ChatMemory.CONVERSATION_ID, id))
                .user(message).stream().content();
    }

    public ChatClient initChatClient(String platform) {
        if (chatClientMap == null || chatClientMap.get(platform) == null) {
            if (chatClientMap == null) {
                chatClientMap = new HashMap<>();
            }
            ChatModel chatModel = platforms.get(platform);
            if (chatModel == null) {
                throw new RuntimeException(
                        "未找到对应的平台: " + platform
                                + "。可用平台: " + platforms.keySet()
                                + "。若需要 anthropic，请配置环境变量 ai_key_anthropic（及兼容网关 ai_base_anthropic）。");
            }
            ChatClient.Builder builder = ChatClient.builder(chatModel);
            ChatOptions options = ChatOptions.builder().build();
            builder.defaultSystem(resource);
            builder.defaultOptions(options);
            builder.defaultAdvisors(List.of(
                    new SimpleLoggerAdvisor(),
                    new SafeGuardAdvisor(List.of("政治")),
                    PromptChatMemoryAdvisor.builder(chatMemory).build()
            ));
            ChatClient chatClient = builder.build();
            chatClientMap.put(platform, chatClient);
            return chatClient;
        } else {
            return chatClientMap.get(platform);
        }
    }

}
