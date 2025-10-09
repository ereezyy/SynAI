# Self-Aware Assistant Bot

Advanced personal assistant bot with self-awareness, self-healing capabilities, and AI text detection.

## Features

- **Self-Awareness Module**: Monitors system health, evaluates responses, and learns from interactions
- **Self-Healing Module**: Automatically diagnoses and fixes code issues
- **Command Interface**: Natural language command processing with NLP
- **Text Humanization**: Converts AI text to natural-sounding human text
- **AI Detection**: Identifies AI-generated content

## Quick Start

### Using Docker

```bash
docker-compose up
```

### Local Development

```bash
poetry install
poetry run uvicorn src.main:app --reload
```

## API Endpoints

- `POST /api/v1/command` - Execute natural language commands
- `POST /api/v1/humanize` - Humanize AI-generated text
- `POST /api/v1/detect` - Detect AI-generated text
- `GET /api/v1/status` - Get system status
- `POST /api/v1/heal` - Trigger self-healing
- `GET /api/v1/suggestions` - Get command suggestions

## Configuration

Configuration is managed through YAML files in the `config/` directory:

- `default.yaml` - Base configuration
- `development.yaml` - Development overrides
- `production.yaml` - Production settings

Environment variables can override any configuration value.

## Architecture

The system is built with:

- **FastAPI** - Modern web framework
- **Supabase** - Database and authentication
- **Pydantic** - Data validation
- **Loguru** - Enhanced logging
- **Docker** - Containerization

## Development

Run tests:
```bash
poetry run pytest
```

Format code:
```bash
poetry run black src tests
poetry run isort src tests
```

Type checking:
```bash
poetry run mypy src
```

## Deployment

Build production image:
```bash
docker build --target production -t self-aware-bot:latest .
```

Deploy with Kubernetes:
```bash
kubectl apply -f k8s/
```

## License

MIT
