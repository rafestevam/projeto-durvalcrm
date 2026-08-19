// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.usecase;

import java.util.Objects;

import br.org.durvalcrm.context.financial.application.dto.CategoryResponse;
import br.org.durvalcrm.context.financial.application.dto.CreateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.exception.CategoryAlreadyExistsError;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;

public class CreateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public CreateCategoryUseCase(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(
            categoryRepository,
            "O repositório de categorias é obrigatório."
        );
    }

    public CategoryResponse execute(CreateCategoryCommand command) {
        Objects.requireNonNull(command, "O comando não pode ser nulo");

        //Validação duplicados
        categoryRepository.findByNameAndType(command.name(), command.type())
            .filter(FinancialCategory::isActive)
            .ifPresent(existing -> {
                throw new CategoryAlreadyExistsError(
                    String.format(
                        "Já existe uma categoria ativa com o nome '%s'e tipo '%s'.",
                        command.name(),
                        command.type()
                    )
                );
            });
        
        //Criação da entidade
        FinancialCategory category = FinancialCategory.create(
            command.name(),
            command.type()
        );

        //Persistência
        categoryRepository.save(category);

        return CategoryResponse.fromEntity(category);
        
    }
    
}
