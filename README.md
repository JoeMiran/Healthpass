# HealthPass

Protótipo académico de **identidade digital descentralizada aplicada à saúde**.
A demonstração usa credenciais fictícias de vacinação para mostrar emissão,
assinatura, registo numa blockchain local replicada, apresentação, verificação e
revogação.

## O que a aplicação faz

Ao iniciar, prepara três nós TCP locais, regista um emissor e um titular de
demonstração, e abre uma interface com os separadores **Emissor**, **Titular**,
**Verificador** e **Rede**. Pode emitir uma credencial sintética, ver a sua
apresentação JSON, validar assinatura/hash/estado e observar a revogação. Os
nós mantêm cadeias persistidas separadas e usam PoA simplificado com quorum de
2 em 3.

O projeto adapta conceitos e partes do núcleo blockchain da TemplarCoin para
identidade e credenciais; a arquitetura explica o que foi reaproveitado e o que
foi acrescentado para o HealthPass.

É uma prova de conceito, não um sistema clínico. Os três nós da aplicação
gráfica correm na mesma máquina e JVM; os separadores simulam os papéis da
proposta, não são aplicações hospitalares independentes. Use apenas dados
fictícios. A credencial completa fica em ficheiro local off-chain; a blockchain
guarda referências e eventos técnicos. As limitações estão explicadas nos
documentos abaixo.

## Como executar

Requisitos: JDK 21 ou superior e Maven 3.9 ou superior. Abra um terminal na
pasta do projeto e execute:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication
```

Para iniciar a versão de terminal em vez da janela gráfica:

```bash
mvn exec:java -Dexec.mainClass=pt.healthpass.app.HealthPassApplication -Dexec.args=--cli
```

Na primeira execução, Maven pode precisar de Internet para obter dependências.
Os dados de cada demonstração são criados em `data/demo/sessions/`; o programa
não apaga sessões anteriores.

Para validar o projeto com testes:

```bash
mvn test
```

## Documentação

- [Como executar e entender a aplicação](docs/COMO_FUNCIONA.md): tutorial de
  utilização passo a passo, o significado de cada separador e as diferenças
  entre a proposta do relatório e a demonstração atual.
- [Arquitetura do HealthPass](docs/ARQUITETURA.md): diagrama de classes,
  blockchain, SHA-256, Merkle, consenso, P2P e localização das implementações
  no código.

## Tecnologias

- Java 21 e Maven
- Jackson para JSON
- SHA-256 para hashes e `SHA256withRSA` para assinaturas
- Blockchain própria, árvore de Merkle, PoA simplificado e comunicação TCP P2P
- Swing para a interface gráfica
