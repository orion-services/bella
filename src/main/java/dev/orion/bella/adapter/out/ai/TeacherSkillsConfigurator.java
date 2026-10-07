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

import dev.langchain4j.service.tool.ToolProvider;
import io.quarkiverse.langchain4j.runtime.skills.SkillsConfigurator;
import io.quarkiverse.langchain4j.skills.runtime.DefaultSkillsConfigurator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Replaces the English skills catalogue with a Portuguese instruction for the teacher agent.
 *
 * <p>The default configurator remains responsible for loading skills and exposing
 * {@code activate_skill}. This bean only changes the system message appended to the prompt.
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class TeacherSkillsConfigurator implements SkillsConfigurator {

    private final DefaultSkillsConfigurator delegate;

    /**
     * Creates the configurator that delegates tool wiring to the extension default.
     *
     * @param delegate built-in configurator that loads skills and builds the tool provider
     */
    @Inject
    public TeacherSkillsConfigurator(DefaultSkillsConfigurator delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToolProvider createToolProvider(List<String> skillNames) {
        return delegate.createToolProvider(skillNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String formatAvailableSkills(List<String> skillNames) {
        return delegate.formatAvailableSkills(skillNames);
    }

    /**
     * Tells the model, in Portuguese, which skills exist and when to activate them.
     *
     * @param skillNames skill names exposed to this AI service
     * @return the catalogue plus the activation instruction
     */
    @Override
    public String buildSkillsSystemMessage(List<String> skillNames) {
        return """
                Você tem acesso às seguintes skills:
                %s
                Ative a skill quando o estudante pedir o resultado sem querer refletir: \
                resolver um exercício prático, marcar a alternativa de uma múltipla escolha, \
                entregar o código ou dizer qual é a resposta. Conduza o estudo com uma ação da skill. \
                Não ative em cumprimento nem em pergunta administrativa do curso (PPC, ementa, \
                carga horária, calendário, notas, faltas, estágio, TCC, atividades complementares). \
                Se essas instruções já estiverem nesta conversa, não ative de novo. \
                Não diga o nome da skill ao estudante.
                """.formatted(formatAvailableSkills(skillNames));
    }
}
