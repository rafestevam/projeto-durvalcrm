// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

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
import br.org.durvalcrm.context.financial.application.dto.CreateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.CategoryAlreadyExistsError;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCategoryUseCase - Testes unitários da camada de aplicação")
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepositoryPort repository;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    @Nested
    @DisplayName("1. Construção do use case")
    class ConstructorTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao construir com repositório nulo")
        void shouldThrowWhenRepositoryIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new CreateCategoryUseCase(null)
            );
            assertEquals("O repositório de categorias é obrigatório.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("2. Criação bem-sucedida")
    class SuccessfulCreationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve criar uma categoria RECEITA e retornar response com dados corretos")
        void shouldCreateReceitaCategorySuccessfully() {
            CreateCategoryCommand command = new CreateCategoryCommand("Doações PIX", CategoryType.RECEITA);
            when(repository.findByNameAndType("Doações PIX", CategoryType.RECEITA))
                    .thenReturn(Optional.empty());

            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertNotNull(response.id());
            assertEquals("Doações PIX", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            assertTrue(response.active());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());

            ArgumentCaptor<FinancialCategory> categoryCaptor = ArgumentCaptor.forClass(FinancialCategory.class);
            verify(repository, times(1)).save(categoryCaptor.capture());
            FinancialCategory savedCategory = categoryCaptor.getValue();
            assertEquals("Doações PIX", savedCategory.getName());
            assertEquals(CategoryType.RECEITA, savedCategory.getType());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve criar uma categoria DESPESA com sucesso")
        void shouldCreateDespesaCategorySuccessfully() {
            CreateCategoryCommand command = new CreateCategoryCommand("Conta de Luz", CategoryType.DESPESA);
            when(repository.findByNameAndType("Conta de Luz", CategoryType.DESPESA))
                    .thenReturn(Optional.empty());

            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals(CategoryType.DESPESA, response.type());
            assertEquals("Conta de Luz", response.name());

            verify(repository, times(1)).save(any(FinancialCategory.class));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir criar categoria com nome igual ao de uma categoria inativa do mesmo tipo")
        void shouldAllowDuplicateNameWhenExistingIsInactive() {
            FinancialCategory inactive = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            inactive.inactivate();

            when(repository.findByNameAndType("Cantina", CategoryType.RECEITA))
                    .thenReturn(Optional.of(inactive));

            CreateCategoryCommand command = new CreateCategoryCommand("Cantina", CategoryType.RECEITA);
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals("Cantina", response.name());
            verify(repository, times(1)).save(any(FinancialCategory.class));
        }
    }

    @Nested
    @DisplayName("3. Validação de duplicidade")
    class DuplicateValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar CategoryAlreadyExistsError ao criar categoria com nome e tipo duplicados e ativos")
        void shouldThrowWhenActiveDuplicateExists() {
            FinancialCategory active = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);

            when(repository.findByNameAndType("Doações PIX", CategoryType.RECEITA))
                    .thenReturn(Optional.of(active));

            CreateCategoryCommand command = new CreateCategoryCommand("Doações PIX", CategoryType.RECEITA);

            CategoryAlreadyExistsError ex = assertThrows(
                    CategoryAlreadyExistsError.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("Doações PIX"));
            assertTrue(ex.getMessage().contains("RECEITA"));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("4. Validação do comando")
    class CommandValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao executar com comando nulo")
        void shouldThrowWhenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> useCase.execute(null));
        }
    }
}
