# Arquitetura do HealthPass

> **Estado:** esqueleto inicial. Este documento será completado à medida que
> cada camada for implementada. Para já, descreve a organização por camadas e a
> responsabilidade de cada package.

## 1. Visão geral (camadas)

```
+---------------------------------------------------------------+
|  gui/            Interfaces (Emissor, Titular, Verificador, Admin) |
+---------------------------------------------------------------+
|  app/            Arranque e ligacao das partes                     |
+---------------------------------------------------------------+
|  identity/       DIDs, chaves e registo de identidade             |
|  credential/     Emissao, verificacao e revogacao de credenciais  |
+---------------------------------------------------------------+
|  consensus/      Proof of Authority (propostas, votos, quorum)    |
|  network/        Rede P2P (sockets TCP + mensagens JSON)          |
+---------------------------------------------------------------+
|  ledger/         Blocos, transacoes, Merkle Tree, cadeia          |
+---------------------------------------------------------------+
|  crypto/         Hash, assinaturas, serializacao canonica         |
|  storage/        Persistencia local (credenciais, chaves)         |
|  config/         Parametros de configuracao                       |
+---------------------------------------------------------------+
```

Regra de ouro: **as camadas de cima dependem das de baixo, nunca o contrário.**
A GUI não contém lógica criptográfica nem de consenso; chama serviços.

## 2. Responsabilidade de cada package

| Package | Responsabilidade |
|---|---|
| `app` | Ponto de entrada; junta configuração, nós e serviços. |
| `identity` | Criar/registar/consultar identidades (DID + chave pública + papel). |
| `credential` | Criar, assinar, verificar e revogar credenciais. |
| `crypto` | Hash (`SHA3-256`), assinatura (`SHA3-256withRSA`), JSON canónico. |
| `ledger` | Blocos, transações, Merkle Tree, validação e persistência da cadeia. |
| `consensus` | PoA simplificado: proposta de bloco, votos assinados e quórum. |
| `network` | Comunicação P2P por sockets TCP com mensagens JSON. |
| `storage` | Guardar credenciais (off-chain) e gerir chaves em ficheiro. |
| `gui` | As 4 interfaces gráficas Swing. |
| `config` | Parâmetros (portas, quórum, caminhos de ficheiros). |

## 3. Relação entre componentes (a completar)

- `identity.IdentityService` cria transações `IDENTITY_REGISTERED` no `ledger`.
- `credential.CredentialIssuer` assina e cria transações `CREDENTIAL_ISSUED`.
- `consensus.ConsensusEngine` recolhe votos e produz um `ConsensusCertificate`.
- `network.P2PNode` propaga transações e blocos entre nós.

## 4. Fluxo de emissão e verificação (a completar)

1. O emissor cria uma `VaccinationCredential` com dados sintéticos.
2. Assina-a com a sua chave privada (`SignatureService`).
3. Calcula o hash (`HashUtil`) e cria uma transação on-chain.
4. A transação é propagada (`network`) e incluída num bloco.
5. O bloco é validado e aprovado por quórum (`consensus`).
6. O verificador valida assinatura, hash, emissor, estado e validade.

## 5. Separação on-chain / off-chain (a completar)

- **On-chain:** id da credencial, DIDs, hash SHA-256, versão da chave do
  emissor, estado, timestamp.
- **Off-chain:** credencial completa e dados sintéticos de vacinação.
