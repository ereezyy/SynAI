"""Command interface module for parsing and executing commands."""

import re
from datetime import datetime
from typing import Any, Callable, Dict, List, Optional
from uuid import UUID, uuid4

from loguru import logger


class Command:
    """Parsed command structure."""

    def __init__(
        self,
        command_type: str,
        action: str,
        parameters: Dict[str, Any],
        raw_text: str,
        confidence: float = 1.0
    ):
        self.id = uuid4()
        self.command_type = command_type
        self.action = action
        self.parameters = parameters
        self.raw_text = raw_text
        self.confidence = confidence
        self.timestamp = datetime.utcnow()


class CommandResult:
    """Result of command execution."""

    def __init__(
        self,
        success: bool,
        message: str,
        data: Optional[Any] = None,
        error: Optional[str] = None
    ):
        self.success = success
        self.message = message
        self.data = data
        self.error = error
        self.timestamp = datetime.utcnow()


class CommandEntry:
    """Command history entry."""

    def __init__(self, command: Command, result: CommandResult):
        self.command = command
        self.result = result
        self.executed_at = datetime.utcnow()


class CommandSuggestion:
    """Command suggestion for autocomplete."""

    def __init__(
        self,
        command: str,
        description: str,
        examples: List[str],
        relevance: float = 1.0
    ):
        self.command = command
        self.description = description
        self.examples = examples
        self.relevance = relevance


class CommandContext:
    """Context for command execution."""

    def __init__(
        self,
        user_id: UUID,
        conversation_id: UUID,
        environment: Dict[str, Any],
        previous_commands: List[CommandEntry]
    ):
        self.user_id = user_id
        self.conversation_id = conversation_id
        self.environment = environment
        self.previous_commands = previous_commands


class CommandHandler:
    """Base command handler interface."""

    def can_handle(self, command: Command) -> bool:
        """Check if handler can process command."""
        raise NotImplementedError

    def handle(self, command: Command, context: CommandContext) -> CommandResult:
        """Handle command execution."""
        raise NotImplementedError


class CommandInterface:
    """
    Command interface for natural language command processing.

    Provides capabilities for:
    - Parsing natural language commands
    - Routing commands to handlers
    - Managing command history
    - Suggesting commands
    """

    def __init__(self, config: Any):
        """
        Initialize command interface.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._handlers: Dict[str, CommandHandler] = {}
        self._command_history: List[CommandEntry] = []
        self._context_stack: List[CommandContext] = []

        self._command_patterns = self._initialize_patterns()
        self._aliases = self._initialize_aliases()

        logger.info("Command interface initialized")

    def parse_command(self, input_text: str) -> Command:
        """
        Parse natural language input into command.

        Args:
            input_text: Raw input text

        Returns:
            Parsed command object
        """
        cleaned_text = input_text.strip().lower()

        command_type, confidence = self._identify_command_type(cleaned_text)
        action = self._extract_action(cleaned_text, command_type)
        parameters = self._extract_parameters(cleaned_text, command_type, action)

        command = Command(
            command_type=command_type,
            action=action,
            parameters=parameters,
            raw_text=input_text,
            confidence=confidence
        )

        logger.debug(f"Parsed command: {command_type}.{action} with confidence {confidence:.2f}")
        return command

    def execute_command(self, command: Command) -> CommandResult:
        """
        Execute a parsed command.

        Args:
            command: Command to execute

        Returns:
            Result of command execution
        """
        try:
            handler = self._find_handler(command)

            if not handler:
                return CommandResult(
                    success=False,
                    message=f"No handler found for command type: {command.command_type}",
                    error="NO_HANDLER"
                )

            context = self.get_command_context()
            result = handler.handle(command, context)

            entry = CommandEntry(command, result)
            self._command_history.append(entry)

            if len(self._command_history) > 100:
                self._command_history = self._command_history[-100:]

            logger.info(f"Executed command {command.id}: {result.message}")
            return result

        except Exception as e:
            logger.error(f"Error executing command: {e}")
            return CommandResult(
                success=False,
                message="Command execution failed",
                error=str(e)
            )

    def get_command_history(self) -> List[CommandEntry]:
        """
        Get command history.

        Returns:
            List of command entries
        """
        return self._command_history.copy()

    def suggest_commands(self, partial_input: str) -> List[CommandSuggestion]:
        """
        Suggest commands based on partial input.

        Args:
            partial_input: Partial command text

        Returns:
            List of command suggestions
        """
        suggestions = []
        partial_lower = partial_input.lower()

        for cmd_type, patterns in self._command_patterns.items():
            for pattern_info in patterns:
                if pattern_info["trigger"].startswith(partial_lower):
                    suggestion = CommandSuggestion(
                        command=pattern_info["trigger"],
                        description=pattern_info["description"],
                        examples=pattern_info.get("examples", []),
                        relevance=self._calculate_relevance(partial_input, pattern_info["trigger"])
                    )
                    suggestions.append(suggestion)

        suggestions.sort(key=lambda x: x.relevance, reverse=True)
        return suggestions[:5]

    def register_command_handler(
        self,
        command_type: str,
        handler: CommandHandler
    ) -> None:
        """
        Register a command handler.

        Args:
            command_type: Type of command to handle
            handler: Handler instance
        """
        self._handlers[command_type] = handler
        logger.debug(f"Registered handler for command type: {command_type}")

    def get_command_context(self) -> CommandContext:
        """
        Get current command context.

        Returns:
            Current command context
        """
        if self._context_stack:
            return self._context_stack[-1]

        return CommandContext(
            user_id=uuid4(),
            conversation_id=uuid4(),
            environment={},
            previous_commands=self._command_history[-10:] if self._command_history else []
        )

    def push_context(self, context: CommandContext) -> None:
        """
        Push new context onto stack.

        Args:
            context: Context to push
        """
        self._context_stack.append(context)

    def pop_context(self) -> Optional[CommandContext]:
        """
        Pop context from stack.

        Returns:
            Popped context or None
        """
        if self._context_stack:
            return self._context_stack.pop()
        return None

    def _initialize_patterns(self) -> Dict[str, List[Dict[str, Any]]]:
        """Initialize command patterns."""
        return {
            "query": [
                {
                    "trigger": "what",
                    "pattern": r"what (is|are|was|were) (.+)",
                    "description": "Query information",
                    "examples": ["what is the weather", "what are my tasks"]
                },
                {
                    "trigger": "how",
                    "pattern": r"how (to|do|does|can) (.+)",
                    "description": "Query instructions",
                    "examples": ["how to schedule meeting", "how can I help"]
                }
            ],
            "action": [
                {
                    "trigger": "create",
                    "pattern": r"(create|make|add|new) (.+)",
                    "description": "Create new item",
                    "examples": ["create meeting", "add task", "new reminder"]
                },
                {
                    "trigger": "schedule",
                    "pattern": r"schedule (.+) (at|on|for) (.+)",
                    "description": "Schedule event",
                    "examples": ["schedule meeting at 3pm", "schedule call for tomorrow"]
                },
                {
                    "trigger": "send",
                    "pattern": r"send (.+) to (.+)",
                    "description": "Send message or email",
                    "examples": ["send email to John", "send message to team"]
                }
            ],
            "system": [
                {
                    "trigger": "help",
                    "pattern": r"help( with)?(.+)?",
                    "description": "Get help",
                    "examples": ["help", "help with commands"]
                },
                {
                    "trigger": "status",
                    "pattern": r"(status|health|state)",
                    "description": "Check system status",
                    "examples": ["status", "system health"]
                }
            ]
        }

    def _initialize_aliases(self) -> Dict[str, str]:
        """Initialize command aliases."""
        return {
            "make": "create",
            "add": "create",
            "new": "create",
            "plan": "schedule",
            "book": "schedule",
            "mail": "send",
            "message": "send",
        }

    def _identify_command_type(self, text: str) -> tuple[str, float]:
        """Identify command type from text."""
        best_match = ("general", 0.5)

        for cmd_type, patterns in self._command_patterns.items():
            for pattern_info in patterns:
                if pattern_info["trigger"] in text:
                    confidence = 0.9 if text.startswith(pattern_info["trigger"]) else 0.7
                    if confidence > best_match[1]:
                        best_match = (cmd_type, confidence)

                match = re.search(pattern_info["pattern"], text)
                if match:
                    best_match = (cmd_type, 0.95)
                    break

        return best_match

    def _extract_action(self, text: str, command_type: str) -> str:
        """Extract action from text."""
        words = text.split()

        if not words:
            return "unknown"

        first_word = words[0]

        if first_word in self._aliases:
            return self._aliases[first_word]

        if command_type in self._command_patterns:
            for pattern_info in self._command_patterns[command_type]:
                if pattern_info["trigger"] in text:
                    return pattern_info["trigger"]

        return first_word

    def _extract_parameters(
        self,
        text: str,
        command_type: str,
        action: str
    ) -> Dict[str, Any]:
        """Extract parameters from text."""
        parameters: Dict[str, Any] = {}

        time_patterns = [
            (r"at (\d{1,2}):?(\d{2})?\s?(am|pm)?", "time"),
            (r"on (monday|tuesday|wednesday|thursday|friday|saturday|sunday)", "day"),
            (r"(today|tomorrow|yesterday)", "relative_date"),
        ]

        for pattern, key in time_patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                parameters[key] = match.group(0)

        entity_patterns = [
            (r"to ([a-zA-Z\s]+)", "recipient"),
            (r"for ([a-zA-Z\s]+)", "target"),
            (r"with ([a-zA-Z\s]+)", "companion"),
        ]

        for pattern, key in entity_patterns:
            match = re.search(pattern, text)
            if match:
                parameters[key] = match.group(1).strip()

        action_words = action.split()
        remaining_text = text
        for word in action_words:
            remaining_text = remaining_text.replace(word, "", 1)

        remaining_text = remaining_text.strip()
        if remaining_text and "subject" not in parameters:
            parameters["subject"] = remaining_text

        return parameters

    def _find_handler(self, command: Command) -> Optional[CommandHandler]:
        """Find appropriate handler for command."""
        handler = self._handlers.get(command.command_type)

        if handler and handler.can_handle(command):
            return handler

        for handler in self._handlers.values():
            if handler.can_handle(command):
                return handler

        return None

    def _calculate_relevance(self, partial: str, full: str) -> float:
        """Calculate relevance score for suggestion."""
        if not partial:
            return 0.5

        if full.startswith(partial):
            return 1.0 - (len(full) - len(partial)) / len(full)

        if partial in full:
            return 0.7

        return 0.3
