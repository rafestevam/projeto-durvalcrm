// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import br.org.durvalcrm.context.financial.domain.enums.CategoryType;

/**
 * Data Model (ORM entity) que representa a tabela financial_categories.
 * <p>
 * Isolado da entidade de domínio: nenhuma anotação JPA/ORM pode cruzar
 * para {@link br.org.durvalcrm.context.financial.domain.entity.FinancialCategory}.
 * A conversão entre os dois objetos é responsabilidade exclusiva de
 * {@link br.org.durvalcrm.context.financial.infrastructure.persistence.mapper.FinancialCategoryMapper}.
 * </p>
 */
@Entity
@Table(name = "financial_categories")
public class FinancialCategoryDataModel {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private CategoryType type;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Required by JPA — not used by application code. */
    protected FinancialCategoryDataModel() {}

    public FinancialCategoryDataModel(
            UUID id,
            String name,
            CategoryType type,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // === Getters ===

    public UUID getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    // === Setters (needed by JPA merge / upsert) ===

    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
