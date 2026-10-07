package dev.orion.bella.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.orion.bella.domain.model.AgentKind;
import dev.orion.bella.domain.model.AgentMessage;
import dev.orion.bella.domain.model.Chat;
import dev.orion.bella.domain.model.User;
import dev.orion.bella.domain.model.UserMessage;
import dev.orion.bella.domain.port.in.RoutedAnswer;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Unit tests for marking an agent reply as copied.
 *
 * @author Rodrigo Prestes Machado
 */
class ConversationServiceTest {

    private FakeRepository repository;
    private ConversationService service;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        service = new ConversationService(repository, new UnusedRouter());
    }

    /**
     * Ensures copying an agent reply sets the flag and keeps the reply text and agent.
     */
    @Test
    void markAgentMessageCopied_setsFlagAndKeepsReply() {
        Chat chat = ownedChat();

        service.markAgentMessageCopied(chat.getId(), "hash", 1);
        service.markAgentMessageCopied(chat.getId(), "hash", 1);

        Chat stored = repository.findConversationById(chat.getId()).orElseThrow();
        assertEquals("pergunta", stored.getMessages().get(0).getMessage());
        AgentMessage reply = (AgentMessage) stored.getMessages().get(1);
        assertTrue(reply.isCopied());
        assertEquals("resposta", reply.getMessage());
        assertEquals(AgentKind.ADMINISTRATIVE, reply.getAgent());
    }

    /**
     * Ensures a user message cannot be marked as copied.
     */
    @Test
    void markAgentMessageCopied_rejectsUserMessage() {
        Chat chat = ownedChat();

        assertThrows(IllegalArgumentException.class,
                () -> service.markAgentMessageCopied(chat.getId(), "hash", 0));
        assertTrue(repository.findConversationById(chat.getId()).orElseThrow()
                .getAgentMessages().stream().noneMatch(AgentMessage::isCopied));
    }

    /**
     * Ensures a missing sequence is rejected.
     */
    @Test
    void markAgentMessageCopied_rejectsUnknownSequence() {
        Chat chat = ownedChat();

        assertThrows(java.util.NoSuchElementException.class,
                () -> service.markAgentMessageCopied(chat.getId(), "hash", 5));
    }

    /**
     * Ensures another user cannot mark a reply in a conversation they do not own.
     */
    @Test
    void markAgentMessageCopied_rejectsOtherOwner() {
        Chat chat = ownedChat();

        assertThrows(SecurityException.class,
                () -> service.markAgentMessageCopied(chat.getId(), "outro", 1));
    }

    private Chat ownedChat() {
        User user = new User();
        user.setOrionUserHash("hash");
        Chat chat = Chat.start(user);

        UserMessage question = new UserMessage();
        question.setUser(user);
        question.setMessage("pergunta");
        question.setTimestamp(new Date());
        chat.addMessage(question);

        AgentMessage reply = new AgentMessage();
        reply.setMessage("resposta");
        reply.setAgent(AgentKind.ADMINISTRATIVE);
        reply.setTimestamp(new Date());
        chat.addMessage(reply);

        repository.save(chat);
        return chat;
    }

    private static final class UnusedRouter implements RouterUseCase {

        @Override
        public RoutedAnswer answer(String memoryId, String prompt) {
            return new RoutedAnswer(AgentKind.DISCIPLINE, Multi.createFrom().empty());
        }
    }

    private static final class FakeRepository implements Repository {

        private final ConcurrentMap<String, Chat> chatsById = new ConcurrentHashMap<>();

        @Override
        public Optional<Chat> findLastByPhone(String phoneNumber) {
            return Optional.empty();
        }

        @Override
        public void save(Chat chat) {
            chatsById.put(chat.getId(), chat);
        }

        @Override
        public Optional<Chat> findConversationById(String id) {
            return Optional.ofNullable(chatsById.get(id));
        }

        @Override
        public List<Chat> findAllByOrionUserHash(String orionUserHash) {
            return List.of();
        }

        @Override
        public void deleteConversation(String id) {
            chatsById.remove(id);
        }
    }
}
