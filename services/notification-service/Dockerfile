## Multi-stage Dockerfile — notification-service
# Builder stage: build the service artifact using Maven. The build context is the repository root
FROM maven:3.9.6-eclipse-temurin-21 AS builder
ARG SKIP_TESTS=true
WORKDIR /workspace

# Copy root pom first to leverage Docker layer caching for dependency resolution
COPY pom.xml ./
# Add the parent pom into the builder's local Maven repository path so child modules can
# resolve it without invoking a reactor build. This avoids running Maven on the root pom.
RUN mkdir -p /root/.m2/repository/com/gearup/gearup-root/1.0.0 \
    && cp pom.xml /root/.m2/repository/com/gearup/gearup-root/1.0.0/gearup-root-1.0.0.pom
# Copy the service pom and source
COPY services/notification-service/pom.xml ./services/notification-service/pom.xml
COPY services/notification-service/src ./services/notification-service/src
# Copy shared modules (if any) used by the service
COPY shared-libs ./shared-libs
# Install local shared modules into the builder's Maven local repository so the module
# can resolve them during its build. This avoids requiring the full multi-module reactor.
RUN if [ -d shared-libs/security-lib ]; then mvn -f shared-libs/security-lib/pom.xml clean install -DskipTests=${SKIP_TESTS}; fi
RUN if [ -d shared-libs/event-models ]; then mvn -f shared-libs/event-models/pom.xml clean install -DskipTests=${SKIP_TESTS}; fi
RUN if [ -d shared-libs/common-utils ]; then mvn -f shared-libs/common-utils/pom.xml clean install -DskipTests=${SKIP_TESTS}; fi

# Run a targeted build for the notification-service module only using its module pom
RUN mvn -T 1C -f services/notification-service/pom.xml clean package -DskipTests=${SKIP_TESTS}

# Diagnostic: print produced jar name and its MANIFEST.MF to help debug "no main manifest attribute" issues
RUN set -eux \
    && JAR=$(ls services/notification-service/target/*.jar | head -n1) \
    && echo "Built JAR: $JAR" \
    && if [ -f "$JAR" ]; then echo "--- MANIFEST.MF (via jar) ---"; jar tf "$JAR" | sed -n '1,50p' || true; jar xf "$JAR" META-INF/MANIFEST.MF || true; echo "--- RAW MANIFEST ---"; cat META-INF/MANIFEST.MF || true; else echo "No jar found in target"; fi

## Runtime stage: run the previously built jar using a small JRE image
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the built executable jar from the builder stage
COPY --from=builder /workspace/services/notification-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -f http://localhost:8081/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
