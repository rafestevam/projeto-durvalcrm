// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.org.durvalcrm.context.financial.application.dto.CategoryFilter;
import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.application.usecase.CreateCategoryUseCase;
import br.org.durvalcrm.context.financial.application.usecase.InactivateCategoryUseCase;
import br.org.durvalcrm.context.financial.application.usecase.ListCategoriesUseCase;
import br.org.durvalcrm.context.financial.application.usecase.UpdateCategoryUseCase;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.infrastructure.http.dto.CreateCategoryRequest;
import br.org.durvalcrm.context.financial.infrastructure.http.dto.UpdateCategoryRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialCategoryController - Testes unitários da camada HTTP")
class FinancialCategoryControllerTest {

    @Mock
    private CreateCategoryUseCase createCategoryUseCase;

    @Mock
    private ListCategoriesUseCase listCategoriesUseCase;

    @Mock
    private UpdateCategoryUseCase updateCategoryUseCase;

    @Mock
    private InactivateCategoryUseCase inactivateCategoryUseCase;

    private FinancialCategoryController controller;

    // ── Fixture reutilizável ─────────────────────────────────────────────────

    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private CategoryResponse buildResponse(UUID id, String name, CategoryType type, boolean active) {
        return new CategoryResponse(id, name, type, active, Instant.now(), Instant.now(), null);
    }

    @BeforeEach
    void setUp() {
        controller = new FinancialCategoryController(
                createCategoryUseCase,
                listCategoriesUseCase,
                updateCategoryUseCase,
                inactivateCategoryUseCase
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /api/v1/financial/categories
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST - Criação de categoria")
    class CreateCategoryTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 201 Created com o corpo da categoria criada")
        void shouldReturn201WithCreatedCategory() {
            CategoryResponse expected = buildResponse(CATEGORY_ID, "Doações PIX", CategoryType.RECEITA, true);
            when(createCategoryUseCase.execute(any())).thenReturn(expected);

            CreateCategoryRequest request = new CreateCategoryRequest("Doações PIX", CategoryType.RECEITA);
            Response response = controller.create(request);

            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            assertSame(expected, response.getEntity());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve delegar ao use case com o comando correto")
        void shouldDelegateToUseCaseWithCorrectCommand() {
            CategoryResponse expected = buildResponse(CATEGORY_ID, "Conta de Luz", CategoryType.DESPESA, true);
            when(createCategoryUseCase.execute(any())).thenReturn(expected);

            controller.create(new CreateCategoryRequest("Conta de Luz", CategoryType.DESPESA));

            verify(createCategoryUseCase, times(1)).execute(
                    argThat(cmd -> "Conta de Luz".equals(cmd.name()) && cmd.type() == CategoryType.DESPESA)
            );
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve propagar exceção lançada pelo use case")
        void shouldPropagateExceptionFromUseCase() {
            when(createCategoryUseCase.execute(any()))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThrows(RuntimeException.class,
                    () -> controller.create(new CreateCategoryRequest("X", CategoryType.RECEITA)));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /api/v1/financial/categories
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET - Listagem de categorias")
    class ListCategoriesTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 200 OK com lista de categorias sem filtros")
        void shouldReturn200WithAllCategories() {
            List<CategoryResponse> expected = List.of(
                    buildResponse(CATEGORY_ID, "Doações", CategoryType.RECEITA, true)
            );
            when(listCategoriesUseCase.execute(any())).thenReturn(expected);

            Response response = controller.list(null, "all");

            assertEquals(200, response.getStatus());
            assertSame(expected, response.getEntity());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve passar CategoryFilter.all() quando nenhum parâmetro é fornecido")
        void shouldPassAllFilterWhenNoParams() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            controller.list(null, "all");

            ArgumentCaptor<CategoryFilter> captor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(listCategoriesUseCase).execute(captor.capture());
            assertNull(captor.getValue().type());
            assertFalse(captor.getValue().isActive());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve passar filtro activeOnly=true quando status=active")
        void shouldPassActiveOnlyFilterWhenStatusIsActive() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            controller.list(null, "active");

            ArgumentCaptor<CategoryFilter> captor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(listCategoriesUseCase).execute(captor.capture());
            assertNull(captor.getValue().type());
            assertTrue(captor.getValue().isActive());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve passar filtro com type=RECEITA quando query param type é informado")
        void shouldPassTypeFilterWhenTypeParamProvided() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            controller.list(CategoryType.RECEITA, "all");

            ArgumentCaptor<CategoryFilter> captor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(listCategoriesUseCase).execute(captor.capture());
            assertEquals(CategoryType.RECEITA, captor.getValue().type());
            assertFalse(captor.getValue().isActive());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve passar filtro com type=DESPESA e activeOnly=true quando ambos os params são informados")
        void shouldPassTypeAndActiveFilterWhenBothParamsProvided() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            controller.list(CategoryType.DESPESA, "active");

            ArgumentCaptor<CategoryFilter> captor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(listCategoriesUseCase).execute(captor.capture());
            assertEquals(CategoryType.DESPESA, captor.getValue().type());
            assertTrue(captor.getValue().isActive());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 200 OK com lista vazia quando não há categorias")
        void shouldReturn200WithEmptyList() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            Response response = controller.list(null, "all");

            assertEquals(200, response.getStatus());
            List<?> body = (List<?>) response.getEntity();
            assertTrue(body.isEmpty());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve tratar status em maiúsculas como case-insensitive (ACTIVE → activeOnly=true)")
        void shouldTreatStatusCaseInsensitively() {
            when(listCategoriesUseCase.execute(any())).thenReturn(List.of());

            controller.list(null, "ACTIVE");

            ArgumentCaptor<CategoryFilter> captor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(listCategoriesUseCase).execute(captor.capture());
            assertTrue(captor.getValue().isActive());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/financial/categories/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT - Atualização de categoria")
    class UpdateCategoryTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 200 OK com a categoria atualizada")
        void shouldReturn200WithUpdatedCategory() {
            CategoryResponse expected = buildResponse(CATEGORY_ID, "Novo Nome", CategoryType.RECEITA, true);
            when(updateCategoryUseCase.execute(any())).thenReturn(expected);

            Response response = controller.update(CATEGORY_ID, new UpdateCategoryRequest("Novo Nome"));

            assertEquals(200, response.getStatus());
            assertSame(expected, response.getEntity());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve delegar ao use case com ID e novo nome corretos")
        void shouldDelegateToUseCaseWithCorrectIdAndName() {
            when(updateCategoryUseCase.execute(any()))
                    .thenReturn(buildResponse(CATEGORY_ID, "Novo Nome", CategoryType.RECEITA, true));

            controller.update(CATEGORY_ID, new UpdateCategoryRequest("Novo Nome"));

            verify(updateCategoryUseCase, times(1)).execute(
                    argThat(cmd -> CATEGORY_ID.equals(cmd.id()) && "Novo Nome".equals(cmd.newName()))
            );
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve propagar exceção lançada pelo use case na atualização")
        void shouldPropagateExceptionFromUseCaseOnUpdate() {
            when(updateCategoryUseCase.execute(any()))
                    .thenThrow(new RuntimeException("falha"));

            assertThrows(RuntimeException.class,
                    () -> controller.update(CATEGORY_ID, new UpdateCategoryRequest("X")));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/financial/categories/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE - Inativação lógica via DELETE")
    class DeleteCategoryTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 200 OK com a categoria inativada")
        void shouldReturn200WithInactivatedCategory() {
            CategoryResponse expected = buildResponse(CATEGORY_ID, "Doações", CategoryType.RECEITA, false);
            when(inactivateCategoryUseCase.execute(CATEGORY_ID)).thenReturn(expected);

            Response response = controller.delete(CATEGORY_ID);

            assertEquals(200, response.getStatus());
            assertSame(expected, response.getEntity());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve delegar ao InactivateCategoryUseCase com o UUID correto")
        void shouldDelegateToUseCaseWithCorrectId() {
            when(inactivateCategoryUseCase.execute(CATEGORY_ID))
                    .thenReturn(buildResponse(CATEGORY_ID, "Doações", CategoryType.RECEITA, false));

            controller.delete(CATEGORY_ID);

            verify(inactivateCategoryUseCase, times(1)).execute(CATEGORY_ID);
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve propagar exceção lançada pelo use case no DELETE")
        void shouldPropagateExceptionFromUseCaseOnDelete() {
            when(inactivateCategoryUseCase.execute(any()))
                    .thenThrow(new RuntimeException("não encontrado"));

            assertThrows(RuntimeException.class, () -> controller.delete(CATEGORY_ID));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/financial/categories/{id}/inactivate
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH - Inativação lógica via PATCH /inactivate")
    class InactivatePatchTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar 200 OK com a categoria inativada via PATCH")
        void shouldReturn200WithInactivatedCategoryViaPatch() {
            CategoryResponse expected = buildResponse(CATEGORY_ID, "Cantina", CategoryType.RECEITA, false);
            when(inactivateCategoryUseCase.execute(CATEGORY_ID)).thenReturn(expected);

            Response response = controller.inactivate(CATEGORY_ID);

            assertEquals(200, response.getStatus());
            assertSame(expected, response.getEntity());
        }

        @Test
        @Timeout(30)
        @DisplayName("PATCH /inactivate e DELETE devem delegar ao mesmo use case com o mesmo ID")
        void patchAndDeleteShouldDelegateToSameUseCaseWithSameId() {
            CategoryResponse inactivated = buildResponse(CATEGORY_ID, "Cantina", CategoryType.RECEITA, false);
            when(inactivateCategoryUseCase.execute(CATEGORY_ID)).thenReturn(inactivated);

            controller.delete(CATEGORY_ID);
            controller.inactivate(CATEGORY_ID);

            verify(inactivateCategoryUseCase, times(2)).execute(CATEGORY_ID);
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve propagar exceção lançada pelo use case no PATCH")
        void shouldPropagateExceptionFromUseCaseOnPatch() {
            when(inactivateCategoryUseCase.execute(any()))
                    .thenThrow(new RuntimeException("falha"));

            assertThrows(RuntimeException.class, () -> controller.inactivate(CATEGORY_ID));
        }
    }
}
