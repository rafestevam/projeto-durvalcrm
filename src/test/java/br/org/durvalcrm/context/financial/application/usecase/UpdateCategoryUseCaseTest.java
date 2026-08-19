// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.application.dto.UpdateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.CategoryAlreadyExistsError;
import br.org.durvalcrm.context.financial.domain.exception.CategoryNotFoundError;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCategoryUseCase - Testes unitários da camada de aplicação")
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepositoryPort repository;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    @Nested
    @DisplayName("1. Construção do use case")
    class ConstructorTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao construir com repositório nulo")
        void shouldThrowWhenRepositoryIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new UpdateCategoryUseCase(null)
            );
            assertEquals("O repositório de categorias é obrigatório.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("2. Renomeação bem-sucedida")
    class SuccessfulUpdateTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve renomear uma categoria existente e retornar response com novo nome")
        void shouldRenameCategorySuccessfully() {
            FinancialCategory category = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            when(repository.findById(category.getId())).thenReturn(Optional.of(category));
            when(repository.findByNameAndType("Cantina e Lanches", CategoryType.RECEITA))
                    .thenReturn(Optional.empty());

            UpdateCategoryCommand command = new UpdateCategoryCommand(category.getId(), "Cantina e Lanches");
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals(category.getId(), response.id());
            assertEquals("Cantina e Lanches", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            assertTrue(response.active());

            ArgumentCaptor<FinancialCategory> categoryCaptor = ArgumentCaptor.forClass(FinancialCategory.class);
            verify(repository, times(1)).save(categoryCaptor.capture());
            assertEquals("Cantina e Lanches", categoryCaptor.getValue().getName());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir renomear quando categoria com mesmo nome existe mas está inativa")
        void shouldAllowRenameWhenDuplicateIsInactive() {
            FinancialCategory inactive = FinancialCategory.create("Novo Nome", CategoryType.RECEITA);
            inactive.inactivate();

            FinancialCategory target = FinancialCategory.create("Nome Antigo", CategoryType.RECEITA);

            when(repository.findById(target.getId())).thenReturn(Optional.of(target));
            when(repository.findByNameAndType("Novo Nome", CategoryType.RECEITA))
                    .thenReturn(Optional.of(inactive));

            UpdateCategoryCommand command = new UpdateCategoryCommand(target.getId(), "Novo Nome");
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals("Novo Nome", response.name());
            verify(repository, times(1)).save(target);
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir renomear para nome igual mas de tipo diferente do existente ativo")
        void shouldAllowRenameWhenExistingActiveHasDifferentType() {
            FinancialCategory target = FinancialCategory.create("Receita Antiga", CategoryType.RECEITA);

            when(repository.findById(target.getId())).thenReturn(Optional.of(target));
            when(repository.findByNameAndType("Geral", CategoryType.RECEITA))
                    .thenReturn(Optional.empty());

            UpdateCategoryCommand command = new UpdateCategoryCommand(target.getId(), "Geral");
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals("Geral", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            verify(repository, times(1)).save(target);
        }
    }

    @Nested
    @DisplayName("3. Validação de duplicidade na renomeação")
    class DuplicateValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar CategoryAlreadyExistsError ao renomear para nome já usado por categoria ativa do mesmo tipo")
        void shouldThrowWhenNewNameConflictsWithActiveCategory() {
            FinancialCategory target = FinancialCategory.create("Antigas Doações", CategoryType.RECEITA);
            FinancialCategory conflicting = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);

            when(repository.findById(target.getId())).thenReturn(Optional.of(target));
            when(repository.findByNameAndType("Doações PIX", CategoryType.RECEITA))
                    .thenReturn(Optional.of(conflicting));

            UpdateCategoryCommand command = new UpdateCategoryCommand(target.getId(), "Doações PIX");

            CategoryAlreadyExistsError ex = assertThrows(
                    CategoryAlreadyExistsError.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("Doações PIX"));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("4. Categoria não encontrada")
    class NotFoundTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar CategoryNotFoundError quando o ID não existir no repositório")
        void shouldThrowWhenCategoryNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(repository.findById(unknownId)).thenReturn(Optional.empty());

            UpdateCategoryCommand command = new UpdateCategoryCommand(unknownId, "Qualquer Nome");

            CategoryNotFoundError ex = assertThrows(
                    CategoryNotFoundError.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains(unknownId.toString()));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("5. Validação do comando")
    class CommandValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao executar com comando nulo")
        void shouldThrowWhenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> useCase.execute(null));
        }
    }
}
