# Demonstração do HealthPass

## 1. A ideia

O cenário é este: o **Hospital A** emite uma credencial fictícia de vacinação
para um **titular**. Mais tarde, o titular apresenta essa credencial ao
**Hospital B**, que verifica se foi assinada por um emissor confiável, se o
conteúdo não foi alterado, se continua ativa e se não expirou.

O documento completo da credencial fica fora da blockchain, num ficheiro local
da sessão. A blockchain guarda o identificador, os DIDs do emissor e do
titular, o hash da credencial e os eventos técnicos de emissão e revogação. O
hash é uma impressão digital do documento: uma alteração ao conteúdo produz
outro hash. A assinatura digital permite confirmar que o emissor configurado
assinou a credencial.

A aplicação inicia três nós TCP que replicam a cadeia. Um bloco é confirmado
quando há votos suficientes dos validadores: neste protótipo, são necessários
dois votos em três. Assim, o Hospital B pode comparar a credencial apresentada
com um registo replicado, em vez de confiar apenas numa cópia local do emissor.

**O que o sistema prova:** certas propriedades técnicas do documento e do
registo. **O que não prova:** que a vacinação ocorreu na realidade ou que é
clinicamente adequada.

## 2. O que é a aplicação e o que não é

A GUI tem cinco áreas, acessíveis pela navegação à esquerda:

| Área | Papel representado | Para que serve |
|---|---|---|
| **Visão geral** | Apresentação do fluxo | Resumo da sessão, estado da credencial, altura da cadeia e registo de atividade. |
| **Emissor · Hospital A** | Emissor | Preencher dados fictícios, emitir e assinar a credencial, ou revogá-la. |
| **Carteira · Titular** | Titular | Ver a credencial da sessão, preparar a apresentação JSON e copiá-la. |
| **Verificação · Hospital B** | Verificador | Colar a apresentação recebida e ver o resultado de cada validação. |
| **Rede e validadores** | Administrador técnico / validadores | Consultar altura, pendências, validade e hash final dos três nós. |

Estas áreas são papéis de uma só aplicação e partilham a mesma sessão de
demonstração. O JSON é copiado e colado localmente para representar a passagem
da credencial do titular ao verificador. Não há ainda duas aplicações,
utilizadores autenticados, hospitais independentes ou transferência entre
computadores. Os três nós da GUI também correm na mesma máquina e JVM, embora
tenham portas e ficheiros de cadeia próprios.

O relatório `computacao_distribuida.pdf` descreve o objetivo arquitetural mais
completo. A GUI demonstra o fluxo principal desse objetivo, mas não todas as
funcionalidades previstas para um produto real. Não use dados de pacientes.

## 3. Preparação e arranque

Requisitos: JDK 21 ou superior e Maven 3.9 ou superior. Na pasta do projeto:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication
```

No arranque, a aplicação cria uma sessão nova, gera chaves temporárias,
inicia três nós TCP e regista o emissor e o titular. Aguarde até o indicador
superior mostrar **REDE CONVERGENTE · 3 NÓS**. A atividade da Visão geral deve
mostrar que as identidades de demonstração foram registadas.

O primeiro bloco útil fica na **altura 1**. Depois da emissão, espera-se a
altura **2**; depois da revogação, a altura **3**. Todos os nós devem apresentar
a mesma altura e o mesmo hash final. Uma sessão nova é criada em cada arranque;
as chaves ficam em memória e não são reutilizadas na próxima execução.

## 4. Roteiro curto para apresentar o projeto

Pode começar com esta explicação:

> “O HealthPass é um protótipo académico de identidade digital para saúde. Vou
> mostrar como um emissor assina uma credencial fictícia, como o titular a
> apresenta e como outro papel verifica a assinatura, a integridade e o estado
> da credencial usando um registo replicado por três nós. A interface reúne os
> papéis numa única máquina para fins de demonstração.”

Depois percorra as áreas pela ordem abaixo. Diga o que está a fazer e aponte o
resultado visível antes de passar ao ecrã seguinte.

## 5. Demonstração passo a passo

### Ecrã 1 — Visão geral: explicar a sessão

**O que se vê:**

- **Credencial da sessão:** fica sem valor até se emitir a primeira credencial;
- **Estado atual:** começa como “SEM CREDENCIAL”, depois indica “ATIVA” ou
  “REVOGADA”;
- **Altura da cadeia:** número do bloco mais recente;
- **Percurso da credencial:** atalhos para emissor, titular, verificador e rede;
- **Atividade da sessão:** mensagens sobre as operações concluídas.

**O que dizer:**

> “A rede arranca com três validadores. O primeiro bloco útil regista as
> identidades de demonstração do emissor e do titular. A altura e o hash são
> indicadores da versão da cadeia; se os três nós coincidirem, as réplicas
> estão convergentes.”

Não é necessário começar por ler o hash completo. Explique que é um resumo
criptográfico e mostre a igualdade entre os nós mais tarde, no ecrã da Rede.

### Ecrã 2 — Emissor · Hospital A: criar e assinar

**Campos do ecrã:**

- **Código fictício da vacina:** texto de demonstração, inicialmente
  `VACINA-DEMO-A`;
- **Data de administração:** data sintética no formato `AAAA-MM-DD`;
- **DID do emissor e DID do titular:** identificadores técnicos sem nome ou
  número de utente;
- **Emitir e confirmar na rede:** cria a credencial e submete a referência à
  rede;
- **Revogar credencial:** regista um evento de revogação para a credencial
  atual;
- **Resultado da emissão:** mostra o identificador criado.

**Demonstração:** mantenha os valores de exemplo e carregue **Emitir e
confirmar na rede**. Aguarde a operação terminar.

**O que acontece:** o sistema cria um identificador único, inclui os campos da
credencial, assina-os com a chave privada temporária do emissor e guarda o
documento completo em `credentials/` dentro da sessão. Depois calcula o hash,
cria uma transação com os dados técnicos mínimos, propaga-a aos nós e pede a
confirmação de um bloco por quorum 2/3.

**O que esperar:** o resultado mostra o ID `cred-…`; na Visão geral o estado
passa a **ATIVA** e a altura deve passar de 1 para 2. Na área de atividade
aparece a confirmação da emissão.

**O que dizer:**

> “O Hospital A assina a credencial antes de a registar. O ficheiro completo
> fica off-chain. A blockchain recebe uma referência e um hash, não o
> documento de vacinação completo. Os validadores confirmam a transação por
> quorum.”

### Ecrã 3 — Carteira · Titular: preparar a apresentação

**O que se vê:** resumo da credencial guardada nesta sessão, botão
**Preparar apresentação**, botão **Copiar JSON** e a representação JSON.

Carregue **Preparar apresentação**. A área mostra o JSON da credencial
assinada. Carregue **Copiar JSON** para o passar à área de transferência.

**O que dizer:**

> “O titular apresenta o documento que recebeu. Nesta versão, a carteira é
> simulada dentro da mesma aplicação: o JSON representa a informação que seria
> partilhada com o Hospital B.”

O JSON contém a assinatura e os campos da credencial. Não o confunda com um
registo on-chain; ele é a apresentação off-chain do titular.

### Ecrã 4 — Verificação · Hospital B: validar o documento apresentado

No painel **Credencial recebida**, carregue **Colar da área de transferência**
(ou cole o texto JSON). Em seguida, carregue **Verificar apresentação
recebida**.

O painel mostra um resultado global e cinco verificações independentes:

| Verificação | O que confirma | Resultado esperado antes da revogação |
|---|---|---|
| Assinatura do emissor | A assinatura corresponde à chave pública registada do emissor. | Confirmado |
| Hash corresponde ao registo | O conteúdo apresentado tem o mesmo hash da emissão registada. | Confirmado |
| Emissor autorizado | O DID e a chave do emissor são confiáveis nesta demonstração. | Confirmado |
| Credencial ativa | Existe emissão e não existe revogação válida para este ID. | Confirmado |
| Dentro do prazo de validade | A data atual está entre a emissão e a expiração. | Confirmado |

Antes de revogar, o resultado global deve ser **CREDENCIAL VÁLIDA**. Explique
que “válida” quer dizer tecnicamente válida segundo estas cinco regras, não
que o sistema comprovou um facto clínico.

**Pequena experiência opcional de integridade:** antes de revogar, altere no
JSON recebido o valor de `vaccineCode` e verifique novamente. A assinatura e o
hash deverão falhar, pelo que o resultado será **REJEITADA**. Depois cole outra
vez o JSON original copiado da carteira para continuar a demonstração. Não
altere os outros campos se quiser manter o teste simples.

### Ecrã 5 — Rede e validadores: mostrar a replicação

**O que se vê:** total de validadores, regra de consenso PoA 2/3, altura atual e
texto técnico dos três nós, incluindo porta, pendentes, validade da cadeia e
hash final. O botão **Atualizar estado dos nós** volta a consultar o estado.

Após a emissão, confirme que A, B e C têm altura 2, zero transações pendentes,
cadeia válida e o mesmo hash final.

**O que dizer:**

> “Cada nó mantém uma cópia da cadeia. A emissão foi propagada e confirmada; a
> coincidência da altura e do hash final mostra que estas três réplicas
> chegaram ao mesmo estado. Nesta interface, os nós são locais e não
> representam três hospitais ligados pela Internet.”

Se uma linha não coincidir imediatamente, atualize o estado e aguarde a
propagação terminar.

### Ecrãs 6 e 7 — Revogar e verificar novamente

Volte a **Emissor · Hospital A** e carregue **Revogar credencial**. A aplicação
acrescenta um evento de revogação à cadeia; não apaga a emissão nem modifica a
assinatura da credencial. A altura deve passar de 2 para 3.

Volte a **Verificação · Hospital B**. O JSON original deve continuar no painel
de entrada. Carregue **Verificar apresentação recebida** outra vez.

**Resultado esperado:** assinatura, hash, autorização e validade continuam
confirmados; **Credencial ativa** falha; o resultado global passa a
**REJEITADA**. Na Visão geral, o estado também deve indicar **REVOGADA**.

**O que dizer:**

> “Revogar não apaga o histórico. A rede regista um novo evento. Por isso, o
> documento ainda tem uma assinatura e um hash corretos, mas já não é aceite
> porque o estado consultado na cadeia indica revogação.”

## 6. Vocabulário para responder a perguntas

- **DID:** identificador descentralizado. Neste protótipo identifica os papéis
  sem embutir dados pessoais, por exemplo `did:healthpass:issuer-demo-01`.
- **Chave privada:** usada pelo emissor para assinar. Fica em memória na sessão
  e não é enviada aos validadores.
- **Chave pública:** permite verificar a assinatura e está associada à
  identidade registada.
- **Assinatura:** prova criptográfica de que os dados assinados correspondem à
  chave privada do emissor. Não prova que os dados clínicos são verdadeiros.
- **Hash SHA-256:** resumo determinístico usado para detetar alterações e
  comparar a apresentação com a referência registada.
- **Bloco e cadeia:** um bloco agrupa transações e aponta para o hash do bloco
  anterior. A alteração de um bloco quebra a validação da cadeia.
- **Árvore de Merkle:** resume as transações de um bloco numa raiz que pode ser
  recalculada para detetar alterações.
- **P2P:** os nós enviam mensagens diretamente aos peers por TCP; não há um
  servidor central a encaminhar as mensagens nesta topologia de demonstração.
- **PoA, quorum 2/3:** validadores previamente conhecidos assinam votos. Dois
  votos em três são suficientes para aceitar um bloco neste modelo simplificado.
- **On-chain / off-chain:** a blockchain guarda referências e eventos técnicos;
  a credencial completa fica em ficheiro local.

## 7. Fecho recomendado para a apresentação

Pode terminar assim:

> “O protótipo demonstra o ciclo técnico de emissão, apresentação, verificação
> e revogação, usando assinatura digital, SHA-256, blockchain, Merkle, quorum
> e comunicação P2P. A interface representa os papéis do cenário, mas ainda é
> uma demonstração local: não tem carteiras independentes, hospitais remotos ou
> integração clínica. A rede confirma a integridade e o estado registado, não
> a verdade médica da informação.”

## 8. Execução pelo terminal

Para iniciar sem janela gráfica:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication -Dexec.args=--cli
```

Escreva um comando por vez:

| Comando | Ação |
|---|---|
| `estado` | Mostra altura, hash, pendências e validade das cadeias. |
| `emitir` | Pede código fictício e data, depois emite e confirma a credencial. |
| `apresentar` | Mostra a credencial atual em JSON. |
| `verificar` | Mostra o resultado das cinco verificações da credencial atual. |
| `revogar` | Regista e confirma a revogação da credencial atual. |
| `sair` | Fecha os nós sem apagar os ficheiros da sessão. |

## 9. Ficheiros, execução de testes e limites

Cada arranque cria uma pasta `data/demo/sessions/session-*`, sem apagar as
anteriores. Inclui `node-1.json`, `node-2.json` e `node-3.json` (cadeias dos
nós) e `credentials/*.json` (credenciais completas off-chain). As chaves são
temporárias e ficam em memória; a sessão não é restaurada após fechar a GUI.

Para executar a suíte automatizada, use `mvn test`. Há testes de hash, Merkle,
persistência, credenciais, consenso, rede e nós em processos separados.

Ao apresentar, seja claro sobre estes limites:

- é uma prova de conceito académica, não um sistema clínico nem certificado
  para produção;
- os três nós gráficos correm na mesma máquina e JVM;
- emissor, titular e verificador são áreas de uma única sessão, não aplicações
  isoladas; a apresentação JSON é passada localmente;
- o consenso PoA é simplificado e não oferece garantias BFT formais;
- a carteira não é independente nem persistente, não tem QR e não importa
  credenciais após reiniciar a sessão;
- os validadores não executam toda a lógica de negócio da credencial;
- o resultado de verificação é técnico e não confirma a ocorrência de um ato
  médico.
