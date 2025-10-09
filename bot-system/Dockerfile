# Multi-stage Dockerfile for Self-Aware Assistant Bot

FROM python:3.9-slim as base

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

FROM base as dependencies

RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

RUN pip install poetry==1.7.1

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.create false \
    && poetry install --no-interaction --no-ansi --no-root

FROM dependencies as application

COPY src/ /app/src/
COPY config/ /app/config/

RUN poetry install --no-interaction --no-ansi

FROM application as development

RUN poetry install --no-interaction --no-ansi --with dev

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

FROM application as production

RUN apt-get update && apt-get install -y \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1000 botuser && \
    chown -R botuser:botuser /app

USER botuser

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8000/api/v1/health')"

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]
