package dev.rpmhub.adapter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.rpmhub.adapter.out.ai.AdministrativeAgent;
import dev.rpmhub.adapter.out.ai.TeacherAgent;
import dev.rpmhub.application.ChatService;
import dev.rpmhub.domain.model.Intention;
import dev.rpmhub.domain.model.RagQuery;
import dev.rpmhub.domain.model.RagResponse;
import dev.rpmhub.domain.port.in.ChatUseCase;
import dev.rpmhub.domain.port.out.EmbeddingRepository;
import dev.rpmhub.domain.port.out.QuestionRouter;
import dev.rpmhub.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Unit tests for {@link ApplicationBeans}.
 *
 * <p>Verifies that the CDI wiring class produces a working {@link ChatUseCase}
 * backed by a plain {@link ChatService}, without booting a CDI container.
 *
 * @author Rodrigo Prestes Machado
 */
@ExtendWith(MockitoExtension.class)
class ApplicationBeansTest {

    /**
     * Driven port mocked to isolate the wiring under test.
     */
    @Mock
    private Repository chatRepository;

    /**
     * Driven port mocked to isolate the wiring under test.
     */
    @Mock
    private EmbeddingRepository embeddingRepository;

    /**
     * Driven port mocked to isolate the wiring under test.
     */
    @Mock
    private TeacherAgent teacherAgent;

    /**
     * Administrative agent mocked so wiring can build the assistant router.
     */
    @Mock
    private AdministrativeAgent administrativeAgent;

    /**
     * Router mocked so the chat use case can classify the test message.
     */
    @Mock
    private QuestionRouter questionRouter;

    /**
     * Wiring class under test.
     */
    private ApplicationBeans applicationBeans;

    /**
     * Wires the beans class with mocked ports before each test.
     */
    @BeforeEach
    void setUp() {
        applicationBeans = new ApplicationBeans();
        applicationBeans.chatRepository = chatRepository;
        applicationBeans.embeddingRepository = embeddingRepository;
        applicationBeans.teacherAgent = teacherAgent;
        applicationBeans.administrativeAgent = administrativeAgent;
        applicationBeans.questionRouter = questionRouter;
    }

    /**
     * Ensures the produced use case is a plain, framework-free {@link ChatService}
     * wired with the injected ports.
     */
    @Test
    void chatUseCase_producesChatServiceWiredWithInjectedPorts() {
        when(questionRouter.classify("oi")).thenReturn(Intention.DISCIPLINE);
        when(chatRepository.findLastByPhone("5511999999999")).thenReturn(Optional.empty());
        when(embeddingRepository.searchChunks(org.mockito.ArgumentMatchers.any(RagQuery.class)))
                .thenReturn(new RagResponse("oi", List.of(), 0.0));
        when(teacherAgent.answer(anyString(), anyString(), anyString()))
                .thenReturn(Multi.createFrom().items("resposta"));

        ChatUseCase chatUseCase = applicationBeans.chatUseCase();

        assertInstanceOf(ChatService.class, chatUseCase);
        List<String> chunks = chatUseCase.chat("5511999999999", "oi").collect().asList().await().indefinitely();
        assertEquals(List.of("resposta"), chunks);
    }

}
