package br.org.durvalcrm.context.financial.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;

public interface CategoryRepositoryPort {
    
    /**
     * Persiste uma categoria (criação ou atualização de estado).
     *
     * @param category Entidade de domínio a ser persistida.
     */
    void save(FinancialCategory category);

    /**
     * Busca uma categoria pelo seu identificador único.
     *
     * @param id Identificador da categoria.
     * @return Optional contendo a entidade se encontrada, ou Optional.empty().
     */
    Optional<FinancialCategory> findById(UUID id);

    /**
     * Busca uma categoria por nome e tipo contábil para validação de unicidade.
     *
     * @param name Nome da categoria.
     * @param type Tipo contábil (INCOME ou EXPENSE).
     * @return Optional contendo a entidade se existente.
     */
    Optional<FinancialCategory> findByNameAndType(String name, CategoryType type);

    /**
     * Lista todas as categorias cadastradas, com suporte a filtro por tipo contábil.
     *
     * @param type Tipo contábil opcional; se nulo, retorna todos os tipos.
     * @return Lista imutável ou vazia de categorias encontradas.
     */
    List<FinancialCategory> findAll(CategoryType type);

    /**
     * Verifica se a categoria possui transações financeiras vinculadas.
     * Utilizado para garantir a regra de integridade antes de operações de inativação.
     *
     * @param categoryId Identificador da categoria.
     * @return true se houver vínculos, false caso contrário.
     */
    boolean hasLinkedTransactions(UUID categoryId);

}
