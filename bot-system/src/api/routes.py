"""API routes for the bot system."""

from datetime import datetime
from typing import Any, Dict, List, Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from loguru import logger
from pydantic import BaseModel

from ..core import (
    AITextDetectionModule,
    CommandInterface,
    SelfAwareModule,
    SelfHealingCodingModule,
    TextHumanizationModule,
)
from ..models import Conversation, Interaction, SystemState, User

router = APIRouter()


class CommandRequest(BaseModel):
    """Command request model."""

    text: str
    user_id: str
    conversation_id: Optional[str] = None


class CommandResponse(BaseModel):
    """Command response model."""

    success: bool
    message: str
    data: Optional[Dict[str, Any]] = None


class HumanizeRequest(BaseModel):
    """Text humanization request."""

    text: str
    user_id: str
    formality: Optional[float] = 0.5
    enthusiasm: Optional[float] = 0.5


class HumanizeResponse(BaseModel):
    """Text humanization response."""

    original_text: str
    humanized_text: str


class DetectionRequest(BaseModel):
    """AI detection request."""

    text: str


class DetectionResponse(BaseModel):
    """AI detection response."""

    is_ai_generated: bool
    confidence: float
    analysis: Dict[str, Any]


class SystemStatusResponse(BaseModel):
    """System status response."""

    healthy: bool
    components: Dict[str, str]
    metrics: Dict[str, float]
    timestamp: str


@router.post("/command", response_model=CommandResponse)
async def execute_command(
    request: CommandRequest,
    command_interface: CommandInterface = Depends()
) -> CommandResponse:
    """
    Execute a natural language command.

    Args:
        request: Command request
        command_interface: Command interface instance

    Returns:
        Command execution response
    """
    try:
        command = command_interface.parse_command(request.text)

        result = command_interface.execute_command(command)

        return CommandResponse(
            success=result.success,
            message=result.message,
            data=result.data if hasattr(result, 'data') else None
        )

    except Exception as e:
        logger.error(f"Error executing command: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/humanize", response_model=HumanizeResponse)
async def humanize_text(
    request: HumanizeRequest,
    humanization: TextHumanizationModule = Depends()
) -> HumanizeResponse:
    """
    Humanize AI-generated text.

    Args:
        request: Humanization request
        humanization: Text humanization module

    Returns:
        Humanized text response
    """
    try:
        from ..core.text_humanization_module import HumanizationContext

        context = HumanizationContext(
            user_id=request.user_id,
            conversation_history=[],
            user_preferences={
                "formality": request.formality,
                "enthusiasm": request.enthusiasm,
            }
        )

        humanized = humanization.humanize_text(request.text, context)

        return HumanizeResponse(
            original_text=request.text,
            humanized_text=humanized
        )

    except Exception as e:
        logger.error(f"Error humanizing text: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/detect", response_model=DetectionResponse)
async def detect_ai_text(
    request: DetectionRequest,
    detection: AITextDetectionModule = Depends()
) -> DetectionResponse:
    """
    Detect if text is AI-generated.

    Args:
        request: Detection request
        detection: AI detection module

    Returns:
        Detection result
    """
    try:
        result = detection.detect_ai_text(request.text)

        return DetectionResponse(
            is_ai_generated=result.is_ai_generated,
            confidence=result.confidence,
            analysis=result.analysis
        )

    except Exception as e:
        logger.error(f"Error detecting AI text: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.get("/status", response_model=SystemStatusResponse)
async def get_system_status(
    self_aware: SelfAwareModule = Depends()
) -> SystemStatusResponse:
    """
    Get system status and health.

    Args:
        self_aware: Self-awareness module

    Returns:
        System status response
    """
    try:
        state = self_aware.analyze_state()

        capabilities = self_aware.get_capability_status()

        component_health = {
            name: cap.health.value
            for name, cap in capabilities.items()
        }

        return SystemStatusResponse(
            healthy=state.is_healthy(),
            components=component_health,
            metrics=state.metrics,
            timestamp=datetime.utcnow().isoformat()
        )

    except Exception as e:
        logger.error(f"Error getting system status: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/heal")
async def trigger_self_healing(
    healing: SelfHealingCodingModule = Depends()
) -> Dict[str, Any]:
    """
    Trigger self-healing verification and fixes.

    Args:
        healing: Self-healing module

    Returns:
        Healing result
    """
    try:
        report = healing.verify_system_integrity()

        return {
            "healthy": report.is_healthy,
            "issues": report.issues,
            "warnings": report.warnings,
            "recommendations": report.recommendations,
            "timestamp": report.generated_at.isoformat()
        }

    except Exception as e:
        logger.error(f"Error during self-healing: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.get("/suggestions")
async def get_command_suggestions(
    partial: str,
    command_interface: CommandInterface = Depends()
) -> List[Dict[str, Any]]:
    """
    Get command suggestions based on partial input.

    Args:
        partial: Partial command text
        command_interface: Command interface

    Returns:
        List of suggestions
    """
    try:
        suggestions = command_interface.suggest_commands(partial)

        return [
            {
                "command": s.command,
                "description": s.description,
                "examples": s.examples,
                "relevance": s.relevance
            }
            for s in suggestions
        ]

    except Exception as e:
        logger.error(f"Error getting suggestions: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.get("/health")
async def health_check() -> Dict[str, str]:
    """
    Health check endpoint.

    Returns:
        Health status
    """
    return {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat()
    }
