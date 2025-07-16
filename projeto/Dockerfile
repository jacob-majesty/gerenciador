# Imagem base com Java 21
FROM eclipse-temurin:21-jdk-alpine

# Variável de ambiente para evitar problemas com caracteres
ENV LANG C.UTF-8

# Define diretório de trabalho no container
WORKDIR /app

# Copia o JAR gerado pelo Maven/Gradle para dentro do container
COPY target/*.jar app.jar

# Define a porta exposta (ajuste se estiver diferente no Spring Boot)
EXPOSE 8085

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
