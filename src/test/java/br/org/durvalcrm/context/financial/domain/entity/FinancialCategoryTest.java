package br.org.durvalcrm.context.financial.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;

@DisplayName("FinantialCategory - Testes unitários de domínio")
class FinancialCategoryTest {
    
    @Nested
    @DisplayName("1. Criação e reestruturação")
    class CreationAndRestoreTests {

        @Test
        @DisplayName("Deve instanciar uma categoria ativa com UUID e timestamps válidos.")
        void shouldCreateValidCategory(){
            FinancialCategory category = FinancialCategory.create(
                "Doações PIX",
                CategoryType.RECEITA
            );

            assertNotNull(category.getId());
            assertEquals("Doações PIX", category.getName());
            assertEquals(CategoryType.RECEITA, category.getType());
            assertTrue(category.isActive());
            assertNotNull(category.getCreatedAt());
            assertEquals(category.getCreatedAt(), category.getUpdatedAt());
            assertNull(category.getDeletedAt());
            
        }

        @Test
        @DisplayName("Deve reconstituir a entidade sem alterar dados históricos (restore)")
        void shouldRestoreCategorySuccessfully() {
            UUID id = UUID.randomUUID();
            Instant past = Instant.now().minusSeconds(3600);

            FinancialCategory category = FinancialCategory.restore(
                    id, "Luz", CategoryType.DESPESA, false, past, past, past
            );

            assertEquals(id, category.getId());
            assertEquals("Luz", category.getName());
            assertFalse(category.isActive());
            assertEquals(past, category.getDeletedAt());
        }

    }

    @Nested
    @DisplayName("2. Invariantes e Regras de Validação")
    class InvariantValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar DomainValidationException ao informar nome nulo ou vazio")
        void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
            DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> FinancialCategory.create(invalidName, CategoryType.DESPESA)
            );
            assertEquals("O nome da categoria não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando o tipo for nulo")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThrows(
                    NullPointerException.class,
                    () -> FinancialCategory.create("Contas Fixas", null)
            );
        }

    }

    @Nested
    @DisplayName("3. Comportamento e Transições de Estado")
    class StateTransitionTests {

        @Test
        @DisplayName("Deve renomear e atualizar updatedAt")
        void shouldRenameCategory() throws InterruptedException {
            FinancialCategory category = FinancialCategory.create("Cantina", CategoryType.RECEITA);
            Instant beforeUpdate = category.getUpdatedAt();

            Thread.sleep(5);
            category.rename("Cantina e Lanches");

            assertEquals("Cantina e Lanches", category.getName());
            assertTrue(category.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        @DisplayName("Deve inativar categoria e definir deletedAt")
        void shouldInactivateCategory() {
            FinancialCategory category = FinancialCategory.create("Livraria", CategoryType.RECEITA);

            category.inactivate();

            assertFalse(category.isActive());
            assertNotNull(category.getDeletedAt());
            assertEquals(category.getDeletedAt(), category.getUpdatedAt());
        }

        @Test
        @DisplayName("Deve reativar categoria inativa e limpar deletedAt")
        void shouldReactivateCategory() {
            FinancialCategory category = FinancialCategory.create("Livraria", CategoryType.RECEITA);
            category.inactivate();

            category.activate();

            assertTrue(category.isActive());
            assertNull(category.getDeletedAt());
        }
    }
}
