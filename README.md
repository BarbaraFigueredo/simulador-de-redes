# NetSim Java — Simulador Visual de Redes

Um simulador de redes de computadores com interface gráfica, feito em Java. Você monta uma topologia arrastando dispositivos, conecta eles, envia pacotes e vê tudo acontecer animado na tela.

## O que dá pra fazer

- **Adicionar dispositivos** — computadores, roteadores e switches. Cada um com nome e IP.
- **Conectar dispositivos** — clica em dois nós e define a latência (ms) e a largura de banda (Mbps) do enlace.
- **Enviar pacotes** — escolhe origem, destino, conteúdo e TTL. O simulador calcula o melhor caminho e anima o pacote percorrendo a rota na tela.
- **Roteamento automático** — os roteadores montam a tabela de roteamento sozinhos usando o algoritmo de Dijkstra, igual ao que o protocolo OSPF faz em redes reais.
- **Ver estatísticas em tempo real** — pacotes enviados, entregues, perdidos, taxa de entrega e latência média.
- **Salvar e carregar topologias** — exporta e importa como JSON.
- **Remover dispositivos** — ao remover, os enlaces saem junto automaticamente.

## Como rodar

Você precisa ter Java 17+ e Maven instalados.

```bash
# clonar o repositório
git clone <url-do-repo>
cd simulador-redes

# rodar a aplicação
mvn javafx:run

# rodar os testes
mvn test
```

## Tecnologias

| O quê | Por quê |
|---|---|
| Java 17 | LTS estável, records, sealed classes |
| JavaFX 21 | Interface gráfica nativa |
| Jackson | Serializar/deserializar topologia em JSON |
| JUnit 5 + Mockito | Testes unitários (25 testes passando) |
| Maven | Build e gestão de dependências |

## Arquitetura

O projeto segue Clean Architecture dividido em três camadas:

```
domain/          → entidades e regras de negócio (Router, Switch, Packet, Dijkstra...)
application/     → casos de uso (AddDevice, SendPacket, ExportTopology...)
presentation/    → interface gráfica JavaFX (controllers, canvas, animações)
```

A lógica de roteamento não sabe nada de JavaFX. A interface não sabe nada de Dijkstra. Cada camada faz só o que é dela.

## Como o roteamento funciona

Quando você envia um pacote, o simulador usa o **algoritmo de Dijkstra** para encontrar o caminho de menor custo entre origem e destino, levando em conta a latência de cada enlace. Dispositivos inativos são ignorados na busca. Os roteadores também mantêm tabelas de roteamento atualizadas automaticamente a cada mudança na topologia — o mesmo princípio do OSPF.

## Estrutura de pastas

```
src/
  main/java/com/netsim/
    domain/         → entidades (Router, Switch, Computer, Packet, NetworkTopology)
    application/    → use cases, DTOs, eventos, estatísticas
    presentation/   → controllers FXML, canvas, animações, viewmodels
    config/         → AppConfig (injeção de dependências manual)
  main/resources/
    fxml/           → telas (main, dialogs)
    css/            → tema escuro
  test/             → testes unitários
```

## Sobre o projeto

Feito como projeto pessoal pra estudar redes de computadores na prática, juntando teoria (algoritmos de roteamento, modelo OSI, protocolo OSPF) com desenvolvimento Java com uma interface decente.