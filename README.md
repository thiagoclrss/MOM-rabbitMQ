# Projeto MOM - Contagem Distribuída de Palavras-Chave

O sistema consiste em um workflow distribuído baseado em **MOM (Message-Oriented Middleware)** para leitura paralela de arquivos de texto e contagem reativa de palavras-chave em tempo real utilizando **RabbitMQ** e **JavaFX**.

---

## Arquitetura do Sistema

O fluxo de processamento segue a arquitetura orientada a mensagens proposta:

```
[ Cliente (UI) ] 
       │
       ├── (Thread 1: Linhas Pares)   ──┐
       │                                ├──> [ Fila: queue_lines ] (RabbitMQ)
       └── (Thread 2: Linhas Ímpares) ──┘           │
                                                    ├──> [ Workers: 3-5 Threads ]
                                                    │           │
[ Dashboard (UI) ] <── [ Subscrição Dinâmica ] <────┴───────────┴─> [ Tópico: topic_words ]
```

1. **Cliente (1):** Solicita o arquivo de entrada e as palavras-chave via interface gráfica JavaFX.
2. **Leitores de Linhas (2):** Instancia duas threads simultâneas:
   - **Leitor Par:** Lê apenas as linhas de índice par.
   - **Leitor Ímpar:** Lê apenas as linhas de índice ímpar.
3. **Fila de Linhas (3):** As linhas lidas são produzidas para a fila `queue_lines` no broker RabbitMQ.
4. **Contadores de Palavras / Workers (4):** Múltiplas instâncias concorrentes (`concurrency = 3-5`) consomem as linhas da fila, realizam a contagem de frequência de cada palavra-chave e publicam os resultados parciais.
5. **Tópico de Palavras (5):** As contagens parciais são publicadas na *Exchange* do tipo `topic` (`topic_words`) com a routing key `words.count`.
6. **Dashboard (6):** Atua como *Subscriber* do tópico criando uma fila exclusiva temporária e atualiza reativamente a interface gráfica (`TableView`) à medida que as mensagens chegam.

---

## Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4 (`spring-boot-starter-amqp`, `spring-boot-starter-json`).
- **Broker de Mensageria:** RabbitMQ 3 (via Docker Compose).
- **Interface Gráfica:** JavaFX 21.
- **Build Tool:** Apache Maven.

---

## Como Executar o Projeto

### Pré-requisitos
- JDK 21 instalado e configurado nas variáveis de ambiente.
- Docker e Docker Compose instalados.
- Maven instalado.

### 1. Iniciar o RabbitMQ Broker
Na raiz do projeto, onde está o arquivo `docker-compose.yml`, execute:

```bash
docker-compose up -d
```

Você pode acompanhar as filas e exchanges criadas acessando a interface de gerenciamento do RabbitMQ:
- **URL:** [http://localhost:15672](http://localhost:15672)
- **Usuário:** `admin`
- **Senha:** `admin`

### 2. Compilar o Projeto e Gerar o Executável 
Para gerar o pacote `.jar` executável:

```bash
mvn clean package
```

O executável será gerado na pasta `target`.

### 3. Executar a Aplicação
Você pode rodar diretamente via Maven:

```bash
mvn spring-boot:run
```

Ou rodar o arquivo `.jar` compilado:

```bash
java -jar target/mom-0.0.1-SNAPSHOT.jar
```

---