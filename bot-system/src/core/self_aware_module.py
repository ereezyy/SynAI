"""Self-awareness module for introspection and capability monitoring."""

import time
from datetime import datetime
from typing import Any, Callable, Dict, List, Optional
from uuid import UUID

from loguru import logger

from ..models.system_state_model import ComponentHealth, SystemState


class CapabilityStatus:
    """Status of a system capability."""

    def __init__(self, name: str, enabled: bool = True, health: ComponentHealth = ComponentHealth.HEALTHY):
        self.name = name
        self.enabled = enabled
        self.health = health
        self.last_check = datetime.utcnow()
        self.metrics: Dict[str, float] = {}

    def update(self, health: ComponentHealth, metrics: Optional[Dict[str, float]] = None) -> None:
        """Update capability status."""
        self.health = health
        self.last_check = datetime.utcnow()
        if metrics:
            self.metrics.update(metrics)


class EvaluationResult:
    """Result of response evaluation."""

    def __init__(
        self,
        score: float,
        confidence: float,
        metrics: Dict[str, float],
        feedback: Optional[str] = None
    ):
        self.score = score
        self.confidence = confidence
        self.metrics = metrics
        self.feedback = feedback
        self.timestamp = datetime.utcnow()


class Context:
    """Context for evaluating responses."""

    def __init__(
        self,
        user_id: UUID,
        conversation_id: UUID,
        query: str,
        history: List[Dict[str, Any]]
    ):
        self.user_id = user_id
        self.conversation_id = conversation_id
        self.query = query
        self.history = history


class Interaction:
    """Interaction data for learning."""

    def __init__(
        self,
        query: str,
        response: str,
        context: Context,
        evaluation: Optional[EvaluationResult] = None
    ):
        self.query = query
        self.response = response
        self.context = context
        self.evaluation = evaluation
        self.timestamp = datetime.utcnow()


class CodeAnalysisResult:
    """Result of code introspection."""

    def __init__(
        self,
        module_name: str,
        complexity: float,
        quality_score: float,
        issues: List[str],
        suggestions: List[str]
    ):
        self.module_name = module_name
        self.complexity = complexity
        self.quality_score = quality_score
        self.issues = issues
        self.suggestions = suggestions


class SelfAwareObserver:
    """Observer interface for self-awareness events."""

    def on_state_change(self, state: SystemState) -> None:
        """Called when system state changes."""
        pass

    def on_capability_change(self, capability: str, status: CapabilityStatus) -> None:
        """Called when capability status changes."""
        pass


class SelfAwareModule:
    """
    Self-awareness module for monitoring and introspection.

    Provides capabilities for:
    - Monitoring system health and performance
    - Evaluating response quality
    - Learning from interactions
    - Introspecting code quality
    """

    def __init__(self, config: Any):
        """
        Initialize self-awareness module.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._capabilities: Dict[str, CapabilityStatus] = {}
        self._observers: List[SelfAwareObserver] = []
        self._interaction_history: List[Interaction] = []
        self._performance_metrics: Dict[str, List[float]] = {}

        self._initialize_capabilities()
        logger.info("Self-awareness module initialized")

    def _initialize_capabilities(self) -> None:
        """Initialize system capabilities."""
        capabilities = [
            "command_processing",
            "text_generation",
            "ai_detection",
            "self_healing",
            "external_integration",
        ]

        for cap in capabilities:
            self._capabilities[cap] = CapabilityStatus(cap)

    def analyze_state(self) -> SystemState:
        """
        Analyze current system state.

        Returns:
            Current system state snapshot
        """
        state_data = {
            "capabilities": {
                name: {
                    "enabled": cap.enabled,
                    "health": cap.health.value,
                    "metrics": cap.metrics,
                }
                for name, cap in self._capabilities.items()
            },
            "interaction_count": len(self._interaction_history),
            "uptime_seconds": self._calculate_uptime(),
        }

        overall_health = self._determine_overall_health()

        state = SystemState(
            component="self_aware_module",
            state_data=state_data,
            health_status=overall_health,
            metrics=self._aggregate_metrics(),
        )

        self._notify_state_change(state)
        return state

    def evaluate_response(
        self,
        response: str,
        context: Context
    ) -> EvaluationResult:
        """
        Evaluate quality of generated response.

        Args:
            response: Generated response text
            context: Context in which response was generated

        Returns:
            Evaluation result with scores and feedback
        """
        start_time = time.time()

        metrics = {
            "length": len(response),
            "word_count": len(response.split()),
            "relevance": self._calculate_relevance(response, context),
            "coherence": self._calculate_coherence(response),
            "appropriateness": self._calculate_appropriateness(response, context),
        }

        score = sum(metrics.values()) / len(metrics)
        confidence = self._calculate_confidence(metrics)

        processing_time = time.time() - start_time
        self._record_metric("evaluation_time", processing_time)

        return EvaluationResult(
            score=score,
            confidence=confidence,
            metrics=metrics,
            feedback=self._generate_feedback(metrics),
        )

    def learn_from_interaction(self, interaction: Interaction) -> None:
        """
        Learn from past interaction.

        Args:
            interaction: Interaction data to learn from
        """
        self._interaction_history.append(interaction)

        if len(self._interaction_history) > 1000:
            self._interaction_history = self._interaction_history[-1000:]

        if interaction.evaluation:
            self._update_learning_metrics(interaction)

        logger.debug(f"Learned from interaction, history size: {len(self._interaction_history)}")

    def get_capability_status(self) -> Dict[str, CapabilityStatus]:
        """
        Get status of all capabilities.

        Returns:
            Dictionary of capability statuses
        """
        for name, cap in self._capabilities.items():
            cap.update(
                self._check_capability_health(name),
                self._get_capability_metrics(name)
            )

        return self._capabilities.copy()

    def introspect_code(self, module_name: str) -> CodeAnalysisResult:
        """
        Introspect code quality of a module.

        Args:
            module_name: Name of module to analyze

        Returns:
            Code analysis result
        """
        try:
            import ast
            import inspect

            module = __import__(module_name)
            source = inspect.getsource(module)
            tree = ast.parse(source)

            complexity = self._calculate_complexity(tree)
            quality_score = self._assess_code_quality(tree, source)
            issues = self._find_code_issues(tree, source)
            suggestions = self._generate_suggestions(issues)

            return CodeAnalysisResult(
                module_name=module_name,
                complexity=complexity,
                quality_score=quality_score,
                issues=issues,
                suggestions=suggestions,
            )

        except Exception as e:
            logger.error(f"Error introspecting module {module_name}: {e}")
            return CodeAnalysisResult(
                module_name=module_name,
                complexity=0.0,
                quality_score=0.0,
                issues=[str(e)],
                suggestions=["Unable to analyze module"],
            )

    def register_observer(self, observer: SelfAwareObserver) -> None:
        """
        Register observer for self-awareness events.

        Args:
            observer: Observer to register
        """
        self._observers.append(observer)
        logger.debug(f"Registered observer: {observer.__class__.__name__}")

    def _calculate_uptime(self) -> float:
        """Calculate system uptime in seconds."""
        return 0.0

    def _determine_overall_health(self) -> ComponentHealth:
        """Determine overall system health."""
        unhealthy_count = sum(
            1 for cap in self._capabilities.values()
            if cap.health == ComponentHealth.UNHEALTHY
        )

        if unhealthy_count > 0:
            return ComponentHealth.UNHEALTHY

        degraded_count = sum(
            1 for cap in self._capabilities.values()
            if cap.health == ComponentHealth.DEGRADED
        )

        if degraded_count > len(self._capabilities) // 2:
            return ComponentHealth.DEGRADED

        return ComponentHealth.HEALTHY

    def _aggregate_metrics(self) -> Dict[str, float]:
        """Aggregate performance metrics."""
        aggregated = {}
        for metric_name, values in self._performance_metrics.items():
            if values:
                aggregated[f"{metric_name}_avg"] = sum(values) / len(values)
                aggregated[f"{metric_name}_max"] = max(values)
                aggregated[f"{metric_name}_min"] = min(values)
        return aggregated

    def _calculate_relevance(self, response: str, context: Context) -> float:
        """Calculate response relevance score."""
        query_words = set(context.query.lower().split())
        response_words = set(response.lower().split())
        overlap = len(query_words & response_words)
        return min(1.0, overlap / max(len(query_words), 1))

    def _calculate_coherence(self, response: str) -> float:
        """Calculate response coherence score."""
        sentences = response.split(".")
        if len(sentences) < 2:
            return 1.0
        return min(1.0, 0.7 + len(sentences) * 0.05)

    def _calculate_appropriateness(self, response: str, context: Context) -> float:
        """Calculate response appropriateness score."""
        inappropriate_terms = ["error", "fail", "crash", "bug"]
        term_count = sum(term in response.lower() for term in inappropriate_terms)
        return max(0.0, 1.0 - term_count * 0.2)

    def _calculate_confidence(self, metrics: Dict[str, float]) -> float:
        """Calculate confidence in evaluation."""
        variance = sum((v - 0.5) ** 2 for v in metrics.values()) / len(metrics)
        return 1.0 - variance

    def _generate_feedback(self, metrics: Dict[str, float]) -> str:
        """Generate feedback based on metrics."""
        low_metrics = [name for name, value in metrics.items() if value < 0.5]
        if not low_metrics:
            return "Response quality is good"
        return f"Consider improving: {', '.join(low_metrics)}"

    def _update_learning_metrics(self, interaction: Interaction) -> None:
        """Update learning metrics from interaction."""
        if interaction.evaluation:
            self._record_metric("response_quality", interaction.evaluation.score)

    def _check_capability_health(self, capability: str) -> ComponentHealth:
        """Check health of specific capability."""
        return ComponentHealth.HEALTHY

    def _get_capability_metrics(self, capability: str) -> Dict[str, float]:
        """Get metrics for specific capability."""
        return {}

    def _calculate_complexity(self, tree: Any) -> float:
        """Calculate code complexity."""
        return 1.0

    def _assess_code_quality(self, tree: Any, source: str) -> float:
        """Assess code quality."""
        return 0.8

    def _find_code_issues(self, tree: Any, source: str) -> List[str]:
        """Find code issues."""
        return []

    def _generate_suggestions(self, issues: List[str]) -> List[str]:
        """Generate improvement suggestions."""
        return []

    def _record_metric(self, name: str, value: float) -> None:
        """Record performance metric."""
        if name not in self._performance_metrics:
            self._performance_metrics[name] = []
        self._performance_metrics[name].append(value)
        if len(self._performance_metrics[name]) > 100:
            self._performance_metrics[name] = self._performance_metrics[name][-100:]

    def _notify_state_change(self, state: SystemState) -> None:
        """Notify observers of state change."""
        for observer in self._observers:
            try:
                observer.on_state_change(state)
            except Exception as e:
                logger.error(f"Error notifying observer: {e}")
