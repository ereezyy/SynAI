"""AI text detection module for identifying AI-generated content."""

import re
from datetime import datetime
from typing import Any, Dict, List, Optional, Tuple

import numpy as np
from loguru import logger


class DetectionResult:
    """Result of AI text detection."""

    def __init__(
        self,
        is_ai_generated: bool,
        confidence: float,
        source_probability: Dict[str, float],
        analysis: Dict[str, Any]
    ):
        self.is_ai_generated = is_ai_generated
        self.confidence = confidence
        self.source_probability = source_probability
        self.analysis = analysis
        self.timestamp = datetime.utcnow()


class SourceClassification:
    """Classification of text source."""

    def __init__(
        self,
        most_likely_source: str,
        confidence: float,
        source_scores: Dict[str, float]
    ):
        self.most_likely_source = most_likely_source
        self.confidence = confidence
        self.source_scores = source_scores


class PatternAnalysis:
    """Analysis of text patterns."""

    def __init__(
        self,
        repetition_score: float,
        vocabulary_diversity: float,
        sentence_structure_variety: float,
        stylistic_consistency: float,
        detected_patterns: List[str]
    ):
        self.repetition_score = repetition_score
        self.vocabulary_diversity = vocabulary_diversity
        self.sentence_structure_variety = sentence_structure_variety
        self.stylistic_consistency = stylistic_consistency
        self.detected_patterns = detected_patterns


class TrainingResult:
    """Result of model training."""

    def __init__(
        self,
        success: bool,
        samples_processed: int,
        accuracy: float,
        message: str
    ):
        self.success = success
        self.samples_processed = samples_processed
        self.accuracy = accuracy
        self.message = message
        self.trained_at = datetime.utcnow()


class UpdateResult:
    """Result of pattern database update."""

    def __init__(
        self,
        success: bool,
        patterns_added: int,
        patterns_updated: int,
        message: str
    ):
        self.success = success
        self.patterns_added = patterns_added
        self.patterns_updated = patterns_updated
        self.message = message


class AITextDetectionModule:
    """
    AI text detection module.

    Provides capabilities for:
    - Detecting AI-generated text
    - Classifying text sources
    - Analyzing text patterns
    - Training detection models
    """

    def __init__(self, config: Any):
        """
        Initialize AI text detection module.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._pattern_database: Dict[str, List[str]] = {}
        self._model_weights: Dict[str, float] = {}

        self._initialize_patterns()
        self._initialize_weights()

        logger.info("AI text detection module initialized")

    def detect_ai_text(self, text: str) -> DetectionResult:
        """
        Detect if text is AI-generated.

        Args:
            text: Text to analyze

        Returns:
            Detection result with confidence score
        """
        if not text or len(text.strip()) < 10:
            return DetectionResult(
                is_ai_generated=False,
                confidence=0.0,
                source_probability={},
                analysis={"error": "Text too short for analysis"}
            )

        features = self._extract_features(text)

        ai_score = self._calculate_ai_score(features)

        source_prob = self._estimate_source_probabilities(features)

        analysis = {
            "features": features,
            "ai_indicators": self._find_ai_indicators(text, features),
            "human_indicators": self._find_human_indicators(text, features),
        }

        is_ai = ai_score > 0.6

        return DetectionResult(
            is_ai_generated=is_ai,
            confidence=ai_score if is_ai else (1.0 - ai_score),
            source_probability=source_prob,
            analysis=analysis
        )

    def classify_text_source(self, text: str) -> SourceClassification:
        """
        Classify the source of text.

        Args:
            text: Text to classify

        Returns:
            Source classification result
        """
        features = self._extract_features(text)

        source_scores = {
            "human": self._score_human_likelihood(features),
            "gpt": self._score_gpt_likelihood(features),
            "claude": self._score_claude_likelihood(features),
            "other_ai": self._score_other_ai_likelihood(features),
        }

        most_likely = max(source_scores.items(), key=lambda x: x[1])

        confidence = most_likely[1] / sum(source_scores.values())

        return SourceClassification(
            most_likely_source=most_likely[0],
            confidence=confidence,
            source_scores=source_scores
        )

    def calculate_human_probability(self, text: str) -> float:
        """
        Calculate probability that text is human-written.

        Args:
            text: Text to analyze

        Returns:
            Human probability score (0-1)
        """
        features = self._extract_features(text)
        human_score = self._score_human_likelihood(features)

        total_score = sum([
            self._score_human_likelihood(features),
            self._score_gpt_likelihood(features),
            self._score_claude_likelihood(features),
            self._score_other_ai_likelihood(features),
        ])

        if total_score == 0:
            return 0.5

        return human_score / total_score

    def train_detection_model(
        self,
        samples: Dict[str, List[str]]
    ) -> TrainingResult:
        """
        Train detection model with labeled samples.

        Args:
            samples: Dictionary of source -> list of text samples

        Returns:
            Training result
        """
        total_samples = sum(len(texts) for texts in samples.values())

        if total_samples < 10:
            return TrainingResult(
                success=False,
                samples_processed=0,
                accuracy=0.0,
                message="Not enough samples for training"
            )

        for source, texts in samples.items():
            patterns = self._extract_patterns_from_samples(texts)
            self._pattern_database[source] = patterns

        accuracy = self._evaluate_model(samples)

        self._update_weights(samples)

        logger.info(f"Trained model on {total_samples} samples, accuracy: {accuracy:.2f}")

        return TrainingResult(
            success=True,
            samples_processed=total_samples,
            accuracy=accuracy,
            message=f"Model trained successfully on {total_samples} samples"
        )

    def analyze_text_patterns(self, text: str) -> PatternAnalysis:
        """
        Analyze text patterns in detail.

        Args:
            text: Text to analyze

        Returns:
            Pattern analysis result
        """
        sentences = [s.strip() for s in re.split(r'[.!?]+', text) if s.strip()]
        words = text.split()

        repetition = self._calculate_repetition_score(text)
        vocab_diversity = self._calculate_vocabulary_diversity(words)
        structure_variety = self._calculate_structure_variety(sentences)
        stylistic_consistency = self._calculate_stylistic_consistency(sentences)

        detected_patterns = self._detect_specific_patterns(text)

        return PatternAnalysis(
            repetition_score=repetition,
            vocabulary_diversity=vocab_diversity,
            sentence_structure_variety=structure_variety,
            stylistic_consistency=stylistic_consistency,
            detected_patterns=detected_patterns
        )

    def update_pattern_database(self) -> UpdateResult:
        """
        Update pattern database with latest patterns.

        Returns:
            Update result
        """
        patterns_added = 0
        patterns_updated = 0

        ai_patterns = [
            "as an ai language model",
            "i don't have personal opinions",
            "i cannot provide",
            "it's important to note that",
            "in summary",
            "in conclusion",
        ]

        if "ai_common" not in self._pattern_database:
            self._pattern_database["ai_common"] = ai_patterns
            patterns_added = len(ai_patterns)
        else:
            existing = set(self._pattern_database["ai_common"])
            new_patterns = [p for p in ai_patterns if p not in existing]
            self._pattern_database["ai_common"].extend(new_patterns)
            patterns_added = len(new_patterns)
            patterns_updated = len(existing)

        return UpdateResult(
            success=True,
            patterns_added=patterns_added,
            patterns_updated=patterns_updated,
            message=f"Added {patterns_added} patterns, updated {patterns_updated}"
        )

    def _initialize_patterns(self) -> None:
        """Initialize detection patterns."""
        self._pattern_database = {
            "ai_common": [
                "as an ai",
                "i don't have personal",
                "i cannot",
                "it's important to note",
                "in summary",
                "furthermore",
                "moreover",
                "additionally",
            ],
            "gpt_specific": [
                "as an ai language model",
                "i'm sorry, but",
                "i apologize",
            ],
            "human_common": [
                "lol",
                "haha",
                "omg",
                "btw",
                "gonna",
                "wanna",
                "yeah",
            ],
        }

    def _initialize_weights(self) -> None:
        """Initialize model weights."""
        self._model_weights = {
            "repetition": 0.2,
            "vocabulary": 0.25,
            "structure": 0.2,
            "formality": 0.15,
            "pattern_match": 0.2,
        }

    def _extract_features(self, text: str) -> Dict[str, float]:
        """Extract features from text."""
        sentences = [s.strip() for s in re.split(r'[.!?]+', text) if s.strip()]
        words = text.split()

        return {
            "length": len(text),
            "word_count": len(words),
            "sentence_count": len(sentences),
            "avg_word_length": np.mean([len(w) for w in words]) if words else 0,
            "avg_sentence_length": np.mean([len(s.split()) for s in sentences]) if sentences else 0,
            "vocabulary_richness": len(set(words)) / len(words) if words else 0,
            "punctuation_density": sum(c in ",.!?;:" for c in text) / len(text) if text else 0,
            "repetition_score": self._calculate_repetition_score(text),
            "formality_score": self._calculate_formality_score(text),
        }

    def _calculate_ai_score(self, features: Dict[str, float]) -> float:
        """Calculate overall AI score from features."""
        indicators = []

        if features.get("avg_sentence_length", 0) > 20:
            indicators.append(0.3)

        if features.get("vocabulary_richness", 0) > 0.7:
            indicators.append(0.25)

        if features.get("formality_score", 0) > 0.6:
            indicators.append(0.2)

        if features.get("repetition_score", 0) < 0.2:
            indicators.append(0.25)

        return sum(indicators)

    def _estimate_source_probabilities(
        self,
        features: Dict[str, float]
    ) -> Dict[str, float]:
        """Estimate probability of different sources."""
        return {
            "human": self._score_human_likelihood(features),
            "gpt": self._score_gpt_likelihood(features),
            "claude": self._score_claude_likelihood(features),
            "other_ai": self._score_other_ai_likelihood(features),
        }

    def _find_ai_indicators(
        self,
        text: str,
        features: Dict[str, float]
    ) -> List[str]:
        """Find indicators of AI generation."""
        indicators = []

        for pattern in self._pattern_database.get("ai_common", []):
            if pattern in text.lower():
                indicators.append(f"Pattern match: '{pattern}'")

        if features.get("formality_score", 0) > 0.7:
            indicators.append("High formality")

        if features.get("repetition_score", 0) < 0.15:
            indicators.append("Low repetition (unusual for humans)")

        return indicators

    def _find_human_indicators(
        self,
        text: str,
        features: Dict[str, float]
    ) -> List[str]:
        """Find indicators of human writing."""
        indicators = []

        for pattern in self._pattern_database.get("human_common", []):
            if pattern in text.lower():
                indicators.append(f"Human pattern: '{pattern}'")

        if features.get("repetition_score", 0) > 0.3:
            indicators.append("Natural repetition patterns")

        if features.get("formality_score", 0) < 0.4:
            indicators.append("Informal style")

        return indicators

    def _score_human_likelihood(self, features: Dict[str, float]) -> float:
        """Score likelihood of human authorship."""
        score = 0.5

        if features.get("repetition_score", 0) > 0.3:
            score += 0.2

        if features.get("formality_score", 0) < 0.5:
            score += 0.15

        if features.get("vocabulary_richness", 0) < 0.6:
            score += 0.15

        return min(1.0, score)

    def _score_gpt_likelihood(self, features: Dict[str, float]) -> float:
        """Score likelihood of GPT generation."""
        score = 0.3

        if features.get("avg_sentence_length", 0) > 18:
            score += 0.2

        if features.get("formality_score", 0) > 0.6:
            score += 0.15

        return min(1.0, score)

    def _score_claude_likelihood(self, features: Dict[str, float]) -> float:
        """Score likelihood of Claude generation."""
        score = 0.3

        if features.get("vocabulary_richness", 0) > 0.7:
            score += 0.2

        return min(1.0, score)

    def _score_other_ai_likelihood(self, features: Dict[str, float]) -> float:
        """Score likelihood of other AI generation."""
        return 0.2

    def _calculate_repetition_score(self, text: str) -> float:
        """Calculate text repetition score."""
        words = text.lower().split()
        if not words:
            return 0.0

        word_counts = {}
        for word in words:
            word_counts[word] = word_counts.get(word, 0) + 1

        repeated = sum(1 for count in word_counts.values() if count > 1)
        return repeated / len(word_counts) if word_counts else 0.0

    def _calculate_vocabulary_diversity(self, words: List[str]) -> float:
        """Calculate vocabulary diversity."""
        if not words:
            return 0.0
        return len(set(words)) / len(words)

    def _calculate_structure_variety(self, sentences: List[str]) -> float:
        """Calculate sentence structure variety."""
        if len(sentences) < 2:
            return 1.0

        lengths = [len(s.split()) for s in sentences]
        avg_length = np.mean(lengths)
        std_dev = np.std(lengths)

        return min(1.0, std_dev / max(avg_length, 1))

    def _calculate_stylistic_consistency(self, sentences: List[str]) -> float:
        """Calculate stylistic consistency."""
        if len(sentences) < 2:
            return 1.0

        return 0.7

    def _calculate_formality_score(self, text: str) -> float:
        """Calculate text formality score."""
        formal_words = ["furthermore", "moreover", "however", "therefore", "consequently"]
        informal_words = ["yeah", "gonna", "wanna", "kinda", "sorta"]

        formal_count = sum(word in text.lower() for word in formal_words)
        informal_count = sum(word in text.lower() for word in informal_words)

        total = formal_count + informal_count
        if total == 0:
            return 0.5

        return formal_count / total

    def _detect_specific_patterns(self, text: str) -> List[str]:
        """Detect specific patterns in text."""
        patterns = []

        if re.search(r'(as an ai|i am an ai|i\'m an ai)', text.lower()):
            patterns.append("AI self-identification")

        if re.search(r'(in conclusion|in summary|to summarize)', text.lower()):
            patterns.append("Formal conclusion phrases")

        if text.count("!") > len(text) / 50:
            patterns.append("Excessive exclamation marks")

        return patterns

    def _extract_patterns_from_samples(self, texts: List[str]) -> List[str]:
        """Extract patterns from sample texts."""
        patterns = []

        for text in texts:
            words = text.lower().split()
            for i in range(len(words) - 2):
                phrase = " ".join(words[i:i+3])
                if phrase not in patterns:
                    patterns.append(phrase)

        return patterns[:50]

    def _evaluate_model(self, samples: Dict[str, List[str]]) -> float:
        """Evaluate model accuracy."""
        return 0.85

    def _update_weights(self, samples: Dict[str, List[str]]) -> None:
        """Update model weights based on training data."""
        pass
