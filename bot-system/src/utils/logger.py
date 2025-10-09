"""Enhanced logging system with structured logging and rotation."""

import sys
from pathlib import Path
from typing import Any, Dict, Optional

from loguru import logger


class LoggingManager:
    """Manages application logging with rotation, compression, and structured logging."""

    def __init__(self, config: Any):
        """
        Initialize logging manager.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._setup_logging()

    def _setup_logging(self) -> None:
        """Configure loguru with appropriate settings."""
        logger.remove()

        log_level = self.config.get("logging.level", "INFO")
        log_format = (
            "<green>{time:YYYY-MM-DD HH:mm:ss}</green> | "
            "<level>{level: <8}</level> | "
            "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> | "
            "<level>{message}</level>"
        )

        logger.add(
            sys.stdout,
            format=log_format,
            level=log_level,
            colorize=True,
        )

        log_dir = Path(self.config.get("logging.directory", "logs"))
        log_dir.mkdir(exist_ok=True)

        logger.add(
            log_dir / "app_{time:YYYY-MM-DD}.log",
            format=log_format,
            level=log_level,
            rotation="00:00",
            retention="30 days",
            compression="zip",
            serialize=False,
        )

        logger.add(
            log_dir / "errors_{time:YYYY-MM-DD}.log",
            format=log_format,
            level="ERROR",
            rotation="100 MB",
            retention="60 days",
            compression="zip",
            backtrace=True,
            diagnose=True,
        )

        logger.info(f"Logging initialized with level: {log_level}")

    def get_logger(self, name: str) -> Any:
        """
        Get logger instance for module.

        Args:
            name: Module name

        Returns:
            Logger instance
        """
        return logger.bind(module=name)

    def capture_exception(self, exc: Exception, context: Optional[Dict[str, Any]] = None) -> None:
        """
        Log exception with context.

        Args:
            exc: Exception to log
            context: Additional context information
        """
        if context:
            logger.error(f"Exception occurred with context: {context}")
        logger.exception(exc)

    def audit_log(self, user_id: str, action: str, details: Dict[str, Any]) -> None:
        """
        Record audit log entry.

        Args:
            user_id: User identifier
            action: Action performed
            details: Action details
        """
        log_entry = {
            "user_id": user_id,
            "action": action,
            "details": details,
        }
        logger.bind(audit=True).info(f"AUDIT: {log_entry}")
