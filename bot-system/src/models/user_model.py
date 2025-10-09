"""User data models."""

from datetime import datetime
from typing import Dict, Optional
from uuid import UUID, uuid4

from pydantic import BaseModel, EmailStr, Field


class UserPreferences(BaseModel):
    """User preferences configuration."""

    language: str = "en"
    timezone: str = "UTC"
    notification_enabled: bool = True
    theme: str = "light"
    humanization_level: float = Field(default=0.5, ge=0.0, le=1.0)
    auto_summarize: bool = True
    custom_settings: Dict[str, any] = Field(default_factory=dict)


class User(BaseModel):
    """User model."""

    id: UUID = Field(default_factory=uuid4)
    name: str
    email: EmailStr
    preferences: UserPreferences = Field(default_factory=UserPreferences)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        """Pydantic configuration."""

        json_encoders = {
            UUID: str,
            datetime: lambda v: v.isoformat(),
        }
