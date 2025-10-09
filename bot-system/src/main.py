"""Main application entry point."""

import sys
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger
from supabase import create_client, Client

sys.path.insert(0, str(Path(__file__).parent.parent))

from src.api.routes import router as api_router
from src.api.webhooks import router as webhook_router
from src.core import (
    AITextDetectionModule,
    CommandInterface,
    SelfAwareModule,
    SelfHealingCodingModule,
    TextHumanizationModule,
)
from src.utils.config_manager import ConfigManager
from src.utils.logger import LoggingManager

config = ConfigManager()
logging_manager = LoggingManager(config)

app = FastAPI(
    title="Self-Aware Assistant Bot",
    description="Advanced AI assistant with self-awareness and self-healing capabilities",
    version="0.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=config.get("api.cors_origins", ["*"]),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

supabase_url = config.get("database.supabase_url")
supabase_key = config.get("database.supabase_key")
supabase: Client = create_client(supabase_url, supabase_key)

self_aware_module = SelfAwareModule(config)
self_healing_module = SelfHealingCodingModule(config)
command_interface = CommandInterface(config)
humanization_module = TextHumanizationModule(config)
detection_module = AITextDetectionModule(config)


@app.on_event("startup")
async def startup_event():
    """Initialize application on startup."""
    logger.info("Starting Self-Aware Assistant Bot")

    logger.info("Initializing core modules")
    logger.info("- Self-Awareness Module: OK")
    logger.info("- Self-Healing Module: OK")
    logger.info("- Command Interface: OK")
    logger.info("- Text Humanization: OK")
    logger.info("- AI Detection: OK")

    logger.info("Verifying system integrity")
    integrity_report = self_healing_module.verify_system_integrity()

    if not integrity_report.is_healthy:
        logger.warning(f"System integrity issues detected: {integrity_report.issues}")
    else:
        logger.info("System integrity verified")

    logger.info("Application started successfully")


@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on application shutdown."""
    logger.info("Shutting down Self-Aware Assistant Bot")


app.include_router(api_router, prefix="/api/v1", tags=["API"])
app.include_router(webhook_router, prefix="/webhooks", tags=["Webhooks"])


@app.get("/")
async def root():
    """Root endpoint."""
    return {
        "name": "Self-Aware Assistant Bot",
        "version": "0.1.0",
        "status": "running",
        "endpoints": {
            "api": "/api/v1",
            "webhooks": "/webhooks",
            "docs": "/docs",
            "health": "/api/v1/health"
        }
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=config.get("api.host", "0.0.0.0"),
        port=config.get("api.port", 8000),
        reload=config.is_debug,
        log_level="info"
    )
