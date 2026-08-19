// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Controlador HTTP para o sub-domínio de Categorias Financeiras.
 *
 * <p>Expõe operações CRUD mapeadas em endpoints REST versionados sob
 * {@code /api/v1/financial/categories}. A conversão de exceções de domínio
 * em respostas HTTP é delegada ao {@link CategoryExceptionMapper}.</p>
 */
@Path("/api/v1/financial/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FinancialCategoryController {

    private final CreateCategoryUseCase createCategory;
    private final ListCategoriesUseCase listCategories;
    private final UpdateCategoryUseCase updateCategory;
    private final InactivateCategoryUseCase inactivateCategory;

    @Inject
    public FinancialCategoryController(
            CreateCategoryUseCase createCategory,
            ListCategoriesUseCase listCategories,
            UpdateCategoryUseCase updateCategory,
            InactivateCategoryUseCase inactivateCategory) {
        this.createCategory = createCategory;
        this.listCategories = listCategories;
        this.updateCategory = updateCategory;
        this.inactivateCategory = inactivateCategory;
    }

    /**
     * POST /api/v1/financial/categories
     * Cadastra uma nova categoria financeira.
     *
     * @return 201 Created com o recurso criado no corpo.
     */
    @POST
    public Response create(@Valid CreateCategoryRequest request) {
        CategoryResponse response = createCategory.execute(request.toCommand());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * GET /api/v1/financial/categories
     * Lista categorias com filtros opcionais.
     *
     * @param type   Filtra por tipo: {@code RECEITA} ou {@code DESPESA}. Opcional.
     * @param status {@code active} retorna apenas ativas; qualquer outro valor (ou omitido)
     *               retorna todas. Padrão: {@code all}.
     * @return 200 OK com array de categorias.
     */
    @GET
    public Response list(
            @QueryParam("type") CategoryType type,
            @QueryParam("status") @DefaultValue("all") String status) {

        boolean activeOnly = "active".equalsIgnoreCase(status);

        CategoryFilter filter = (type != null)
                ? new CategoryFilter(type, activeOnly)
                : (activeOnly ? CategoryFilter.activeOnly() : CategoryFilter.all());

        List<CategoryResponse> categories = listCategories.execute(filter);
        return Response.ok(categories).build();
    }

    /**
     * PUT /api/v1/financial/categories/{id}
     * Atualiza o nome de uma categoria existente.
     *
     * @return 200 OK com a categoria atualizada.
     */
    @PUT
    @Path("/{id}")
    public Response update(
            @PathParam("id") UUID id,
            @Valid UpdateCategoryRequest request) {
        CategoryResponse response = updateCategory.execute(request.toCommand(id));
        return Response.ok(response).build();
    }

    /**
     * DELETE /api/v1/financial/categories/{id}
     * Realiza a inativação lógica (soft delete) de uma categoria.
     *
     * @return 200 OK com a categoria inativada.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        CategoryResponse response = inactivateCategory.execute(id);
        return Response.ok(response).build();
    }

    /**
     * PATCH /api/v1/financial/categories/{id}/inactivate
     * Alias semântico para a inativação lógica (soft delete).
     *
     * @return 200 OK com a categoria inativada.
     */
    @PATCH
    @Path("/{id}/inactivate")
    public Response inactivate(@PathParam("id") UUID id) {
        CategoryResponse response = inactivateCategory.execute(id);
        return Response.ok(response).build();
    }
}
