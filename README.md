# DurvalCRM

> Sistema Open Source de Gestão Integrada para Centros Espíritas e Entidades Sem Fins Lucrativos.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-brightgreen.svg)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
[![DCO Certified](https://img.shields.io/badge/DCO-1.1%20Signed-orange.svg)](https://developercertificate.org/)

---

## 📌 Sobre o Projeto

O **DurvalCRM** é uma solução comunitária concebida sob os princípios de **Clean Architecture** e **Clean Code** para desonerar os voluntários da administração de entidades do Terceiro Setor.

### Bounded Contexts (Contextos Delimitados)
1. **Financeiro (`FinancialContext`):** Contas a pagar/receber, plano de contas dinâmico, upload desacoplado de comprovantes e emissão simplificada de balancete mensal para envio ao contador.
2. **Voluntários (`VolunteerContext`):** Cadastro em conformidade com a LGPD e Lei do Voluntariado (Lei nº 9.608/1998), termo de adesão digital com hash SHA-256 e gestão de escalas de trabalho *top-down*.
3. **Eventos (`EventContext`):** Planejamento orçamentário, apuração de receitas/despesas e montagem de equipes de apoio dedicadas.
4. **Inventário & Patrimônio (`InventoryContext`):** Controle de estoque de cantina, acervo da livraria e inventário de móveis/equipamentos para tombamento contábil.

---

## 🏛️ Arquitetura

O sistema aplica a **Regra de Dependência** da Clean Architecture: o núcleo de domínio não possui qualquer acoplamento com frameworks web, bancos de dados ou provedores de armazenamento.

   [ Core Domain (Entities & Value Objects) ]
                      ▲
   [ Application (Use Cases & Ports) ]
                      ▲
   [ Interface Adapters (Controllers, Repositories, Presenters) ]
                      ▲
   [ Frameworks & Drivers (PostgreSQL, Express/NestJS, Keycloak, S3/MinIO) ]

* **Autenticação:** Delegada ao **Keycloak** (IdP / OIDC).
* **Armazenamento de Comprovantes:** Abstraído via *Port & Adapter* (`FileStoragePort`), suportando S3/MinIO ou FTP.

---

## 🤝 Como Contribuir

Contribuições da comunidade são muito bem-vindas! Por favor, leia nosso [Guia de Contribuição](CONTRIBUTING.md) antes de submeter um Pull Request.

Todos os commits devem ser assinados conforme o [Developer Certificate of Origin (DCO)](DCO) com a flag `-s`:
```bash
git commit -s -m "feat(financial): implement Money value object"
```

---

## 📄 Licença

Distribuído sob a licença **GNU AGPLv3**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes. Qualquer melhoria hospedada e oferecida como serviço deve permanecer em código aberto.