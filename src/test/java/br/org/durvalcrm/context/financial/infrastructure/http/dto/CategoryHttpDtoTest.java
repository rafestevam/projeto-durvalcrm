// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import br.org.durvalcrm.context.financial.application.dto.CreateCategoryCommand;
import br.org.durvalcrm.context.financial.application.dto.UpdateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayName("DTOs HTTP - Testes unitários de CreateCategoryRequest e UpdateCategoryRequest")
class CategoryHttpDtoTest {

    // ────────────────────────────────────────────────────────────────────────
    // CreateCategoryRequest
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CreateCategoryRequest")
    class CreateCategoryRequestTests {

        @Test
        @Timeout(30)
        @DisplayName("toCommand() deve mapear name e type corretamente para RECEITA")
        void shouldMapToCommandForReceita() {
            CreateCategoryRequest request = new CreateCategoryRequest("Doações PIX", CategoryType.RECEITA);

            CreateCategoryCommand command = request.toCommand();

            assertEquals("Doações PIX", command.name());
            assertEquals(CategoryType.RECEITA, command.type());
        }

        @Test
        @Timeout(30)
        @DisplayName("toCommand() deve mapear name e type corretamente para DESPESA")
        void shouldMapToCommandForDespesa() {
            CreateCategoryRequest request = new CreateCategoryRequest("Conta de Luz", CategoryType.DESPESA);

            CreateCategoryCommand command = request.toCommand();

            assertEquals("Conta de Luz", command.name());
            assertEquals(CategoryType.DESPESA, command.type());
        }

        @Test
        @Timeout(30)
        @DisplayName("Dois CreateCategoryRequest com mesmos valores devem ser iguais (record equality)")
        void shouldHaveValueEquality() {
            CreateCategoryRequest a = new CreateCategoryRequest("X", CategoryType.RECEITA);
            CreateCategoryRequest b = new CreateCategoryRequest("X", CategoryType.RECEITA);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @Timeout(30)
        @DisplayName("name() e type() devem retornar os valores passados no construtor")
        void shouldExposeAccessors() {
            CreateCategoryRequest request = new CreateCategoryRequest("Livraria", CategoryType.RECEITA);

            assertEquals("Livraria", request.name());
            assertEquals(CategoryType.RECEITA, request.type());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // UpdateCategoryRequest
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UpdateCategoryRequest")
    class UpdateCategoryRequestTests {

        private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

        @Test
        @Timeout(30)
        @DisplayName("toCommand(id) deve mapear o UUID e o novo nome corretamente")
        void shouldMapToCommandWithIdAndName() {
            UpdateCategoryRequest request = new UpdateCategoryRequest("Novo Nome");

            UpdateCategoryCommand command = request.toCommand(ID);

            assertEquals(ID, command.id());
            assertEquals("Novo Nome", command.newName());
        }

        @Test
        @Timeout(30)
        @DisplayName("toCommand() com IDs diferentes deve produzir commands diferentes")
        void shouldProduceDifferentCommandsForDifferentIds() {
            UUID otherId = UUID.randomUUID();
            UpdateCategoryRequest request = new UpdateCategoryRequest("Nome");

            UpdateCategoryCommand cmd1 = request.toCommand(ID);
            UpdateCategoryCommand cmd2 = request.toCommand(otherId);

            assertNotEquals(cmd1.id(), cmd2.id());
            assertEquals(cmd1.newName(), cmd2.newName());
        }

        @Test
        @Timeout(30)
        @DisplayName("Dois UpdateCategoryRequest com mesmo nome devem ser iguais (record equality)")
        void shouldHaveValueEquality() {
            UpdateCategoryRequest a = new UpdateCategoryRequest("Nome A");
            UpdateCategoryRequest b = new UpdateCategoryRequest("Nome A");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @Timeout(30)
        @DisplayName("name() deve retornar o valor passado no construtor")
        void shouldExposeNameAccessor() {
            UpdateCategoryRequest request = new UpdateCategoryRequest("Cantina");

            assertEquals("Cantina", request.name());
        }
    }
}
