FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/target/mall-chain-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /data/app/logs/mall_chain \
    && groupadd -r app && useradd -r -g app app \
    && chown -R app:app /app /data/app/logs

USER app

ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -Dfile.encoding=UTF-8"
ENV SPRING_PROFILE="prod"

EXPOSE 10010

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=$SPRING_PROFILE"]
