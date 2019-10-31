FROM maven:3-jdk-8-alpine AS BUILDER

COPY . /app

WORKDIR /app

RUN mvn --batch-mode --errors --fail-fast \
  --define maven.javadoc.skip=true \
  --define skipTests=true install

FROM java:8-jre-alpine

ARG VERSION=0.0.1-SNAPSHOT

COPY --from=BUILDER /app/target/mean-speed-service-${VERSION}.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]