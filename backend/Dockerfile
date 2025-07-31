FROM maven:eclipse-temurin AS build
COPY pom.xml .

RUN mvn dependency:go-offline

COPY . .
RUN mvn package -DskipTests

FROM eclipse-temurin:22

RUN groupadd -r myuser && useradd -r -g myuser myuser

WORKDIR /usr/src/app

COPY --chown=myuser:myuser --from=build target/backend-0.0.1.jar ./backend-0.0.1.jar

USER myuser

CMD ["java", "-jar", "./backend-0.0.1.jar"]