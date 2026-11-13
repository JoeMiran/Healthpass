# HealthPass

Sistema de **identidade digital descentralizada (Blockchain ID)** aplicado à
área da saúde. Permite emitir, guardar e verificar **credenciais de saúde**
(primeiro caso de uso: certificados de vacinação **sintéticos**) sobre uma
blockchain permissionada.

Este projeto parte do núcleo blockchain da **TemplarCoin** (projeto da unidade
curricular de Sistemas Distribuídos) e adapta-o para identidade e credenciais,
removendo toda a lógica de criptomoeda.

## Objetivo

- Reaproveitar a blockchain existente da TemplarCoin (blocos, transações,
  criptografia) em vez de criar uma nova de raiz.
- Construir os módulos de **identidade (DID)**, **credenciais**, **consenso
  (PoA simplificado)** e **rede P2P**.
- Servir de base de estudo, com código comentado em português.

## Estado do projeto

**Esqueleto inicial (primeiro commit).** A estrutura de pacotes e as classes
existem com documentação e TODO, mas a lógica ainda não está implementada. Ver
`docs/ANALISE_TEMPLARCOIN.md` e `docs/MIGRACAO_TEMPLARCOIN.md`.

## Tecnologias

- **Java 21 (LTS)**
- **Apache Maven** (build)
- **Jackson** (JSON)
- **JUnit 5** (testes)
- Criptografia da plataforma Java: `SHA3-256`, `SHA3-256withRSA`, `AES/GCM`,
  `PBKDF2-HMAC-SHA256` (herdada da TemplarCoin)

## Requisitos para executar

- JDK 21 ou superior
- Apache Maven 3.9 ou superior
- Internet na primeira compilação (descarregar dependências)

## Como compilar

```bash
mvn clean compile
```

## Como executar os testes

```bash
mvn test
```

## Como gerar o jar

```bash
mvn package
```

## Estrutura dos diretórios

```
HealthPass/
├── pom.xml                     # configuracao Maven
├── README.md
├── .gitignore
├── data/
│   └── demo/                   # dados sinteticos de demonstracao
├── docs/
│   ├── ANALISE_TEMPLARCOIN.md  # analise do projeto original
│   ├── MIGRACAO_TEMPLARCOIN.md # mapa de migracao
│   ├── ARQUITETURA.md          # arquitetura por camadas
│   └── GUIA_EXECUCAO.md        # como compilar/executar
└── src/
    ├── main/java/pt/healthpass/
    │   ├── app/                # arranque da aplicacao
    │   ├── identity/           # DIDs e identidades
    │   ├── credential/         # credenciais de saude
    │   ├── crypto/             # hash, assinaturas, JSON canonico
    │   ├── ledger/             # blocos, transacoes, Merkle Tree, cadeia
    │   ├── consensus/          # Proof of Authority simplificado
    │   ├── network/            # rede P2P (TCP + JSON)
    │   ├── storage/            # persistencia local
    │   ├── gui/                # interfaces Swing
    │   └── config/             # configuracao
    └── test/java/pt/healthpass/
```

## Origem dos componentes

Os ficheiros reutilizados da TemplarCoin, e a respetiva adaptação, estão
documentados em `docs/MIGRACAO_TEMPLARCOIN.md`. A TemplarCoin original **não é
alterada** por este projeto.
