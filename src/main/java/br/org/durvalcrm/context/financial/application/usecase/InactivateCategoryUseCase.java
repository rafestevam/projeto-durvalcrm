// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import java.util.Objects;
import java.util.UUID;

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.exception.CategoryNotFoundError;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

public class InactivateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public InactivateCategoryUseCase(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(
            categoryRepository,
            "O repositório de categorias é obrigatório."
        );
    }

    public CategoryResponse execute(UUID categoryId) {
        Objects.requireNonNull(categoryId, "O ID da categoria não pode ser nulo.");

        // Busca a categoria existente
        FinancialCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundError(
                String.format("Categoria com ID '%s' não encontrada.", categoryId)
            ));

        // Verifica se já está inativa
        if (!category.isActive()) {
            throw new DomainValidationException(
                String.format("A categoria '%s' já está inativa.", category.getName())
            );
        }

        // Verifica integridade referencial com transações financeiras
        if (categoryRepository.hasLinkedTransactions(categoryId)) {
            throw new DomainValidationException(
                String.format(
                    "A categoria '%s' não pode ser inativada pois possui transações financeiras vinculadas.",
                    category.getName()
                )
            );
        }

        // Inativa a entidade e persiste
        category.inactivate();
        categoryRepository.save(category);

        return CategoryResponse.fromEntity(category);
    }

}
