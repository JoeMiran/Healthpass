# Guia de execução do HealthPass

> **Estado:** esqueleto inicial. As secções de execução de nós e demonstração
> serão preenchidas quando a PoC estiver implementada. Para já, constam os
> comandos de build que já funcionam.

## 1. Requisitos

- JDK 21 ou superior (`java -version`).
- Apache Maven 3.9+ (`mvn -version`).
- Ligação à Internet na primeira compilação (para descarregar Jackson e JUnit).

## 2. Compilar

A partir da pasta `HealthPass`:

```bash
mvn clean compile
```

O código compilado fica em `target/classes`.

## 3. Executar os testes

```bash
mvn test
```

(Os testes serão adicionados nas etapas seguintes; a pasta `src/test/java`
já existe.)

## 4. Gerar o pacote (jar)

```bash
mvn package
```

O ficheiro `.jar` é criado em `target/`.

## 5. Iniciar os nós (a preencher)

Topologia de demonstração prevista:

| Nó | Endereço | Papel |
|---|---|---|
| A | `127.0.0.1:5001` | Hospital A |
| B | `127.0.0.1:5002` | Hospital B |
| C | `127.0.0.1:5003` | Validador adicional |

> Os comandos concretos de arranque serão adicionados quando `network.P2PNode`
> e `app.HealthPassApplication` estiverem implementados.

## 6. Testar a comunicação P2P (a preencher)

## 7. Demonstração completa (a preencher)

1. Inicializar a blockchain.
2. Criar/carregar identidades de demonstração.
3. Inicializar três nós e estabelecer comunicação P2P.
4. Emitir uma credencial fictícia, assinar e propagar.
5. Validar proposta e consenso.
6. Confirmar a mesma cadeia nos três nós.
