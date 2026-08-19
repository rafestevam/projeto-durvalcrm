// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@DisplayName("InactivateCategoryUseCase - Testes unitários da camada de aplicação")
class InactivateCategoryUseCaseTest {

    // -------------------------------------------------------------------------
    // Fake in-memory repository with configurable transaction flag
    // -------------------------------------------------------------------------
    static class FakeCategoryRepository implements CategoryRepositoryPort {

        final List<FinancialCategory> store = new ArrayList<>();
        boolean simulateLinkedTransactions = false;

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
            return simulateLinkedTransactions;
        }
    }

    // -------------------------------------------------------------------------
    // Test setup
    // -------------------------------------------------------------------------
    private FakeCategoryRepository repository;
    private InactivateCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeCategoryRepository();
        useCase = new InactivateCategoryUseCase(repository);
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
            repository.save(category);

            CategoryResponse response = useCase.execute(category.getId());

            assertNotNull(response);
            assertEquals(category.getId(), response.id());
            assertFalse(response.active());
            assertNotNull(response.deletedAt());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve persistir a categoria inativa no repositório após inativação")
        void shouldPersistInactivatedCategory() {
            FinancialCategory category = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            repository.save(category);

            useCase.execute(category.getId());

            FinancialCategory persisted = repository.findById(category.getId()).orElseThrow();
            assertFalse(persisted.isActive());
            assertNotNull(persisted.getDeletedAt());
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
            repository.save(category);

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(category.getId())
            );
            assertNotNull(ex.getMessage());
            assertEquals(
                    String.format("A categoria '%s' já está inativa.", category.getName()),
                    ex.getMessage()
            );
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException quando a categoria possui transações vinculadas")
        void shouldThrowWhenCategoryHasLinkedTransactions() {
            FinancialCategory category = FinancialCategory.create("Conta de Luz", CategoryType.DESPESA);
            repository.save(category);
            repository.simulateLinkedTransactions = true;

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
        }
    }

    @Nested
    @DisplayName("4. Categoria não encontrada")
    class NotFoundTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException quando o ID não existir no repositório")
        void shouldThrowWhenCategoryNotFound() {
            UUID unknownId = UUID.randomUUID();

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(unknownId)
            );
            assertNotNull(ex.getMessage());
            assertEquals(
                    String.format("Categoria com ID '%s' não encontrada.", unknownId),
                    ex.getMessage()
            );
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
