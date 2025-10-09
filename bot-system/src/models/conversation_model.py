"""Conversation and interaction data models."""

from datetime import datetime
from typing import Dict, Optional
from uuid import UUID, uuid4

from pydantic import BaseModel, Field


class Interaction(BaseModel):
    """Interaction within a conversation."""

    id: UUID = Field(default_factory=uuid4)
    conversation_id: UUID
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    content: str
    source: str
    metadata: Dict[str, any] = Field(default_factory=dict)
    ai_detected: bool = False
    confidence_score: float = Field(default=0.0, ge=0.0, le=1.0)

    class Config:
        """Pydantic configuration."""

        json_encoders = {
            UUID: str,
            datetime: lambda v: v.isoformat(),
        }


class Conversation(BaseModel):
    """Conversation model."""

    id: UUID = Field(default_factory=uuid4)
    user_id: UUID
    start_time: datetime = Field(default_factory=datetime.utcnow)
    end_time: Optional[datetime] = None
    summary: Optional[str] = None
    context: Dict[str, any] = Field(default_factory=dict)
    interactions: list[Interaction] = Field(default_factory=list)

    class Config:
        """Pydantic configuration."""

        json_encoders = {
            UUID: str,
            datetime: lambda v: v.isoformat(),
        }

    def add_interaction(self, content: str, source: str, **kwargs: any) -> Interaction:
        """
        Add interaction to conversation.

        Args:
            content: Interaction content
            source: Source of interaction (user, bot, system)
            **kwargs: Additional interaction parameters

        Returns:
            Created interaction
        """
        interaction = Interaction(
            conversation_id=self.id,
            content=content,
            source=source,
            **kwargs
        )
        self.interactions.append(interaction)
        return interaction

    def end_conversation(self, summary: Optional[str] = None) -> None:
        """
        End the conversation.

        Args:
            summary: Optional conversation summary
        """
        self.end_time = datetime.utcnow()
        if summary:
            self.summary = summary
