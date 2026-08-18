# Diretrizes de Contribuição — DurvalCRM

Agradecemos o seu interesse em contribuir para o DurvalCRM! Como um projeto mantido pela comunidade e voltado ao Terceiro Setor, seguimos padrões rigorosos de engenharia de software para garantir sustentabilidade a longo prazo.

---

## 1. Princípios Arquiteturais Obrigatórios

1. **Independência de Frameworks:** Nenhum arquivo dentro de `domain/` ou `application/` pode importar bibliotecas externas (como ORMs, drivers de banco, frameworks HTTP ou SDKs de terceiros).
2. **Imutabilidade e Segurança de Domínio:** Entidades e *Value Objects* devem validar seus próprios estados na instanciação e manter imutabilidade sempre que aplicável.
3. **Padrão Ports & Adapters:** Qualquer interação com I/O (disco, banco de dados, storage, provedor de e-mail/pagamento) deve ser declarada como uma interface (Porta) e implementada na camada de infraestrutura (Adaptador).

---

## 2. Cabeçalho SPDX Obrigatório

Todo arquivo de código-fonte (`.ts`, `.java`, `.cs`, etc.) deve incluir na primeira linha o identificador de licença:

```typescript
// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later
```