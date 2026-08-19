// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.port.CategoryRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Bean CDI auxiliar para os testes E2E.
 *
 * <p>Como o Quarkus não gerencia o ciclo de vida CDI da classe de teste
 * (que não é um bean), métodos {@code @Transactional} dentro de
 * {@code @QuarkusTest} são ignorados silenciosamente. Este bean resolve o
 * problema: é um CDI bean real, portanto seus métodos são interceptados
 * e o commit ocorre antes que o RestAssured dispare a requisição HTTP.</p>
 *
 * <p>O método {@link #deleteByName} usa JPQL DELETE direto para remover
 * TODOS os registros com aquele nome (ativos e inativos), garantindo
 * isolamento total entre testes — sem contaminação residual de runs anteriores.</p>
 */
@ApplicationScoped
class E2ETestFixtureHelper {

    @Inject
    CategoryRepositoryPort repository;

    @Inject
    EntityManager em;

    @Transactional
    FinancialCategory persistActive(String name, CategoryType type) {
        FinancialCategory cat = FinancialCategory.create(name, type);
        repository.save(cat);
        return cat;
    }

    @Transactional
    FinancialCategory persistInactive(String name, CategoryType type) {
        FinancialCategory cat = FinancialCategory.create(name, type);
        cat.inactivate();
        repository.save(cat);
        return cat;
    }

    /**
     * Remove fisicamente todos os registros com o nome+tipo informados.
     * Usar no {@code @AfterEach} para garantir isolamento total.
     */
    @Transactional
    void deleteByName(String name) {
        em.createQuery(
            "DELETE FROM FinancialCategoryDataModel c WHERE LOWER(c.name) = LOWER(:name)")
            .setParameter("name", name)
            .executeUpdate();
    }
}
