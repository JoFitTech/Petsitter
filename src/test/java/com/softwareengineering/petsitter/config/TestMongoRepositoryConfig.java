package com.softwareengineering.petsitter.config;

import com.softwareengineering.petsitter.chat.repository.ChatConversationRepository;
import com.softwareengineering.petsitter.chat.repository.ChatMessageRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "petsitter.test.mock-mongo-repositories", havingValue = "true")
class TestMongoRepositoryConfig {

    @Bean
    ChatConversationRepository chatConversationRepository() {
        return testRepository(ChatConversationRepository.class);
    }

    @Bean
    ChatMessageRepository chatMessageRepository() {
        return testRepository(ChatMessageRepository.class);
    }

    private <T> T testRepository(Class<T> repositoryType) {
        Object proxy = Proxy.newProxyInstance(
            repositoryType.getClassLoader(),
            new Class<?>[] {repositoryType},
            (instance, method, args) -> {
                if ("equals".equals(method.getName())) {
                    return instance == args[0];
                }
                if ("hashCode".equals(method.getName())) {
                    return System.identityHashCode(instance);
                }
                if ("toString".equals(method.getName())) {
                    return "Test repository proxy for " + repositoryType.getSimpleName();
                }
                if (Optional.class.equals(method.getReturnType())) {
                    return Optional.empty();
                }
                if (List.class.equals(method.getReturnType())) {
                    return List.of();
                }
                if (long.class.equals(method.getReturnType())) {
                    return 0L;
                }
                if (boolean.class.equals(method.getReturnType())) {
                    return false;
                }
                throw new UnsupportedOperationException(
                    repositoryType.getSimpleName() + "." + method.getName()
                        + " is not available in Mongo-free context tests"
                );
            }
        );
        return repositoryType.cast(proxy);
    }
}
