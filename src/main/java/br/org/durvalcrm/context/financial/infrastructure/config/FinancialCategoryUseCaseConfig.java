// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.config;

import br.org.durvalcrm.context.financial.application.usecase.CreateCategoryUseCase;
import br.org.durvalcrm.context.financial.application.usecase.InactivateCategoryUseCase;
import br.org.durvalcrm.context.financial.application.usecase.ListCategoriesUseCase;
import br.org.durvalcrm.context.financial.application.usecase.UpdateCategoryUseCase;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Fábrica CDI para os Use Cases do contexto financeiro.
 *
 * <p>Os Use Cases são classes Java puras (sem anotações de framework), portanto
 * precisam ser expostos como beans CDI a partir desta camada de infraestrutura.
 * Isso preserva a regra de independência de framework nas camadas de domínio
 * e aplicação.</p>
 */
@ApplicationScoped
public class FinancialCategoryUseCaseConfig {

    @Produces
    @ApplicationScoped
    public CreateCategoryUseCase createCategoryUseCase(CategoryRepositoryPort repository) {
        return new CreateCategoryUseCase(repository);
    }

    @Produces
    @ApplicationScoped
    public ListCategoriesUseCase listCategoriesUseCase(CategoryRepositoryPort repository) {
        return new ListCategoriesUseCase(repository);
    }

    @Produces
    @ApplicationScoped
    public UpdateCategoryUseCase updateCategoryUseCase(CategoryRepositoryPort repository) {
        return new UpdateCategoryUseCase(repository);
    }

    @Produces
    @ApplicationScoped
    public InactivateCategoryUseCase inactivateCategoryUseCase(CategoryRepositoryPort repository) {
        return new InactivateCategoryUseCase(repository);
    }
}
