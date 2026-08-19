// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Testes de integração para {@link CategoryRepositoryAdapter}.
 *
 * <p>Executa contra o banco H2 em memória (perfil {@code %test}) com schema
 * gerado automaticamente pelo Hibernate (drop-and-create). A anotação
 * {@link TestTransaction} garante rollback automático após cada teste,
 * mantendo isolamento total entre os cenários sem necessidade de limpeza manual.</p>
 *
 * <p>Nota: {@code @Nested} não é utilizado pois o Quarkus CDI não processa
 * interceptors em classes internas não gerenciadas.</p>
 */
@QuarkusTest
@DisplayName("CategoryRepositoryAdapter - Testes de integração de repositório")
class CategoryRepositoryAdapterIT {

    @Inject
    CategoryRepositoryAdapter repository;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private FinancialCategory saveActive(String name, CategoryType type) {
        FinancialCategory category = FinancialCategory.create(name, type);
        repository.save(category);
        return category;
    }

    private FinancialCategory saveInactive(String name, CategoryType type) {
        FinancialCategory category = FinancialCategory.create(name, type);
        category.inactivate();
        repository.save(category);
        return category;
    }

    // =========================================================================
    // 1. Persistência real — save e findById
    // =========================================================================

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Persistência] Deve persistir RECEITA e recuperar pelo ID com campos íntegros")
    void shouldPersistAndFindReceitaCategory() {
        FinancialCategory saved = saveActive("Doações PIX", CategoryType.RECEITA);

        Optional<FinancialCategory> found = repository.findById(saved.getId());

        assertTrue(found.isPresent(), "Categoria deve ser encontrada após save");
        FinancialCategory result = found.get();
        assertEquals(saved.getId(), result.getId());
        assertEquals("Doações PIX", result.getName());
        assertEquals(CategoryType.RECEITA, result.getType());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getDeletedAt());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Persistência] Deve persistir DESPESA e recuperar pelo ID com campos íntegros")
    void shouldPersistAndFindDespesaCategory() {
        FinancialCategory saved = saveActive("Conta de Luz", CategoryType.DESPESA);

        Optional<FinancialCategory> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(CategoryType.DESPESA, found.get().getType());
        assertEquals("Conta de Luz", found.get().getName());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Persistência] Deve retornar Optional.empty() ao buscar ID inexistente")
    void shouldReturnEmptyForUnknownId() {
        Optional<FinancialCategory> result = repository.findById(UUID.randomUUID());

        assertFalse(result.isPresent(), "Deve retornar vazio para ID desconhecido");
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Persistência] Deve atualizar nome da categoria ao fazer merge via save")
    void shouldUpdateCategoryNameOnSecondSave() {
        FinancialCategory original = saveActive("Cantina", CategoryType.RECEITA);

        original.rename("Cantina e Lanches");
        repository.save(original);

        Optional<FinancialCategory> updated = repository.findById(original.getId());
        assertTrue(updated.isPresent());
        assertEquals("Cantina e Lanches", updated.get().getName());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Persistência] Deve preservar createdAt imutável após atualização")
    void shouldPreserveCreatedAtAfterUpdate() {
        FinancialCategory category = saveActive("Livraria", CategoryType.RECEITA);
        var originalCreatedAt = category.getCreatedAt();

        category.rename("Livraria Espírita");
        repository.save(category);

        FinancialCategory reloaded = repository.findById(category.getId()).orElseThrow();
        assertEquals(originalCreatedAt, reloaded.getCreatedAt(),
                "createdAt não deve mudar após atualização");
    }

    // =========================================================================
    // 2. Consultas com filtro — findByNameAndType e findAll(type)
    // =========================================================================

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] Deve encontrar categoria por nome e tipo com comparação case-insensitive")
    void shouldFindByNameAndTypeCaseInsensitive() {
        saveActive("Doações Mensais", CategoryType.RECEITA);

        Optional<FinancialCategory> lower =
                repository.findByNameAndType("doações mensais", CategoryType.RECEITA);
        Optional<FinancialCategory> upper =
                repository.findByNameAndType("DOAÇÕES MENSAIS", CategoryType.RECEITA);

        assertTrue(lower.isPresent(), "Busca em minúsculas deve retornar resultado");
        assertTrue(upper.isPresent(), "Busca em maiúsculas deve retornar resultado");
        assertEquals("Doações Mensais", lower.get().getName());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] Deve retornar Optional.empty() para nome existente mas tipo diferente")
    void shouldReturnEmptyForDifferentType() {
        saveActive("Aluguel", CategoryType.DESPESA);

        Optional<FinancialCategory> result =
                repository.findByNameAndType("Aluguel", CategoryType.RECEITA);

        assertFalse(result.isPresent(),
                "Nome existente em DESPESA não deve ser encontrado como RECEITA");
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] Deve retornar Optional.empty() para nome inexistente")
    void shouldReturnEmptyForUnknownName() {
        Optional<FinancialCategory> result =
                repository.findByNameAndType("Categoria Inexistente", CategoryType.RECEITA);

        assertFalse(result.isPresent());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] findAll sem filtro de tipo deve retornar todas as categorias ordenadas por nome ASC")
    void shouldReturnAllCategoriesOrderedByNameWhenTypeFilterIsNull() {
        saveActive("Aluguel", CategoryType.DESPESA);
        saveActive("Doações", CategoryType.RECEITA);
        saveActive("Água e Luz", CategoryType.DESPESA);

        List<FinancialCategory> all = repository.findAll(null);

        assertTrue(all.size() >= 3, "Deve retornar ao menos as 3 categorias inseridas");
        for (int i = 0; i < all.size() - 1; i++) {
            assertTrue(
                    all.get(i).getName().compareToIgnoreCase(all.get(i + 1).getName()) <= 0,
                    "Resultado deve estar ordenado por nome ASC"
            );
        }
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] findAll com tipo RECEITA deve retornar apenas categorias de receita")
    void shouldReturnOnlyReceitaCategoriesWhenFiltered() {
        saveActive("Doações", CategoryType.RECEITA);
        saveActive("Eventos", CategoryType.RECEITA);
        saveActive("Conta de Água", CategoryType.DESPESA);

        List<FinancialCategory> receitas = repository.findAll(CategoryType.RECEITA);

        assertTrue(receitas.size() >= 2, "Deve retornar ao menos as 2 receitas inseridas");
        receitas.forEach(c -> assertEquals(CategoryType.RECEITA, c.getType(),
                "Todos os resultados devem ser RECEITA"));
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] findAll com tipo DESPESA deve retornar apenas categorias de despesa")
    void shouldReturnOnlyDespesaCategoriesWhenFiltered() {
        saveActive("Salários", CategoryType.DESPESA);
        saveActive("Material de Escritório", CategoryType.DESPESA);
        saveActive("Vendas Livraria", CategoryType.RECEITA);

        List<FinancialCategory> despesas = repository.findAll(CategoryType.DESPESA);

        assertTrue(despesas.size() >= 2);
        despesas.forEach(c -> assertEquals(CategoryType.DESPESA, c.getType(),
                "Todos os resultados devem ser DESPESA"));
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Filtro] findAll deve retornar lista não nula (nunca null)")
    void shouldReturnNonNullListAlways() {
        List<FinancialCategory> result = repository.findAll(null);

        assertNotNull(result, "Resultado nunca deve ser null");
    }

    // =========================================================================
    // 3. Integridade do soft-delete
    // =========================================================================

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] Deve persistir deletedAt e active=false ao inativar uma categoria")
    void shouldPersistSoftDeleteFieldsOnInactivation() {
        FinancialCategory category = saveActive("Cantina", CategoryType.RECEITA);

        category.inactivate();
        repository.save(category);

        FinancialCategory reloaded = repository.findById(category.getId()).orElseThrow();
        assertFalse(reloaded.isActive(), "active deve ser false após inativação");
        assertNotNull(reloaded.getDeletedAt(), "deletedAt deve ser preenchido após inativação");
        assertEquals(reloaded.getDeletedAt(), reloaded.getUpdatedAt(),
                "deletedAt e updatedAt devem ser iguais no momento da inativação");
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] Deve limpar deletedAt e restaurar active=true ao reativar categoria inativa")
    void shouldClearSoftDeleteFieldsOnReactivation() {
        FinancialCategory category = saveInactive("Livraria", CategoryType.RECEITA);

        category.activate();
        repository.save(category);

        FinancialCategory reloaded = repository.findById(category.getId()).orElseThrow();
        assertTrue(reloaded.isActive(), "active deve ser true após reativação");
        assertNull(reloaded.getDeletedAt(), "deletedAt deve ser null após reativação");
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] Categoria inativa deve permanecer visível em findById (sem filtro automático)")
    void shouldFindInactiveCategoryById() {
        FinancialCategory inactive = saveInactive("Eventos Antigos", CategoryType.RECEITA);

        Optional<FinancialCategory> found = repository.findById(inactive.getId());

        assertTrue(found.isPresent(), "findById não deve filtrar categorias inativas");
        assertFalse(found.get().isActive());
        assertNotNull(found.get().getDeletedAt());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] findByNameAndType deve retornar categoria inativa (decisão de negócio é do domínio)")
    void shouldFindInactiveCategoryByNameAndType() {
        saveInactive("Rifa", CategoryType.RECEITA);

        Optional<FinancialCategory> found =
                repository.findByNameAndType("Rifa", CategoryType.RECEITA);

        assertTrue(found.isPresent(),
                "findByNameAndType não deve silenciosamente excluir inativas — decisão é do domínio");
        assertFalse(found.get().isActive());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] findAll sem filtro deve incluir categorias inativas")
    void shouldIncludeInactiveCategoriesInFindAll() {
        FinancialCategory inactive = saveInactive("Despesa Inativa", CategoryType.DESPESA);

        List<FinancialCategory> all = repository.findAll(null);

        boolean containsInactive = all.stream()
                .anyMatch(c -> c.getId().equals(inactive.getId()));
        assertTrue(containsInactive,
                "findAll deve retornar todas as categorias, incluindo inativas");
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] Ciclo completo: ativo → inativo → ativo deve preservar ID e tipo imutáveis")
    void shouldPreserveImmutableFieldsThroughFullLifecycle() {
        FinancialCategory category = saveActive("Arrecadação", CategoryType.RECEITA);
        UUID originalId = category.getId();

        // Inativa e persiste
        category.inactivate();
        repository.save(category);

        FinancialCategory afterInactivation = repository.findById(originalId).orElseThrow();
        assertFalse(afterInactivation.isActive());
        assertEquals(originalId, afterInactivation.getId());
        assertEquals(CategoryType.RECEITA, afterInactivation.getType());

        // Reativa e persiste
        afterInactivation.activate();
        repository.save(afterInactivation);

        FinancialCategory afterReactivation = repository.findById(originalId).orElseThrow();
        assertTrue(afterReactivation.isActive());
        assertNull(afterReactivation.getDeletedAt());
        assertEquals(originalId, afterReactivation.getId());
        assertEquals(CategoryType.RECEITA, afterReactivation.getType());
    }

    @Test
    @Timeout(30)
    @TestTransaction
    @DisplayName("[Soft-delete] hasLinkedTransactions deve retornar false (contexto de transações não implementado)")
    void shouldReturnFalseForHasLinkedTransactions() {
        FinancialCategory category = saveActive("Qualquer", CategoryType.DESPESA);

        boolean hasLinks = repository.hasLinkedTransactions(category.getId());

        assertFalse(hasLinks,
                "hasLinkedTransactions deve retornar false até o contexto de transações ser implementado");
    }
}
