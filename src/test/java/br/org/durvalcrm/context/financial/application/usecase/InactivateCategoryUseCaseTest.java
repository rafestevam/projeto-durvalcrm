// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.CategoryNotFoundError;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("InactivateCategoryUseCase - Testes unitários da camada de aplicação")
class InactivateCategoryUseCaseTest {

    @Mock
    private CategoryRepositoryPort repository;

    @InjectMocks
    private InactivateCategoryUseCase useCase;

    @Nested
    @DisplayName("1. Construção do use case")
    class ConstructorTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao construir com repositório nulo")
        void shouldThrowWhenRepositoryIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new InactivateCategoryUseCase(null)
            );
            assertEquals("O repositório de categorias é obrigatório.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("2. Inativação bem-sucedida")
    class SuccessfulInactivationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve inativar uma categoria ativa sem transações vinculadas")
        void shouldInactivateActiveCategorySuccessfully() {
            FinancialCategory category = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);
            when(repository.findById(category.getId())).thenReturn(Optional.of(category));
            when(repository.hasLinkedTransactions(category.getId())).thenReturn(false);

            CategoryResponse response = useCase.execute(category.getId());

            assertNotNull(response);
            assertEquals(category.getId(), response.id());
            assertFalse(response.active());
            assertNotNull(response.deletedAt());
            verify(repository, times(1)).save(category);
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve persistir a categoria inativa no repositório após inativação")
        void shouldPersistInactivatedCategory() {
            FinancialCategory category = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            when(repository.findById(category.getId())).thenReturn(Optional.of(category));
            when(repository.hasLinkedTransactions(category.getId())).thenReturn(false);

            useCase.execute(category.getId());

            assertFalse(category.isActive());
            assertNotNull(category.getDeletedAt());
            verify(repository, times(1)).save(category);
        }
    }

    @Nested
    @DisplayName("3. Regras de negócio que impedem a inativação")
    class BusinessRuleViolationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException ao tentar inativar categoria já inativa")
        void shouldThrowWhenCategoryAlreadyInactive() {
            FinancialCategory category = FinancialCategory.create("Livraria", CategoryType.RECEITA);
            category.inactivate();

            when(repository.findById(category.getId())).thenReturn(Optional.of(category));

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(category.getId())
            );
            assertNotNull(ex.getMessage());
            assertEquals(
                    String.format("A categoria '%s' já está inativa.", category.getName()),
                    ex.getMessage()
            );
            verify(repository, never()).save(any());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException quando a categoria possui transações vinculadas")
        void shouldThrowWhenCategoryHasLinkedTransactions() {
            FinancialCategory category = FinancialCategory.create("Conta de Luz", CategoryType.DESPESA);

            when(repository.findById(category.getId())).thenReturn(Optional.of(category));
            when(repository.hasLinkedTransactions(category.getId())).thenReturn(true);

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(category.getId())
            );
            assertNotNull(ex.getMessage());
            assertEquals(
                    String.format(
                            "A categoria '%s' não pode ser inativada pois possui transações financeiras vinculadas.",
                            category.getName()
                    ),
                    ex.getMessage()
            );
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

            CategoryNotFoundError ex = assertThrows(
                    CategoryNotFoundError.class,
                    () -> useCase.execute(unknownId)
            );
            assertNotNull(ex.getMessage());
            assertEquals(
                    String.format("Categoria com ID '%s' não encontrada.", unknownId),
                    ex.getMessage()
            );
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("5. Validação do parâmetro de entrada")
    class InputValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao executar com ID nulo")
        void shouldThrowWhenCategoryIdIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> useCase.execute(null)
            );
            assertEquals("O ID da categoria não pode ser nulo.", ex.getMessage());
        }
    }
}
