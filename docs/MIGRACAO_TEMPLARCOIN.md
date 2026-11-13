# Mapa de migração: TemplarCoin → HealthPass

> Este documento liga cada ficheiro do HealthPass à sua origem (ou indica que é
> trabalho novo). Serve para manter **documentado o histórico de origem** dos
> componentes reutilizados, conforme exigido.

Legenda:

- **REUTILIZAR** — copiado com poucas alterações.
- **ADAPTAR** — reaproveita a ideia/estrutura, mas muda substancialmente.
- **DESCARTAR** — existe no original mas não vai para o HealthPass.
- **NOVO** — não existe no original; construído de raiz.

## 1. Núcleo da blockchain

| HealthPass | Origem (TemplarCoin) | Ação | Notas |
|---|---|---|---|
| `ledger.Block` | `core.Block` | ADAPTAR | Adiciona `height`, `timestamp`, `merkleRoot`, lista de `Transaction`, `validatorId`. Mantém a fórmula de hash determinística. |
| `ledger.Blockchain` | `core.BlockChain` | ADAPTAR | Remove o génesis monetário; adiciona pool de pendentes, validação da cadeia, Merkle Root e gancho de consenso. Persistência delegada. |
| `ledger.Transaction` | `templarCoin.core.Transaction` | ADAPTAR | Deixa de ser monetária; passa a `type` + `payload` + assinaturas. |
| `ledger.TransactionType` | — | NOVO | Enum com `IDENTITY_REGISTERED`, `CREDENTIAL_ISSUED`, `CREDENTIAL_REVOKED`. |
| `ledger.MerkleTree` | — | NOVO | Não existia árvore de Merkle no original. |
| `ledger.ChainRepository` | `core.BlockChain` (persistência) | NOVO | Extrai a persistência que estava embutida no `BlockChain`. |

## 2. Criptografia e serialização

| HealthPass | Origem (TemplarCoin) | Ação | Notas |
|---|---|---|---|
| `crypto.HashUtil` | `utils.SecurityUtils.getHash` | ADAPTAR | Isola o cálculo de hash (`SHA3-256`) numa classe própria. |
| `crypto.SignatureService` | `utils.SecurityUtils.sign/verifySign` | ADAPTAR | Isola `SHA3-256withRSA` (mantido por decisão do projeto). |
| `crypto.CanonicalSerializer` | `utils.Conversion` | ADAPTAR | Troca a serialização Java (insegura) por JSON canónico determinístico (Jackson). |
| `storage.KeyStoreService` | `utils.SecurityUtils` (AES/PBKDF2, IO de chaves) | ADAPTAR | Gere chaves em ficheiro (`.pub`/`.priv`), mantendo as chaves privadas fora da cadeia. |

## 3. Identidade

| HealthPass | Origem (TemplarCoin) | Ação | Notas |
|---|---|---|---|
| `identity.DID` | — | NOVO | Formato `did:healthpass:...`. O original usava o nome do utilizador. |
| `identity.Identity` | `digitalID.core.DigitalID` | ADAPTAR | Deixa de ser só `name`+`publicKey`; passa a ter DID, papel e chave. |
| `identity.IdentityRole` | — | NOVO | Enum de papéis (emissor, titular, validador, admin). |
| `identity.KeyManager` | `utils.SecurityUtils` (geração de chaves) | ADAPTAR | Encapsula geração/gestão de pares de chaves. |
| `identity.IdentityService` | `digitalID.core.DigitalIDServices` | ADAPTAR | Registo/consulta por DID e verificação de emissor autorizado. |

## 4. Credenciais (totalmente novo)

| HealthPass | Ação | Notas |
|---|---|---|
| `credential.Credential` | NOVO | Credencial genérica + assinatura. |
| `credential.VaccinationCredential` | NOVO | Certificado de vacinação sintético. |
| `credential.CredentialIssuer` | NOVO | Assina e emite credenciais. |
| `credential.CredentialVerifier` | NOVO | Verificações separadas (assinatura, hash, emissor, estado, validade). |
| `credential.CredentialStatus` | NOVO | Enum `ACTIVE`, `REVOKED`, `EXPIRED`. |
| `credential.CredentialPresentation` | NOVO | Representação para apresentação/exportação. |

## 5. Consenso (totalmente novo — substitui o PoW)

| HealthPass | Origem | Ação | Notas |
|---|---|---|---|
| `consensus.ConsensusEngine` | `core.MinerPOW` (conceito) | ADAPTAR/NOVO | O original tinha PoW, não adequado a rede permissionada. Passa a PoA simplificado. |
| `consensus.Validator` | — | NOVO | Representa um validador autorizado. |
| `consensus.ValidatorRegistry` | — | NOVO | Lista de validadores autorizados. |
| `consensus.BlockProposal` | — | NOVO | Proposta de bloco. |
| `consensus.ValidatorVote` | — | NOVO | Voto assinado de um validador. |
| `consensus.ConsensusCertificate` | — | NOVO | Certificado com os votos (separado do bloco, sem dependência circular). |

## 6. Rede P2P (totalmente novo)

| HealthPass | Origem | Ação | Notas |
|---|---|---|---|
| `network.P2PNode` | — | NOVO | Servidor TCP que aceita ligações de outros nós. |
| `network.PeerManager` | — | NOVO | Gere a lista de peers e o envio de mensagens. |
| `network.NetworkMessage` | — | NOVO | Envelope de mensagem (id, tipo, conteúdo). |
| `network.MessageType` | — | NOVO | Enum `HELLO`, `NEW_TRANSACTION`, `BLOCK_PROPOSAL`, `BLOCK_VOTE`, `NEW_BLOCK`, `GET_CHAIN_STATUS`, `GET_BLOCKS`, `BLOCKS_RESPONSE`. |
| `network.NodeConfig` | — | NOVO | Endereço/porta e peers iniciais do nó. |

## 7. Armazenamento, GUI, configuração e aplicação

| HealthPass | Origem | Ação | Notas |
|---|---|---|---|
| `storage.OffChainCredentialStore` | — | NOVO | Guarda a credencial completa fora da cadeia. |
| `gui.IssuerView` | `digitalID.ui.DigitalIDui` (padrão) | ADAPTAR | Reaproveita o estilo Swing. |
| `gui.HolderWalletView` | `templarCoin.ui.TemplarCoin` (padrão) | ADAPTAR | Reaproveita o estilo Swing. |
| `gui.VerifierView` | — | NOVO | |
| `gui.AdminView` | `templarCoin.ui.TemplarCoin` (explorador) | ADAPTAR | Reaproveita o explorador de blocos. |
| `config.HealthPassConfig` | — | NOVO | Parâmetros (portas, dificuldade/quórum, caminhos). |
| `app.HealthPassApplication` | `templarCoin.ui.TemplarLogin.main` | ADAPTAR | Novo ponto de entrada. |

## 8. Componentes descartados (não vão para o HealthPass)

| Original | Motivo |
|---|---|
| `templarCoin.core.TemplarServices` | Lógica monetária (saldos, moedas). |
| Bloco génesis `MASTER → System 1000` | Emissão monetária. |
| `core.MinerPOW` (mineração) | Recompensa/mineração monetária; substituído por PoA. |
| `MinerPOW.getSigner` | Código morto e seleção ponderada por saldo. |
| GUIs monetárias (parte financeira) | Sem equivalente no HealthPass. |

## 9. Dependências herdadas

- **Conceito de GUI Swing** (sem biblioteca externa).
- **Algoritmos criptográficos** já usados na plataforma Java: `SHA3-256`,
  `RSA`/`SHA3-256withRSA`, `AES/GCM`, `PBKDF2-HMAC-SHA256` (todos mantidos).
- **Nada mais** é herdado. O HealthPass introduz Jackson e JUnit 5.
