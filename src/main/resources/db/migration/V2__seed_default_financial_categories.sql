-- SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
-- SPDX-License-Identifier: AGPL-3.0-or-later
--
-- Flyway migration: V2 — seed das categorias financeiras padrão do Centro Espírita
--
-- Pré-cadastra as 8 categorias padrão (4 de receita e 4 de despesa).
-- Idempotente: ON CONFLICT DO NOTHING garante que execuções repetidas
-- não duplicam registros, respeitando a constraint uq_financial_categories_name_type.

INSERT INTO financial_categories (id, name, type, active, created_at, updated_at, deleted_at)
VALUES
    -- Categorias de Receita
    ('11111111-0000-0000-0000-000000000001', 'Doações',                    'RECEITA', TRUE, NOW(), NOW(), NULL),
    ('11111111-0000-0000-0000-000000000002', 'Contribuição de Voluntários', 'RECEITA', TRUE, NOW(), NOW(), NULL),
    ('11111111-0000-0000-0000-000000000003', 'Cantina',                    'RECEITA', TRUE, NOW(), NOW(), NULL),
    ('11111111-0000-0000-0000-000000000004', 'Livraria',                   'RECEITA', TRUE, NOW(), NOW(), NULL),

    -- Categorias de Despesa
    ('22222222-0000-0000-0000-000000000001', 'Contas Fixas',               'DESPESA', TRUE, NOW(), NOW(), NULL),
    ('22222222-0000-0000-0000-000000000002', 'Insumos',                    'DESPESA', TRUE, NOW(), NOW(), NULL),
    ('22222222-0000-0000-0000-000000000003', 'Manutenção',                 'DESPESA', TRUE, NOW(), NOW(), NULL),
    ('22222222-0000-0000-0000-000000000004', 'Móveis/Equipamentos',        'DESPESA', TRUE, NOW(), NOW(), NULL)

ON CONFLICT (name, type) DO NOTHING;
