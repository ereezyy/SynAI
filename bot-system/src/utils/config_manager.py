"""Configuration management system."""

import os
from pathlib import Path
from typing import Any, Dict, Optional

import yaml
from dotenv import load_dotenv
from loguru import logger


class ConfigManager:
    """Manages application configuration with environment-specific overrides."""

    def __init__(self, env: str = "development"):
        """
        Initialize configuration manager.

        Args:
            env: Environment name (development, testing, production)
        """
        self.env = env
        self.config: Dict[str, Any] = {}
        self._runtime_config: Dict[str, Any] = {}
        self._load_config()

    def _load_config(self) -> None:
        """Load configuration from files and environment variables."""
        load_dotenv()

        config_dir = Path(__file__).parent.parent.parent / "config"

        default_config_path = config_dir / "default.yaml"
        if default_config_path.exists():
            with open(default_config_path, "r") as f:
                self.config = yaml.safe_load(f) or {}
                logger.info(f"Loaded default configuration from {default_config_path}")

        env_config_path = config_dir / f"{self.env}.yaml"
        if env_config_path.exists():
            with open(env_config_path, "r") as f:
                env_config = yaml.safe_load(f) or {}
                self._merge_config(self.config, env_config)
                logger.info(f"Loaded {self.env} configuration from {env_config_path}")

        self._load_env_variables()

    def _load_env_variables(self) -> None:
        """Load configuration from environment variables."""
        env_mappings = {
            "SUPABASE_URL": ["database", "supabase_url"],
            "SUPABASE_KEY": ["database", "supabase_key"],
            "OPENAI_API_KEY": ["ai", "openai_key"],
            "ANTHROPIC_API_KEY": ["ai", "anthropic_key"],
            "LOG_LEVEL": ["logging", "level"],
        }

        for env_var, config_path in env_mappings.items():
            value = os.getenv(env_var)
            if value:
                self._set_nested(self.config, config_path, value)
                logger.debug(f"Set {'.'.join(config_path)} from environment variable")

    def _merge_config(self, base: Dict[str, Any], override: Dict[str, Any]) -> None:
        """
        Recursively merge override config into base config.

        Args:
            base: Base configuration dictionary
            override: Override configuration dictionary
        """
        for key, value in override.items():
            if key in base and isinstance(base[key], dict) and isinstance(value, dict):
                self._merge_config(base[key], value)
            else:
                base[key] = value

    def _set_nested(self, config: Dict[str, Any], path: list[str], value: Any) -> None:
        """
        Set nested configuration value using path.

        Args:
            config: Configuration dictionary
            path: List of keys representing path
            value: Value to set
        """
        for key in path[:-1]:
            config = config.setdefault(key, {})
        config[path[-1]] = value

    def get(self, key: str, default: Any = None) -> Any:
        """
        Get configuration value using dot notation.

        Args:
            key: Configuration key (supports dot notation, e.g., 'database.host')
            default: Default value if key not found

        Returns:
            Configuration value or default
        """
        if key in self._runtime_config:
            return self._runtime_config[key]

        keys = key.split(".")
        value = self.config
        for k in keys:
            if isinstance(value, dict):
                value = value.get(k)
            else:
                return default
            if value is None:
                return default
        return value

    def set_runtime(self, key: str, value: Any) -> None:
        """
        Set runtime configuration value.

        Args:
            key: Configuration key
            value: Value to set
        """
        self._runtime_config[key] = value
        logger.debug(f"Set runtime configuration: {key}")

    def get_all(self) -> Dict[str, Any]:
        """
        Get all configuration as dictionary.

        Returns:
            Complete configuration dictionary
        """
        config = self.config.copy()
        config.update(self._runtime_config)
        return config

    @property
    def is_production(self) -> bool:
        """Check if running in production environment."""
        return self.env == "production"

    @property
    def is_debug(self) -> bool:
        """Check if debug mode is enabled."""
        return self.get("debug", False) or self.env == "development"
