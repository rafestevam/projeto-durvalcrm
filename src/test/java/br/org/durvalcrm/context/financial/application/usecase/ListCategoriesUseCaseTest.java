// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.org.durvalcrm.context.financial.application.dto.CategoryFilter;
import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCategoriesUseCase - Testes unitários da camada de aplicação")
class ListCategoriesUseCaseTest {

    @Mock
    private CategoryRepositoryPort repository;

    @InjectMocks
    private ListCategoriesUseCase useCase;

    @Nested
    @DisplayName("1. Construção do use case")
    class ConstructorTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao construir com repositório nulo")
        void shouldThrowWhenRepositoryIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new ListCategoriesUseCase(null)
            );
            assertEquals("O repositório de categorias é obrigatório.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("2. Listagem sem filtro de status")
    class ListAllTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar lista vazia quando não há categorias cadastradas")
        void shouldReturnEmptyListWhenNoCategories() {
            when(repository.findAll(null)).thenReturn(List.of());

            List<CategoryResponse> result = useCase.execute(CategoryFilter.all());
            assertTrue(result.isEmpty());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar todas as categorias (ativas e inativas) com filtro all()")
        void shouldReturnAllCategoriesIncludingInactive() {
            FinancialCategory ativa = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);
            FinancialCategory inativa = FinancialCategory.create("Conta de Luz", CategoryType.DESPESA);
            inativa.inactivate();

            when(repository.findAll(null)).thenReturn(List.of(ativa, inativa));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.all());

            assertEquals(2, result.size());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar apenas categorias ativas com filtro activeOnly()")
        void shouldReturnOnlyActiveCategories() {
            FinancialCategory ativa = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);
            FinancialCategory inativa = FinancialCategory.create("Conta de Luz", CategoryType.DESPESA);
            inativa.inactivate();

            when(repository.findAll(null)).thenReturn(List.of(ativa, inativa));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.activeOnly());

            assertEquals(1, result.size());
            assertEquals("Doações PIX", result.get(0).name());
            assertTrue(result.get(0).active());
        }
    }

    @Nested
    @DisplayName("3. Listagem filtrada por tipo")
    class ListByTypeTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar apenas categorias do tipo RECEITA")
        void shouldReturnOnlyReceitaCategories() {
            FinancialCategory active1 = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);
            FinancialCategory active2 = FinancialCategory.create("Venda de Livros", CategoryType.RECEITA);

            when(repository.findAll(CategoryType.RECEITA)).thenReturn(List.of(active1, active2));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.byType(CategoryType.RECEITA));

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.type() == CategoryType.RECEITA));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar apenas categorias do tipo DESPESA")
        void shouldReturnOnlyDespesaCategories() {
            FinancialCategory active1 = FinancialCategory.create("Conta de Luz", CategoryType.DESPESA);
            FinancialCategory active2 = FinancialCategory.create("Conta de Água", CategoryType.DESPESA);

            when(repository.findAll(CategoryType.DESPESA)).thenReturn(List.of(active1, active2));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.byType(CategoryType.DESPESA));

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.type() == CategoryType.DESPESA));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar lista vazia quando não existem categorias do tipo filtrado")
        void shouldReturnEmptyWhenNoMatchingType() {
            when(repository.findAll(CategoryType.DESPESA)).thenReturn(List.of());

            List<CategoryResponse> result = useCase.execute(CategoryFilter.byType(CategoryType.DESPESA));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("4. Mapeamento para CategoryResponse")
    class MappingTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve mapear corretamente todos os campos da entidade para CategoryResponse")
        void shouldMapAllFieldsCorrectly() {
            FinancialCategory category = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            when(repository.findAll(null)).thenReturn(List.of(category));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.all());

            assertEquals(1, result.size());
            CategoryResponse response = result.get(0);
            assertEquals(category.getId(), response.id());
            assertEquals("Cantina", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            assertTrue(response.active());
            assertEquals(category.getCreatedAt(), response.createdAt());
            assertEquals(category.getUpdatedAt(), response.updatedAt());
        }
    }

    @Nested
    @DisplayName("5. Validação do filtro")
    class FilterValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar NullPointerException ao executar com filtro nulo")
        void shouldThrowWhenFilterIsNull() {
            assertThrows(NullPointerException.class, () -> useCase.execute(null));
        }
    }
}
