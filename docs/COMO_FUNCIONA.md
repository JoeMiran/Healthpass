# Como executar e entender o HealthPass

Este guia foi escrito para quem ainda não conhece o projeto. Leia a secção 2
para perceber a ligação com a proposta do relatório. Depois siga os passos da
GUI; cada passo explica o que carregar e o que o programa faz por trás.

Para localizar e estudar as classes, abra também
[Arquitetura do HealthPass](ARQUITETURA.md). Esse documento contém o diagrama de
classes completo, a explicação técnica do blockchain e os caminhos exatos do
código.

## 1. Qual é a ideia do projeto?

Imagine que o Hospital A emite um comprovativo de vacinação e o Hospital B
precisa confirmar que o documento é autêntico e não foi alterado. Se os dois
dependerem de uma única base de dados central, o serviço central controla o
registo e pode ser um ponto único de falha. O HealthPass experimenta outra
abordagem: vários nós autorizados guardam cópias de um registo comum e usam
regras criptográficas para confirmar os eventos.

O documento completo fica fora da blockchain. A cadeia guarda a referência
técnica da credencial, os DIDs e o respetivo hash. O hash funciona como uma
impressão digital: se o conteúdo mudar, o hash calculado já não coincide. A
assinatura digital permite verificar que a credencial foi assinada pela chave
privada do emissor. A blockchain não prova que uma vacinação aconteceu na vida
real; confirma apenas a integridade técnica e o registo feito pelo emissor
configurado.

## 2. Porque a janela não parece igual ao relatório?

O relatório descreve a **arquitetura pretendida**: Hospital A/emissor, titular
com carteira própria, Hospital B/verificador e nós operados por instituições
distintas. A aplicação que existe agora é uma **demonstração simplificada
desse percurso**:

- há uma só janela com separadores chamados **Emissor**, **Titular**,
  **Verificador** e **Rede**;
- a janela usa identidades de demonstração pré-configuradas, sem login ou
  escolha de instituição;
- os três nós TCP são iniciados localmente pela aplicação, na mesma máquina e
  na mesma JVM, embora cada um tenha porta e ficheiro de cadeia próprios;
- o separador Titular mostra a credencial atual em JSON, mas não é ainda uma
  carteira independente que recebe/importa uma credencial;
- o Verificador verifica o mesmo objeto de credencial da sessão. Ainda não há
  transferência real de credencial entre dois computadores;
- os ecrãs antigos `IssuerView`, `HolderWalletView`, `VerifierView` e
  `AdminView` são esqueletos. O painel funcional é `HealthPassDashboard`.

Portanto, os separadores **representam papéis** do relatório; não são quatro
aplicações nem quatro instituições autónomas. O teste
`P2PMultiProcessIntegrationTest` demonstra nós em processos Java separados,
mas ainda na mesma máquina. Consulte a tabela de diferenças no documento de
[Arquitetura](ARQUITETURA.md).

## 3. Preparar e iniciar

Precisa de JDK 21 ou superior e Maven 3.9 ou superior. Abra um terminal na raiz
do projeto `HealthPass` e confirme as instalações:

```bash
java -version
mvn -version
```

Para iniciar a janela gráfica:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication
```

Na primeira execução, Maven pode precisar de Internet para descarregar o
plugin e as dependências. O arranque gera chaves RSA temporárias para a sessão,
cria três blockchains locais com o bloco génesis e regista o emissor e o
titular de demonstração. Aguarde até a janela indicar que a rede está pronta.

Se estiver num terminal sem interface gráfica, inicie o modo de texto:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication -Dexec.args=--cli
```

O modo texto executa o mesmo fluxo de identidade/credencial/rede. Não é preciso
escolher portas: o programa procura portas TCP livres para os três nós.

## 4. Demonstração gráfica, passo a passo

### Passo A — confirmar que a rede começou

Quando aparecer **Rede pronta**, veja a área de registo. Ela mostra o caminho
da sessão e a altura/hash dos nós. Logo após o arranque, os nós devem ter a
mesma altura e o mesmo hash final. A primeira altura contém as duas
transações de registo de identidade (emissor e titular), confirmadas num bloco
por quorum.

### Passo B — emitir como Hospital A

1. Abra o separador **Emissor**.
2. Deixe ou altere o código fictício, por exemplo `VACINA-DEMO-A`.
3. Use uma data no formato `AAAA-MM-DD`. O campo começa preenchido com a data
   atual; pode mantê-la.
4. Carregue **Emitir e confirmar** e aguarde a mensagem de conclusão.

O sistema cria um ID aleatório para a credencial, assina os seus campos com a
chave do emissor, guarda o documento completo num ficheiro local e cria uma
transação on-chain com ID, DIDs e hash. A transação é enviada aos outros nós.
O proponente da altura prepara um bloco; votos RSA dos validadores formam
quorum de dois em três. Depois da confirmação, a altura esperada passa de 1
para 2 e os três nós mostram o mesmo hash final.

Não introduza dados reais de pacientes. A aplicação não é um sistema clínico.

### Passo C — apresentar como titular

1. Abra **Titular**.
2. Carregue **Mostrar apresentação da credencial**.

O JSON apresentado é a credencial completa, incluindo os dados sintéticos e a
assinatura. Nesta demonstração, o titular e o verificador usam o mesmo
programa; o JSON é uma visualização da apresentação, não uma transferência
para outro computador. O documento completo está no armazenamento local
off-chain.

### Passo D — verificar como Hospital B

1. Abra **Verificador**.
2. Carregue **Verificar última credencial**.

Antes da revogação, todas estas verificações devem estar verdadeiras:

| Verificação mostrada | Em palavras simples |
|---|---|
| Assinatura válida | A chave pública do emissor confirma a assinatura RSA. |
| Hash corresponde | O hash do documento apresentado coincide com o hash registado no bloco. |
| Emissor autorizado | O DID do emissor está registado e consta da lista de confiança da demonstração. |
| Credencial ativa | A cadeia não contém revogação válida para esse ID. |
| Dentro da validade | As datas da credencial não expiraram. |

O resultado **VÁLIDA** quer dizer válida tecnicamente segundo estas regras. Não
é uma avaliação clínica nem prova médica independente.

### Passo E — ver estado dos nós

Abra **Rede** e carregue **Atualizar estado dos nós**. Compare altura, hash e
validade da cadeia. Quando todos têm a mesma altura/hash, as réplicas chegaram
ao mesmo estado. Cada nó grava a sua própria cadeia, embora os três estejam
nesta demonstração na mesma máquina.

### Passo F — revogar e verificar outra vez

1. Volte a **Emissor** e carregue **Revogar última**.
2. Aguarde a confirmação e abra **Verificador**.
3. Verifique de novo.

A nova transação de revogação fica num novo bloco (a altura esperada passa para
3). A assinatura e o hash do documento continuam corretos, mas **Credencial
ativa** passa a falso e o resultado global passa a **REJEITADA**. A blockchain
não apaga o evento de emissão; acrescenta o evento que altera o estado.

## 5. O que cada componente está a fazer

1. **DID e identidade:** `did:healthpass:issuer-demo-01` e
   `did:healthpass:holder-demo-01` são identificadores técnicos. Não contêm
   nomes nem números de utente.
2. **Assinatura:** `SHA256withRSA` combina SHA-256 e RSA. O emissor assina com a
   chave privada; o verificador confirma com a chave pública. A chave privada
   não é transmitida pela rede.
3. **Hash:** SHA-256 gera um resumo de tamanho fixo. O mesmo conteúdo gera o
   mesmo resumo; se se alterar um campo, a verificação de integridade falha.
4. **Bloco:** agrupa transações e contém o hash do bloco anterior. Cada bloco
   fica ligado ao que veio antes.
5. **Merkle Tree:** resume as transações de um bloco numa raiz. Alterar uma
   transação muda a raiz e faz o bloco falhar na validação.
6. **P2P:** cada nó envia mensagens diretamente aos peers por TCP. Não existe
   um servidor central a encaminhar todas as mensagens nesta topologia local.
7. **Consenso PoA:** validadores previamente autorizados assinam votos; para
   esta demonstração, dois votos em três permitem aceitar o bloco.
8. **On-chain/off-chain:** a cadeia guarda a referência/hash e eventos; a
   credencial completa fica em ficheiro local. Um hash não torna dados
   pessoais anónimos, por isso a aplicação usa valores fictícios.

O diagrama de sequência, o diagrama de classes e a localização de cada
componente estão em [Arquitetura](ARQUITETURA.md).

## 6. Como usar pelo terminal

Depois de arrancar com `--cli`, escreva um comando por vez:

| Escreva | O que acontece |
|---|---|
| `estado` | Mostra a altura, hash final, pendentes e integridade de cada nó. |
| `emitir` | Pede código fictício e data; emite e confirma uma credencial. |
| `apresentar` | Mostra em JSON a credencial atual da sessão. |
| `verificar` | Mostra o resultado das cinco verificações. |
| `revogar` | Regista e confirma a revogação da credencial atual. |
| `sair` | Fecha os nós e termina sem apagar a sessão guardada. |

Para observar o resultado de revogação, execute `verificar` depois de
`revogar`. Se tentar `apresentar`, `verificar` ou `revogar` antes de emitir,
o programa informa que ainda não existe credencial na sessão.

## 7. Ficheiros criados e testes

Cada início cria uma pasta nova `data/demo/sessions/session-*`, sem apagar
sessões anteriores. Nela:

- `node-1.json`, `node-2.json`, `node-3.json`: cópia persistida da cadeia de
  cada nó;
- `credentials/*.json`: credenciais completas off-chain.

As chaves da demonstração ficam em memória e são efémeras. Embora o projeto
tenha um componente de armazenamento cifrado de chaves, a GUI atual ainda não o
usa. Ao fechar a aplicação, a cadeia/credencial ficam nos ficheiros, mas a
sessão não restaura a identidade e a chave privada antigas. O próximo arranque
cria uma sessão nova.

Para correr todos os testes:

```bash
mvn test
```

A suíte inclui testes de hashes, Merkle, persistência, credenciais, consenso,
rede local e três processos P2P. O teste multi-processo mostra uma rede mais
próxima do requisito de processos independentes do que a janela local.

## 8. Limites que devemos explicar ao professor

- É uma prova de conceito académica, não um sistema hospitalar.
- O consenso PoA 2/3 é simplificado; não oferece garantias BFT formais.
- Os nós da janela são três endpoints TCP, mas correm na mesma JVM/computador.
- Os papéis de emissor, titular e verificador são separadores da mesma sessão,
  não instituições/aplicações isoladas.
- O consenso verifica blocos e votos; não chama toda a lógica de autorização e
  validação de credencial em cada transação recebida.
- A carteira do titular não é persistente nem importa credenciais de terceiros;
  não há QR, gestão visual de emissores ou rotação de chaves na GUI.
- Os certificados de consenso usados para sincronizar blocos entre peers ficam
  em memória; recuperar um nó atrasado depois de todos os peers reiniciarem
  ainda precisa de melhoria.
- O verificador valida assinatura, integridade, emissor, estado e datas. Não
  decide se a informação médica é verdadeira.
