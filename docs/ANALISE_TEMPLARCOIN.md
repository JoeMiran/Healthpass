# Análise da TemplarCoin (projeto original)

> Documento de apoio à migração. Descreve **o que existe efetivamente** no
> código-fonte da TemplarCoin e o que pode ser reaproveitado no HealthPass.
>
> Regra seguida: não assumir que uma funcionalidade existe só porque existe
> uma classe com esse nome. Tudo o que aqui está foi confirmado por leitura do
> código.

## 1. Contexto do projeto original

- **Nome:** Templar2026
- **Linguagem:** Java
- **Versão alvo no original:** `javac.source=26` / `javac.target=26`
  (`nbproject/project.properties`). O ambiente só tem **JDK 25**, logo o
  original **não compila aqui** sem JDK 26.
- **Sistema de build:** NetBeans **Ant** (`build.xml` + pasta `nbproject/`).
  O Ant **não está instalado** na máquina.
- **Dependências externas:** apenas `libs.absolutelayout` (biblioteca do
  NetBeans, usada pela GUI). Sem Maven/Gradle.
- **Ponto de entrada:** `templarCoin.ui.TemplarLogin`.
- **Repositório Git:** a pasta Templar2026 **não está versionada** (o repositório
  Git existente está em `/home/joemiran`).

## 2. Estrutura de diretórios e pacotes

```
Templar2026/
├── src/
│   ├── core/                     # nucleo da blockchain
│   │   ├── Block.java
│   │   ├── BlockChain.java
│   │   └── MinerPOW.java
│   ├── templarCoin/
│   │   ├── core/                 # regras da criptomoeda
│   │   │   ├── Transaction.java
│   │   │   └── TemplarServices.java
│   │   └── ui/                   # interface grafica da moeda
│   │       ├── TemplarCoin.java
│   │       └── TemplarLogin.java
│   ├── digitalID/
│   │   ├── core/                 # registo de identidade na cadeia
│   │   │   ├── DigitalID.java
│   │   │   └── DigitalIDServices.java
│   │   └── ui/
│   │       └── DigitalIDui.java
│   ├── utils/
│   │   ├── SecurityUtils.java    # hash, RSA, AES, assinaturas
│   │   └── Conversion.java       # serializacao Java <-> Base64
│   └── multimedia/               # imagens/gifs da GUI
├── test/                         # VAZIA (sem testes)
├── nbproject/                    # configuracao NetBeans/Ant
├── build.xml
├── blockchain.bc                 # a cadeia persistida (serializacao Java)
├── MASTER.priv / MASTER.pub      # pares de chaves RSA dos utilizadores
├── System.priv / System.pub
├── jose.priv / jose.pub
├── manso.priv / manso.pub
├── manuel.priv / manuel.pub
└── pedro.priv / pedro.pub
```

## 3. Componentes identificados e funcionalidades implementadas

### 3.1. `core.Block`
Bloco da cadeia. Campos: `previousHash`, `data` (texto), `dificulty`,
`nonce`, `currentHash`. Método `isValid()` verifica que o `currentHash`
começa com N zeros **e** que o hash recalculado corresponde ao guardado.

- **Limitações:** não tem índice/altura, nem timestamp, nem Merkle Root, nem
  lista de transações (guarda apenas um `data` textual). `isValid()` valida um
  bloco isolado, não a ligação ao bloco anterior.

### 3.2. `core.BlockChain`
Lista `ArrayList<Block>`. Construtor lê a cadeia do ficheiro `blockchain.bc`
via `ObjectInputStream`; se falhar, cria o bloco génesis. `addData(String)`
cria um bloco, mina (PoW) e persiste toda a lista no ficheiro.

- **Limitações:** persistência embutida na própria classe; sem validação da
  cadeia; sem pool de transações pendentes; sem Merkle Root; sem rede.

### 3.3. `core.MinerPOW`
Prova de trabalho (Proof of Work) multi-thread. `getNonceP(data, dif)` lança
uma thread por core da CPU à procura de um nonce cujo hash comece por N zeros.
Contém ainda `getSigner(BlockChain)` — uma **seleção aleatória ponderada pelo
saldo** que está **código morto** (não é chamada em lado nenhum).

### 3.4. `templarCoin.core.Transaction`
Transação **monetária**: `from`, `to`, `value`, `pkFrom`, `signature`. O
construtor com `PrivateKey` carrega a chave pública do remetente do ficheiro
`.pub`, assina `from+to+value` e guarda a assinatura.

- **Limitações:** não valida a assinatura no construtor nem ao entrar na
  cadeia (a validação só acontece no `toString`, para exibição).

### 3.5. `templarCoin.core.TemplarServices`
`getAmount` (saldo por soma de transações), `getUsers` e `getUsersCoins`.
**Lógica exclusivamente monetária.**

### 3.6. `utils.SecurityUtils`
- **Hash:** `SHA3-256`, codificado em Base64 (`getHash`).
- **Chaves:** RSA (o registo usa `generateKeyPair(3096)`), chave pública em
  X.509 (`.pub`) e privada PKCS#8 cifrada (`.priv`).
- **Assinatura:** `SHA3-256withRSA` (`sign` / `verifySign`).
- **Cifra simétrica:** AES/GCM/NoPadding com chave derivada por
  PBKDF2-HMAC-SHA256 (600 000 iterações, salt de 16 bytes, IV de 12 bytes).

### 3.7. `utils.Conversion`
Serializa objetos Java para Base64 e vice-versa (`ObjectOutputStream` /
`ObjectInputStream`).

- **Risco de segurança:** usa desserialização Java (`readObject`) sobre dados
  que vêm da cadeia. A missão do HealthPass proíbe este padrão na rede P2P.

### 3.8. `digitalID.core.DigitalID` / `DigitalIDServices`
**Precursor do módulo de identidade.** `DigitalID` guarda `name`,
`publicKey` (Base64) e `signature`. `DigitalIDServices.getPublicKey(name, bc)`
percorre a cadeia à procura do registo com esse nome e devolve a chave pública.

- **Limitação:** identifica o utilizador por **nome** (não por DID), e não
  valida assinaturas nem autorização do emissor.

### 3.9. Interfaces gráficas (Swing)
`TemplarLogin` (login/registo, gera chaves RSA), `TemplarCoin` (carteira,
envio de transações, explorador de blocos) e `DigitalIDui` (registo de ID e
consulta de chave pública). Todas contêm **lógica de negócio misturada com a
interface** (ex.: verificação de saldo dentro do handler do botão).

## 4. Componentes que podem ser reutilizados diretamente

| Componente original | Reutilização no HealthPass |
|---|---|
| `utils.SecurityUtils.getHash` | Base do `crypto.HashUtil` (hash determinístico) |
| `utils.SecurityUtils.sign/verifySign` | Base do `crypto.SignatureService` |
| `utils.SecurityUtils` (AES/PBKDF2, IO de chaves) | Base do `storage.KeyStoreService` |
| `core.Block` (fórmula de hash) | Base do `ledger.Block` (mantendo a fórmula determinística) |
| `digitalID.core.DigitalID` | Base do `identity.Identity` / `identity.DID` |
| Padrão de GUI Swing | Base das 4 views do HealthPass |

## 5. Componentes que precisam de adaptação

| Original | Adaptação necessária |
|---|---|
| `core.Block` | Adicionar `height`, `timestamp`, `merkleRoot`, lista de `Transaction`, `validatorId` |
| `core.BlockChain` | Renomear para `Blockchain`; retirar génesis monetário; adicionar pool de pendentes, validação da cadeia, gancho de consenso, delegação de persistência |
| `Transaction` | Substituir `from/to/value` por `type` + `payload` + assinaturas (identidade/credenciais) |
| `Conversion` | Substituir serialização Java por **JSON canónico determinístico** (Jackson) |
| `DigitalIDServices` | Passar a identificar por **DID**, validar assinatura e autorização |

## 6. Componentes específicos de criptomoeda que NÃO fazem sentido no HealthPass

- Saldos e carteiras (`TemplarServices.getAmount`, `getUsersCoins`).
- Bloco génesis `MASTER -> System, 1000`.
- Mineração e recompensa monetária (`MinerPOW`).
- Taxas, emissão de tokens e transferências financeiras.
- Seleção aleatória ponderada por saldo (`getSigner`).

## 7. Componentes que NÃO existem no original (têm de ser construídos)

Confirmado por pesquisa no código: **não existe** implementação de:

- **Rede P2P** (sem sockets, sem `PeerManager`, sem `NetworkMessage`).
  Cada instância lê/escreve o mesmo `blockchain.bc` local.
- **Merkle Tree** (nem Merkle Root).
- **Motor de consenso** (só PoW; não há votos, quórum, validadores).
- **Repositório de cadeia** (a persistência está dentro do `BlockChain`).
- **Módulo de credenciais** (não existe nada).
- **Testes** (pasta `test/` vazia).

> Consequência para o planeamento: cerca de **70%** da arquitetura pedida
> (Merkle, consenso PoA, P2P, credenciais) é **trabalho novo**, e não uma
> migração. Os restantes **~30%** (blocos, transação, criptografia, registo de
> identidade) é que são efetivamente reaproveitados.

## 8. Dependências necessárias

- **JDK 21+** (HealthPass usa Java 21 LTS).
- **Apache Maven** (build).
- **Jackson databind** (JSON).
- **JUnit 5** (testes).
- Nenhuma dependência herdada da TemplarCoin além do conceito de GUI Swing
  (que faz parte da plataforma Java, sem biblioteca externa).

## 9. Problemas encontrados no código original (a corrigir no HealthPass)

1. **Desserialização insegura:** `Conversion` usa `readObject` sobre dados da
   cadeia. → No HealthPass, usar JSON validado.
2. **Sem validação da cadeia:** não há verificação encadeada de `previousHash`.
3. **Transações não verificadas:** assinatura não é validada ao entrar na
   cadeia.
4. **Lógica de negócio na GUI:** verificação de saldo no handler do botão.
5. **Código morto:** `MinerPOW.getSigner`.
6. **Persistência acoplada:** `BlockChain` escreve diretamente no ficheiro.
7. **Sem testes:** impossível garantir regressões.

## 10. Plano de migração (resumo)

1. Copiar/adaptar o núcleo (`Block`, `BlockChain`→`Blockchain`, `Transaction`)
   para o package `ledger`, mantendo a fórmula de hash.
2. Dividir `SecurityUtils` em `crypto.HashUtil`, `crypto.SignatureService` e
   `storage.KeyStoreService`.
3. Substituir `Conversion` por `crypto.CanonicalSerializer` (JSON).
4. Evoluir `DigitalID` para `identity` (DID + Identity + IdentityService).
5. Construir de raiz: `ledger.MerkleTree`, `ledger.ChainRepository`,
   `consensus.*`, `network.*`, `credential.*`, `storage.OffChainCredentialStore`,
   `gui.*`.
6. Descartar toda a lógica monetária.

O detalhe ficheiro-a-ficheiro está em `docs/MIGRACAO_TEMPLARCOIN.md`.
