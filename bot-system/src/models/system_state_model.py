"""System state data models."""

from datetime import datetime
from enum import Enum
from typing import Dict
from uuid import UUID, uuid4

from pydantic import BaseModel, Field


class ComponentHealth(str, Enum):
    """Component health status."""

    HEALTHY = "healthy"
    DEGRADED = "degraded"
    UNHEALTHY = "unhealthy"
    UNKNOWN = "unknown"


class SystemState(BaseModel):
    """System state snapshot."""

    id: UUID = Field(default_factory=uuid4)
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    component: str
    state_data: Dict[str, any] = Field(default_factory=dict)
    health_status: ComponentHealth = ComponentHealth.HEALTHY
    metrics: Dict[str, float] = Field(default_factory=dict)

    class Config:
        """Pydantic configuration."""

        json_encoders = {
            UUID: str,
            datetime: lambda v: v.isoformat(),
        }

    def is_healthy(self) -> bool:
        """Check if component is healthy."""
        return self.health_status == ComponentHealth.HEALTHY

    def update_health(self, status: ComponentHealth) -> None:
        """
        Update health status.

        Args:
            status: New health status
        """
        self.health_status = status
        self.timestamp = datetime.utcnow()
