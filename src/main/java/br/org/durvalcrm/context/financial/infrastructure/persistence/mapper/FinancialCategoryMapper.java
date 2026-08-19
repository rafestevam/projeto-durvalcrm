// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.persistence.mapper;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.infrastructure.persistence.model.FinancialCategoryDataModel;

/**
 * Data Mapper responsável pela conversão bidirecional entre
 * {@link FinancialCategory} (entidade de domínio) e
 * {@link FinancialCategoryDataModel} (representação de persistência).
 *
 * <p>Garante que as camadas de domínio e infraestrutura permaneçam
 * completamente desacopladas: nenhuma das duas conhece a outra diretamente.</p>
 */
public final class FinancialCategoryMapper {

    private FinancialCategoryMapper() {}

    /**
     * Converte uma entidade de domínio em um Data Model pronto para persistência.
     *
     * @param domain entidade de domínio origem.
     * @return Data Model mapeado.
     */
    public static FinancialCategoryDataModel toDataModel(FinancialCategory domain) {
        return new FinancialCategoryDataModel(
                domain.getId(),
                domain.getName(),
                domain.getType(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt()
        );
    }

    /**
     * Reconstitui uma entidade de domínio a partir do Data Model lido do banco.
     * Utiliza o factory method {@link FinancialCategory#restore} para garantir
     * invariantes de domínio.
     *
     * @param model Data Model lido do banco de dados.
     * @return Entidade de domínio reconstituída.
     */
    public static FinancialCategory toDomain(FinancialCategoryDataModel model) {
        return FinancialCategory.restore(
                model.getId(),
                model.getName(),
                model.getType(),
                model.isActive(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                model.getDeletedAt()
        );
    }
}
