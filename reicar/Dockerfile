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
# Stage 3: NPM - Install frontend dependencies
# -----------------------------------------------------------------------------
FROM node:20-slim AS npm
WORKDIR /build
COPY package.json ./
RUN mkdir -p src/main/resources/static/css/fonts src/main/resources/static/js && \
    npm install --ignore-scripts && \
    cp node_modules/bootstrap/dist/css/bootstrap.min.css src/main/resources/static/css/ && \
    cp node_modules/bootstrap/dist/js/bootstrap.bundle.min.js src/main/resources/static/js/ && \
    cp node_modules/bootstrap-icons/font/bootstrap-icons.min.css src/main/resources/static/css/ && \
    cp node_modules/bootstrap-icons/font/fonts/* src/main/resources/static/css/fonts/ && \
    cp node_modules/chart.js/dist/chart.umd.js src/main/resources/static/js/chart.min.js

# -----------------------------------------------------------------------------
# Stage 4: Package - Build the application JAR
# -----------------------------------------------------------------------------
FROM deps AS package
WORKDIR /build
COPY ./src src/
COPY --from=npm /build/src/main/resources/static/css/ src/main/resources/static/css/
COPY --from=npm /build/src/main/resources/static/js/*.js src/main/resources/static/js/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

# -----------------------------------------------------------------------------
# Stage 5: Extract - Extract Spring Boot layers for optimized caching
# -----------------------------------------------------------------------------
FROM package AS extract
WORKDIR /build
RUN java -Djarmode=tools -jar target/app.jar extract --layers --launcher --destination target/extracted

# -----------------------------------------------------------------------------
# Stage 6: Development - Development image with debugging support
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
# Stage 7: Final - Production-ready minimal image
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
