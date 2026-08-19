// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import java.util.Objects;

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.application.dto.UpdateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

public class UpdateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public UpdateCategoryUseCase(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(
            categoryRepository,
            "O repositório de categorias é obrigatório."
        );
    }

    public CategoryResponse execute(UpdateCategoryCommand command) {
        Objects.requireNonNull(command, "O comando não pode ser nulo");

        // Busca a categoria existente
        FinancialCategory category = categoryRepository.findById(command.id())
            .orElseThrow(() -> new DomainValidationException(
                String.format("Categoria com ID '%s' não encontrada.", command.id())
            ));
        
        // Validação de duplicidade (excluindo a própria categoria)
        categoryRepository.findByNameAndType(command.newName(), category.getType())
            .filter(FinancialCategory::isActive)
            .ifPresent(existing -> {
                throw new DomainValidationException(
                    String.format(
                        "Já existe uma categoria ativa com o nome '%s' e tipo '%s'.",
                        command.newName(),
                        category.getType().name()
                    )
                );
            });
        
        //Altera propriedades da categoria
        category.rename(command.newName());

        //Persistência
        categoryRepository.save(category);

        return CategoryResponse.fromEntity(category);
        
    }
    
}
