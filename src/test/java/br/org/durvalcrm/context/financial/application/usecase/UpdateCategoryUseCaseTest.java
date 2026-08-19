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
import br.org.durvalcrm.context.financial.application.dto.UpdateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

@DisplayName("UpdateCategoryUseCase - Testes unitários da camada de aplicação")
class UpdateCategoryUseCaseTest {

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
    private UpdateCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeCategoryRepository();
        useCase = new UpdateCategoryUseCase(repository);
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
            repository.save(category);

            UpdateCategoryCommand command = new UpdateCategoryCommand(category.getId(), "Cantina e Lanches");
            CategoryResponse response = useCase.execute(command);

            assertNotNull(response);
            assertEquals(category.getId(), response.id());
            assertEquals("Cantina e Lanches", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
            assertTrue(response.active());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve persistir a categoria renomeada no repositório")
        void shouldPersistRenamedCategory() {
            FinancialCategory category = FinancialCategory.create("Livraria", CategoryType.RECEITA);
            repository.save(category);

            UpdateCategoryCommand command = new UpdateCategoryCommand(category.getId(), "Livraria Espírita");
            useCase.execute(command);

            FinancialCategory persisted = repository.findById(category.getId()).orElseThrow();
            assertEquals("Livraria Espírita", persisted.getName());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir renomear quando categoria com mesmo nome existe mas está inativa")
        void shouldAllowRenameWhenDuplicateIsInactive() {
            FinancialCategory inativa = FinancialCategory.create("Novo Nome", CategoryType.RECEITA);
            inativa.inactivate();
            repository.save(inativa);

            FinancialCategory alvo = FinancialCategory.create("Nome Antigo", CategoryType.RECEITA);
            repository.save(alvo);

            UpdateCategoryCommand command = new UpdateCategoryCommand(alvo.getId(), "Novo Nome");
            CategoryResponse response = useCase.execute(command);

            assertEquals("Novo Nome", response.name());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve permitir renomear para nome igual mas de tipo diferente do existente ativo")
        void shouldAllowRenameWhenExistingActiveHasDifferentType() {
            // Existe DESPESA ativa com nome "Geral", renomear RECEITA para "Geral" deve ser permitido
            FinancialCategory despesa = FinancialCategory.create("Geral", CategoryType.DESPESA);
            repository.save(despesa);

            FinancialCategory receita = FinancialCategory.create("Receita Antiga", CategoryType.RECEITA);
            repository.save(receita);

            UpdateCategoryCommand command = new UpdateCategoryCommand(receita.getId(), "Geral");
            CategoryResponse response = useCase.execute(command);

            assertEquals("Geral", response.name());
            assertEquals(CategoryType.RECEITA, response.type());
        }
    }

    @Nested
    @DisplayName("3. Validação de duplicidade na renomeação")
    class DuplicateValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve lançar DomainValidationException ao renomear para nome já usado por categoria ativa do mesmo tipo")
        void shouldThrowWhenNewNameConflictsWithActiveCategory() {
            FinancialCategory existing = FinancialCategory.create("Doações PIX", CategoryType.RECEITA);
            repository.save(existing);

            FinancialCategory alvo = FinancialCategory.create("Antigas Doações", CategoryType.RECEITA);
            repository.save(alvo);

            UpdateCategoryCommand command = new UpdateCategoryCommand(alvo.getId(), "Doações PIX");

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("Doações PIX"));
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
            UpdateCategoryCommand command = new UpdateCategoryCommand(unknownId, "Qualquer Nome");

            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(ex.getMessage().contains(unknownId.toString()));
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
