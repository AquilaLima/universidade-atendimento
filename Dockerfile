# ================================
# ETAPA 1 - COMPILAÇÃO
# ================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom primeiro para aproveitar o cache do Docker
COPY pom.xml .

# Baixa as dependências
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests


# ================================
# ETAPA 2 - EXECUÇÃO
# ================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=build /app/target/universidade-atendimento-0.0.1-SNAPSHOT.jar app.jar

# Porta utilizada pelo Render
EXPOSE 10000

# Inicia o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]