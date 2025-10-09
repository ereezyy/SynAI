"""Core modules for the bot system."""

from .self_aware_module import SelfAwareModule
from .self_healing_coding_module import SelfHealingCodingModule
from .command_interface import CommandInterface
from .text_humanization_module import TextHumanizationModule
from .ai_text_detection_module import AITextDetectionModule

__all__ = [
    "SelfAwareModule",
    "SelfHealingCodingModule",
    "CommandInterface",
    "TextHumanizationModule",
    "AITextDetectionModule",
]
