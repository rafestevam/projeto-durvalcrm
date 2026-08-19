// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.application.dto.CreateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@DisplayName("CreateCategoryUseCase - Testes unitários da camada de aplicação")
class CreateCategoryUseCaseTest {

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
    private CreateCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeCategoryRepository();
        useCase = new CreateCategoryUseCase(repository);
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

            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertNotNull(response.id());
            assertEquals("Doações PIX", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            assertTrue(response.active());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());
            assertEquals(1, repository.store.size());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve criar uma categoria DESPESA com sucesso")
        void shouldCreateDespesaCategorySuccessfully() {
            CreateCategoryCommand command = new CreateCategoryCommand("Conta de Luz", CategoryType.DESPESA);

            CategoryResponse response = useCase.execute(command);

            assertEquals(CategoryType.DESPESA, response.type());
            assertEquals("Conta de Luz", response.name());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir criar categorias com nomes iguais mas tipos diferentes")
        void shouldAllowSameNameWithDifferentTypes() {
            repository.save(FinancialCategory.create("Geral", CategoryType.RECEITA));

            CreateCategoryCommand command = new CreateCategoryCommand("Geral", CategoryType.DESPESA);
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals("Geral", response.name());
            assertEquals(CategoryType.DESPESA, response.type());
            assertEquals(2, repository.store.size());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir criar categoria com nome igual ao de uma categoria inativa do mesmo tipo")
        void shouldAllowDuplicateNameWhenExistingIsInactive() {
            FinancialCategory inativa = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            inativa.inactivate();
            repository.save(inativa);

            CreateCategoryCommand command = new CreateCategoryCommand("Cantina", CategoryType.RECEITA);
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals("Cantina", response.name());
        }
    }

    @Nested
    @DisplayName("3. Validação de duplicidade")
    class DuplicateValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException ao criar categoria com nome e tipo duplicados e ativos")
        void shouldThrowWhenActiveDuplicateExists() {
            repository.save(FinancialCategory.create("Doações PIX", CategoryType.RECEITA));

            CreateCategoryCommand command = new CreateCategoryCommand("Doações PIX", CategoryType.RECEITA);

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("Doações PIX"));
            assertTrue(ex.getMessage().contains("RECEITA"));
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
