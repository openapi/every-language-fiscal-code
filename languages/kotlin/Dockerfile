FROM eclipse-temurin:21-jdk-alpine AS build
ARG KOTLIN_VERSION=2.1.0
RUN apk add --no-cache curl unzip \
 && curl -fsSL -o /tmp/kotlinc.zip "https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip" \
 && unzip -q /tmp/kotlinc.zip -d /opt \
 && rm /tmp/kotlinc.zip
ENV PATH="/opt/kotlinc/bin:${PATH}"
WORKDIR /src
COPY FiscalCode.kt .
RUN kotlinc FiscalCode.kt -include-runtime -d FiscalCode.jar \
 && kotlinc -version > VERSION 2>&1

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/FiscalCode.jar /src/VERSION ./
COPY run.sh ./
RUN chmod +x run.sh
CMD ["./run.sh"]
