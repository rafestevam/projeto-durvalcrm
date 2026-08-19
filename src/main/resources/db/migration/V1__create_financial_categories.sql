-- SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
-- SPDX-License-Identifier: AGPL-3.0-or-later
--
-- Flyway migration: V1 — financial_categories
-- Cria a tabela de categorias financeiras (plano de contas) do contexto financeiro.

CREATE TABLE IF NOT EXISTS financial_categories (
    id          UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_financial_categories PRIMARY KEY (id),
    CONSTRAINT chk_financial_categories_type CHECK (type IN ('RECEITA', 'DESPESA')),
    CONSTRAINT uq_financial_categories_name_type UNIQUE (name, type)
);

COMMENT ON TABLE  financial_categories                IS 'Plano de contas do contexto financeiro';
COMMENT ON COLUMN financial_categories.id             IS 'Identificador único UUID da categoria';
COMMENT ON COLUMN financial_categories.name           IS 'Nome da categoria (único por tipo)';
COMMENT ON COLUMN financial_categories.type           IS 'Tipo contábil: RECEITA ou DESPESA';
COMMENT ON COLUMN financial_categories.active         IS 'Indica se a categoria está ativa';
COMMENT ON COLUMN financial_categories.created_at     IS 'Carimbo de data/hora da criação (UTC)';
COMMENT ON COLUMN financial_categories.updated_at     IS 'Carimbo da última atualização (UTC)';
COMMENT ON COLUMN financial_categories.deleted_at     IS 'Carimbo da inativação lógica (NULL = ativa)';
