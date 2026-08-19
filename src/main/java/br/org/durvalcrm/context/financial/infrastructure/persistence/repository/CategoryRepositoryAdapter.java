// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;
import br.org.durvalcrm.context.financial.infrastructure.persistence.mapper.FinancialCategoryMapper;
import br.org.durvalcrm.context.financial.infrastructure.persistence.model.FinancialCategoryDataModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 * Adapter que implementa {@link CategoryRepositoryPort} conectando o domínio
 * ao banco de dados relacional via JPA/Hibernate.
 *
 * <p>Utiliza o padrão Data Mapper através de {@link FinancialCategoryMapper}:
 * nenhuma lógica de negócio reside aqui — apenas tradução entre camadas e
 * execução de queries.</p>
 */
@ApplicationScoped
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final EntityManager em;

    public CategoryRepositoryAdapter(EntityManager em) {
        this.em = em;
    }

    /**
     * Persiste uma categoria. Usa {@code merge} para tratar tanto criação
     * quanto atualização de estado de forma transparente.
     */
    @Override
    @Transactional
    public void save(FinancialCategory category) {
        FinancialCategoryDataModel model = FinancialCategoryMapper.toDataModel(category);
        em.merge(model);
    }

    /** Busca pelo ID primário; retorna vazio se não existir. */
    @Override
    public Optional<FinancialCategory> findById(UUID id) {
        FinancialCategoryDataModel model = em.find(FinancialCategoryDataModel.class, id);
        return Optional.ofNullable(model)
                .map(FinancialCategoryMapper::toDomain);
    }

    /** Verifica unicidade de nome + tipo para impedir duplicatas. */
    @Override
    public Optional<FinancialCategory> findByNameAndType(String name, CategoryType type) {
        TypedQuery<FinancialCategoryDataModel> query = em.createQuery(
                "SELECT c FROM FinancialCategoryDataModel c " +
                "WHERE LOWER(c.name) = LOWER(:name) AND c.type = :type",
                FinancialCategoryDataModel.class
        );
        query.setParameter("name", name);
        query.setParameter("type", type);
        query.setMaxResults(1);

        return query.getResultStream()
                .findFirst()
                .map(FinancialCategoryMapper::toDomain);
    }

    /**
     * Lista categorias filtradas por tipo. Quando {@code type} é {@code null},
     * retorna todas as categorias (sem filtro de tipo).
     */
    @Override
    public List<FinancialCategory> findAll(CategoryType type) {
        if (type == null) {
            return em.createQuery(
                    "SELECT c FROM FinancialCategoryDataModel c ORDER BY c.name ASC",
                    FinancialCategoryDataModel.class
            )
            .getResultList()
            .stream()
            .map(FinancialCategoryMapper::toDomain)
            .toList();
        }

        return em.createQuery(
                "SELECT c FROM FinancialCategoryDataModel c WHERE c.type = :type ORDER BY c.name ASC",
                FinancialCategoryDataModel.class
        )
        .setParameter("type", type)
        .getResultList()
        .stream()
        .map(FinancialCategoryMapper::toDomain)
        .toList();
    }

    /**
     * Verifica integridade referencial com transações financeiras.
     * <p>
     * Retorna {@code false} enquanto o contexto financeiro de transações não
     * estiver implementado. A query será expandida quando a tabela de
     * transações existir.
     * </p>
     */
    @Override
    public boolean hasLinkedTransactions(UUID categoryId) {
        // TODO: substituir pelo JOIN real quando a tabela de transações existir.
        return false;
    }
}
