FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml ./
COPY videoapp-web/pom.xml videoapp-web/
COPY videoapp-common/pom.xml videoapp-common/
COPY videoapp-storage/pom.xml videoapp-storage/
COPY videoapp-worker/pom.xml videoapp-worker/
COPY videoapp-core/pom.xml videoapp-core/


RUN mvn dependency:go-offline

COPY videoapp-web videoapp-web
COPY videoapp-common videoapp-common
COPY videoapp-storage videoapp-storage
COPY videoapp-worker videoapp-worker
COPY videoapp-core videoapp-core

RUN mvn clean package -X


FROM eclipse-temurin:17-jre-jammy AS videoapp-web
WORKDIR /app

COPY --from=builder /build/videoapp-web/target/videoapp-web.jar videoapp-web.jar
EXPOSE 8080 5005

ENTRYPOINT ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar videoapp-web.jar"]

FROM python:3.10-slim-bookworm AS worker-base

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    ffmpeg \
    libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN pip install --no-cache-dir uv

COPY videoapp-worker/requirements.txt ./

RUN --mount=type=cache,target=/root/.cache/uv \
    uv pip install --system -r requirements.txt

FROM worker-base AS videoapp-worker

ENV PYTHONUNBUFFERED=1 \
    TORCH_HOME=/opt/torch-cache \
    HF_HOME=/opt/huggingface-cache

COPY --from=builder /build/videoapp-worker/target/videoapp-worker.jar videoapp-worker.jar
COPY videoapp-worker/transcribe.py .
COPY videoapp-worker/dubbing_tool.py .

EXPOSE 5006
ENTRYPOINT ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar videoapp-worker.jar"]
