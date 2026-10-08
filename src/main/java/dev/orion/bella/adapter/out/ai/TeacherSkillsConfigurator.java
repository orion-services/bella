/*
 * Copyright 2026 Rodrigo Prestes Machado
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.orion.bella.adapter.out.ai;

import java.util.List;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import dev.orion.bella.domain.port.out.SkillActivation;
import io.quarkiverse.langchain4j.runtime.skills.SkillsConfigurator;
import io.quarkiverse.langchain4j.skills.runtime.DefaultSkillsConfigurator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Replaces the English skills catalogue with a Portuguese instruction for the teacher agent.
 *
 * <p>The default configurator remains responsible for loading skills and exposing
 * {@code activate_skill}. This bean changes the system message appended to the prompt
 * and records, through {@link SkillActivation}, when that tool runs.</p>
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class TeacherSkillsConfigurator implements SkillsConfigurator {

    /** Tool the model calls to load a skill into the conversation. */
    private static final String ACTIVATE_SKILL = "activate_skill";

    private final DefaultSkillsConfigurator delegate;

    private final SkillActivation skillActivation;

    /**
     * Creates the configurator that delegates tool wiring to the extension default.
     *
     * @param delegate        built-in configurator that loads skills and builds the tool provider
     * @param skillActivation port that remembers a skill activation until the reply is saved
     */
    @Inject
    public TeacherSkillsConfigurator(DefaultSkillsConfigurator delegate, SkillActivation skillActivation) {
        this.delegate = delegate;
        this.skillActivation = skillActivation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToolProvider createToolProvider(List<String> skillNames) {
        ToolProvider delegateProvider = delegate.createToolProvider(skillNames);
        return new ToolProvider() {
            @Override
            public ToolProviderResult provideTools(ToolProviderRequest request) {
                return markingActivateSkill(delegateProvider.provideTools(request), request.chatMemoryId());
            }

            @Override
            public boolean isDynamic() {
                return delegateProvider.isDynamic();
            }
        };
    }

    /**
     * Wraps {@code activate_skill} so a successful call marks the conversation.
     *
     * @param result     tools exposed by the default configurator
     * @param memoryId   chat id of the turn, used as the assistant memory id
     * @return the same tools, with the activation tool recording the conversation
     */
    private ToolProviderResult markingActivateSkill(ToolProviderResult result, Object memoryId) {
        String conversationId = memoryId == null ? null : memoryId.toString();
        ToolProviderResult.Builder builder = ToolProviderResult.builder();
        result.aiServiceTools().forEach(tool -> {
            if (ACTIVATE_SKILL.equals(tool.name()) && conversationId != null) {
                builder.add(tool.toBuilder()
                        .toolExecutor(markingExecutor(tool.toolExecutor(), conversationId))
                        .build());
            } else {
                builder.add(tool);
            }
        });
        if (result.immediateReturnToolNames() != null && !result.immediateReturnToolNames().isEmpty()) {
            builder.immediateReturnToolNames(result.immediateReturnToolNames());
        }
        return builder.build();
    }

    /**
     * Marks the conversation and then runs the original skill executor.
     *
     * @param original       executor that loads the skill instructions
     * @param conversationId chat id to mark
     * @return an executor that records the activation before delegating
     */
    private ToolExecutor markingExecutor(ToolExecutor original, String conversationId) {
        return new ToolExecutor() {
            @Override
            public String execute(ToolExecutionRequest request, Object memoryId) {
                skillActivation.mark(conversationId);
                return original.execute(request, memoryId);
            }

            @Override
            public ToolExecutionResult executeWithContext(ToolExecutionRequest request, InvocationContext context) {
                skillActivation.mark(conversationId);
                return original.executeWithContext(request, context);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String formatAvailableSkills(List<String> skillNames) {
        return delegate.formatAvailableSkills(skillNames);
    }

    /**
     * Tells the model, in Portuguese, which skills exist, when to activate them,
     * and to follow the returned instructions over the usual reply format.
     *
     * @param skillNames skill names exposed to this AI service
     * @return the catalogue plus the activation instruction
     */
    @Override
    public String buildSkillsSystemMessage(List<String> skillNames) {
        return """
                Você tem acesso às seguintes skills:
                %s
                Ative a skill quando o estudante pedir o resultado sem querer estudar: \
                resolver um exercício prático, indicar a alternativa de uma múltipla escolha, \
                entregar o código ou dizer qual é a resposta. Siga somente o que a skill devolver. \
                Nesse turno, essas instruções valem mais do que o formato usual da resposta. \
                Não ative em cumprimento nem em pergunta administrativa do curso (PPC, ementa, \
                carga horária, calendário, notas, faltas, estágio, TCC, atividades complementares). \
                Se essas instruções já estiverem nesta conversa, não ative de novo. \
                Não diga o nome da skill ao estudante.
                """.formatted(formatAvailableSkills(skillNames));
    }
}
