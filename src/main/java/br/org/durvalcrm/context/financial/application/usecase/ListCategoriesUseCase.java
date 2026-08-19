// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import java.util.List;
import java.util.Objects;

import br.org.durvalcrm.context.financial.application.dto.CategoryFilter;
import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

public class ListCategoriesUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public ListCategoriesUseCase(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(
            categoryRepository,
            "O repositório de categorias é obrigatório."
        );
    }

    public List<CategoryResponse> execute(CategoryFilter filter) {
        Objects.requireNonNull(filter, "O filtro não pode ser nulo.");

        return categoryRepository.findAll(filter.type())
            .stream()
            .filter(category -> !filter.isActive() || category.isActive())
            .map(CategoryResponse::fromEntity)
            .toList();
    }

}
