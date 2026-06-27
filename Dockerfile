# Multi-stage build: compile with Maven (JDK 21), run on slim JRE 21.
# ----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Cache dependencies first
COPY pom.xml .
RUN mvn -q -B dependency:go-offline 2>/dev/null || true
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S ces && adduser -S ces -G ces && apk add --no-cache wget
# Pre-create the uploads dir owned by ces; a named volume mounted here inherits
# this ownership, so the non-root app can write uploaded images.
RUN mkdir -p /app/uploads && chown ces:ces /app/uploads
COPY --from=build --chown=ces:ces /workspace/target/*.jar /app/app.jar
USER ces
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0"
HEALTHCHECK --interval=30s --timeout=5s --start-period=70s --retries=6 \
  CMD wget -qO- http://localhost:8080/actuator/health >/dev/null 2>&1 || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
