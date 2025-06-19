# Back End de Sistema de Gerenciamento de Gráfica (Serverless)

## 📖 Sobre o Projeto

Este é um projeto de estudo focado no desenvolvimento de um sistema de backend para o gerenciamento da linha de produção de uma gráfica. O sistema está sendo projetado para uma **arquitetura serverless**, com o objetivo de ser implantado na **AWS Lambda**. Essa abordagem visa explorar os benefícios de escalabilidade, custo-benefício e manutenção reduzida oferecidos pela computação sem servidor.

A escolha do **Quarkus** é estratégica, pois seu tempo de inicialização ultrarrápido (fast boot) e baixo consumo de memória o tornam ideal para ambientes como o AWS Lambda, minimizando os efeitos de *cold starts*.

O sistema simula o fluxo de trabalho de uma gráfica, permitindo o cadastro de clientes, a criação de pedidos e o gerenciamento dos itens na linha de produção, com rastreamento contínuo do estado de cada pedido.

## ✨ Funcionalidades Principais

-   **Gestão de Clientes:** CRUD completo de clientes.
-   **Gestão de Pedidos:** Criação de novos pedidos associados a um cliente.
-   **Gestão de Itens de Produção:** Cadastro e controle dos itens na linha de produção.
-   **Rastreamento de Status:** Atualização contínua do estado dos pedidos e itens.
-   **Autenticação e Segurança:** Sistema de login seguro utilizando tokens **JWT**.

## 🚀 Tecnologias Utilizadas

Este projeto foi construído com as seguintes tecnologias:

-   **Backend:**
    -   [**Java 21**](https://www.oracle.com/java/): Versão mais recente da linguagem.
    -   [**Quarkus**](https://quarkus.io/): Framework Java otimizado para cloud-native e serverless.
-   **Plataforma de Implantação (Cloud):**
    -   [**AWS Lambda**](https://aws.amazon.com/lambda/): A plataforma de computação serverless da AWS, onde a aplicação será executada sem a necessidade de gerenciar servidores.
-   **Banco de Dados:**
    -   [**PostgreSQL**](https://www.postgresql.org/): Utilizado no ambiente de desenvolvimento local.
-   **Conteinerização (Ambiente Local):**
    -   [**Docker & Docker Compose**](https://www.docker.com/): Para criar um ambiente de desenvolvimento padronizado e isolado.
-   **Autenticação:**
    -   **JWT (JSON Web Tokens):** Para garantir a segurança dos endpoints da API.

---

## 🔧 Instalação e Execução

### Ambiente de Desenvolvimento Local

Para executar e testar o projeto localmente, siga os passos abaixo.

**Pré-requisitos:**
-   JDK 21+
-   Maven 3.8+ ou Gradle 8.5+
-   Docker e Docker Compose

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/seu-usuario/seu-repositorio.git
    cd seu-repositorio
    ```

2.  **Inicie o banco de dados com Docker Compose:**
    ```bash
    docker-compose up -d
    ```

3.  **Execute a aplicação Quarkus em modo de desenvolvimento (com hot-reload):**
    ```bash
    # Usando Maven
    ./mvnw quarkus:dev
    
    # Usando Gradle
    ./gradlew quarkusDev

    # Usando Quarkus CLI
    quarkus dev
    ```

4.  **Pronto!** A aplicação estará rodando em `http://localhost:8080`.

### ☁️ Deploy na AWS Lambda

O Quarkus possui suporte nativo para a criação de funções para a AWS Lambda através de extensões como `quarkus-amazon-lambda-http`.

1.  **Gere o pacote para Lambda:**
    O processo de build, quando configurado corretamente, gera um arquivo `.zip` pronto para o deploy.
    ```bash
    ./mvnw clean package
    ```

2.  **Arquivos Gerados:** O build criará um arquivo `function.zip` no diretório `target/` (Maven) ou `build/` (Gradle), contendo tudo o que é necessário para a execução na AWS.

3.  **Implantação:** O deploy pode ser feito através do Console da AWS, AWS CLI ou, preferencialmente, usando ferramentas de IaC (Infraestrutura como Código) como **AWS SAM** ou **Terraform**.

Para mais detalhes, consulte o guia oficial do Quarkus: [Deploying to AWS Lambda](https://quarkus.io/guides/aws-lambda).

---
