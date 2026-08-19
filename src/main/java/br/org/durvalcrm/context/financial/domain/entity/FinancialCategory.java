package br.org.durvalcrm.context.financial.domain.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import br.org.durvalcrm.context.financial.domain.exception.InvalidCategoryDataError;

public class FinancialCategory {
    
    private final UUID id;
    private String name;
    private final CategoryType type;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private FinancialCategory(
        UUID id,
        String name,
        CategoryType type,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
    ){
        this.id = Objects.requireNonNull(id, "O identificador não pode ser nulo.");
        this.type = Objects.requireNonNull(type, "O tipo de categoria (RECEITA ou DESPESA) é obrigatório.");
        this.createdAt = Objects.requireNonNull(createdAt, "A data de criação é obrigatória.");

        validateAndSetName(name);
        this.active = active;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
        this.deletedAt = deletedAt;
    }

    /**
     * Factory Method: cria uma nova conta/categoria atribuindo UUID aleatório.
     */
    public static FinancialCategory create(String name, CategoryType type){
        Instant now = Instant.now();
        return new FinancialCategory(
            UUID.randomUUID(),
            name,
            type,
            true,
            now,
            now,
            null
        );
    }

    /**
     * Factory Method: reconstitui a entidade a partir do banco de dados (Data Mapper).
     */
    public static FinancialCategory restore(
        UUID id,
        String name,
        CategoryType type,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
    ){
        return new FinancialCategory(id, name, type, active, createdAt, updatedAt, deletedAt);
    }

    // === Comportamentos de Negocio ===

    public void rename(String newName){
        validateAndSetName(newName);
        this.updatedAt = Instant.now();
    }

    public void inactivate(){
        if(!this.active) return;
        this.active = false;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    public void activate(){
        if(this.active) return;
        this.active = true;
        this.deletedAt = null;
        this.updatedAt = Instant.now();
    }

    private void validateAndSetName(String name){
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCategoryDataError("O nome da categoria não pode ser vazio.");
        }
        this.name = name.trim();
    }

    // === Getters ===

    public UUID getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

}
