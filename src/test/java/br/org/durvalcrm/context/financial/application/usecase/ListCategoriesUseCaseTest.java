// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import br.org.durvalcrm.context.financial.application.dto.CategoryFilter;
import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@DisplayName("ListCategoriesUseCase - Testes unitários da camada de aplicação")
class ListCategoriesUseCaseTest {

    // -------------------------------------------------------------------------
    // Fake in-memory repository
    // -------------------------------------------------------------------------
    static class FakeCategoryRepository implements CategoryRepositoryPort {

        final List<FinancialCategory> store = new ArrayList<>();

        @Override
        public void save(FinancialCategory category) {
            store.removeIf(c -> c.getId().equals(category.getId()));
            store.add(category);
        }

        @Override
        public Optional<FinancialCategory> findById(UUID id) {
            return store.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<FinancialCategory> findByNameAndType(String name, CategoryType type) {
            return store.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name) && c.getType() == type)
                    .findFirst();
        }

        @Override
        public List<FinancialCategory> findAll(CategoryType type) {
            if (type == null) return List.copyOf(store);
            return store.stream().filter(c -> c.getType() == type).toList();
        }

        @Override
        public boolean hasLinkedTransactions(UUID categoryId) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Test setup
    // -------------------------------------------------------------------------
    private FakeCategoryRepository repository;
    private ListCategoriesUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeCategoryRepository();
        useCase = new ListCategoriesUseCase(repository);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

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
            repository.save(ativa);
            repository.save(inativa);

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
            repository.save(ativa);
            repository.save(inativa);

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
            repository.save(FinancialCategory.create("Doações PIX", CategoryType.RECEITA));
            repository.save(FinancialCategory.create("Aluguel", CategoryType.DESPESA));
            repository.save(FinancialCategory.create("Venda de Livros", CategoryType.RECEITA));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.byType(CategoryType.RECEITA));

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.type() == CategoryType.RECEITA));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar apenas categorias do tipo DESPESA")
        void shouldReturnOnlyDespesaCategories() {
            repository.save(FinancialCategory.create("Doações PIX", CategoryType.RECEITA));
            repository.save(FinancialCategory.create("Conta de Luz", CategoryType.DESPESA));
            repository.save(FinancialCategory.create("Conta de Água", CategoryType.DESPESA));

            List<CategoryResponse> result = useCase.execute(CategoryFilter.byType(CategoryType.DESPESA));

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.type() == CategoryType.DESPESA));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar lista vazia quando não existem categorias do tipo filtrado")
        void shouldReturnEmptyWhenNoMatchingType() {
            repository.save(FinancialCategory.create("Doações PIX", CategoryType.RECEITA));

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
            repository.save(category);

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
