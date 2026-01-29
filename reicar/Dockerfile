# syntax=docker/dockerfile:1
# -----------------------------------------------------------------------------
# Stage 1: Base - Common configuration for build stages
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS base
WORKDIR /build
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

# -----------------------------------------------------------------------------
# Stage 2: Dependencies - Download and cache Maven dependencies
# -----------------------------------------------------------------------------
FROM base AS deps
WORKDIR /build
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

# -----------------------------------------------------------------------------
# Stage 3: Package - Build the application JAR
# -----------------------------------------------------------------------------
FROM deps AS package
WORKDIR /build
COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

# -----------------------------------------------------------------------------
# Stage 4: Extract - Extract Spring Boot layers for optimized caching
# -----------------------------------------------------------------------------
FROM package AS extract
WORKDIR /build
RUN java -Djarmode=tools -jar target/app.jar extract --layers --launcher --destination target/extracted

# -----------------------------------------------------------------------------
# Stage 5: Development - Development image with debugging support
# -----------------------------------------------------------------------------
FROM extract AS development
WORKDIR /app
RUN cp -r /build/target/extracted/dependencies/. ./
RUN cp -r /build/target/extracted/spring-boot-loader/. ./
RUN cp -r /build/target/extracted/snapshot-dependencies/. ./
RUN cp -r /build/target/extracted/application/. ./
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"
EXPOSE 8080 8000
CMD ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# -----------------------------------------------------------------------------
# Stage 6: Final - Production-ready minimal image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

WORKDIR /app

COPY --from=extract --chown=appuser:appuser /build/target/extracted/dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/application/ ./

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
