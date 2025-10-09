"""Text humanization module for natural-sounding responses."""

import random
import re
from typing import Any, Dict, List, Optional

from loguru import logger


class TextPersona:
    """Text generation persona configuration."""

    def __init__(
        self,
        name: str,
        formality: float = 0.5,
        enthusiasm: float = 0.5,
        verbosity: float = 0.5,
        use_contractions: bool = True,
        use_filler_words: bool = True,
        personality_traits: Optional[List[str]] = None
    ):
        self.name = name
        self.formality = max(0.0, min(1.0, formality))
        self.enthusiasm = max(0.0, min(1.0, enthusiasm))
        self.verbosity = max(0.0, min(1.0, verbosity))
        self.use_contractions = use_contractions
        self.use_filler_words = use_filler_words
        self.personality_traits = personality_traits or []


class StyleParameters:
    """Style adjustment parameters."""

    def __init__(
        self,
        tone: str = "neutral",
        formality_level: int = 3,
        add_empathy: bool = False,
        add_humor: bool = False,
        word_variety: float = 0.5
    ):
        self.tone = tone
        self.formality_level = max(1, min(5, formality_level))
        self.add_empathy = add_empathy
        self.add_humor = add_humor
        self.word_variety = max(0.0, min(1.0, word_variety))


class HumanizationContext:
    """Context for text humanization."""

    def __init__(
        self,
        user_id: str,
        conversation_history: List[str],
        user_preferences: Dict[str, Any],
        emotional_state: Optional[str] = None
    ):
        self.user_id = user_id
        self.conversation_history = conversation_history
        self.user_preferences = user_preferences
        self.emotional_state = emotional_state or "neutral"


class PatternAnalysisResult:
    """Result of pattern analysis."""

    def __init__(
        self,
        avg_sentence_length: float,
        vocabulary_richness: float,
        formality_score: float,
        common_phrases: List[str],
        writing_style: str
    ):
        self.avg_sentence_length = avg_sentence_length
        self.vocabulary_richness = vocabulary_richness
        self.formality_score = formality_score
        self.common_phrases = common_phrases
        self.writing_style = writing_style


class TextStyle:
    """Text style configuration."""

    def __init__(
        self,
        name: str,
        description: str,
        example: str,
        parameters: StyleParameters
    ):
        self.name = name
        self.description = description
        self.example = example
        self.parameters = parameters


class TextHumanizationModule:
    """
    Text humanization module for natural language generation.

    Provides capabilities for:
    - Converting AI text to human-like text
    - Adjusting tone and style
    - Adding natural variations
    - Personalizing for users
    """

    def __init__(self, config: Any):
        """
        Initialize text humanization module.

        Args:
            config: Configuration manager instance
        """
        self.config = config
        self._current_persona = TextPersona("default")
        self._user_personas: Dict[str, TextPersona] = {}

        self._filler_words = self._load_filler_words()
        self._contractions = self._load_contractions()
        self._transition_phrases = self._load_transition_phrases()
        self._empathy_phrases = self._load_empathy_phrases()
        self._humor_elements = self._load_humor_elements()

        logger.info("Text humanization module initialized")

    def humanize_text(
        self,
        text: str,
        context: HumanizationContext
    ) -> str:
        """
        Humanize AI-generated text.

        Args:
            text: Text to humanize
            context: Humanization context

        Returns:
            Humanized text
        """
        if not text:
            return text

        persona = self._user_personas.get(context.user_id, self._current_persona)

        result = text

        result = self._add_natural_variations(result, persona)

        if persona.use_contractions:
            result = self._apply_contractions(result)

        if persona.use_filler_words and persona.formality < 0.7:
            result = self._add_filler_words(result, persona)

        result = self._adjust_sentence_structure(result, persona)

        result = self._add_personality_touches(result, persona)

        result = self._match_emotional_tone(result, context.emotional_state)

        logger.debug(f"Humanized text for user {context.user_id}")
        return result

    def set_persona(self, persona: TextPersona) -> None:
        """
        Set current persona.

        Args:
            persona: Persona to set
        """
        self._current_persona = persona
        logger.info(f"Set persona: {persona.name}")

    def adjust_style(self, style_params: StyleParameters) -> None:
        """
        Adjust text generation style.

        Args:
            style_params: Style parameters
        """
        self._current_persona.formality = (style_params.formality_level - 1) / 4.0
        logger.debug(f"Adjusted style: {style_params.tone}")

    def analyze_human_patterns(self, samples: List[str]) -> PatternAnalysisResult:
        """
        Analyze human writing patterns from samples.

        Args:
            samples: Sample texts to analyze

        Returns:
            Pattern analysis result
        """
        if not samples:
            return PatternAnalysisResult(
                avg_sentence_length=15.0,
                vocabulary_richness=0.5,
                formality_score=0.5,
                common_phrases=[],
                writing_style="neutral"
            )

        total_sentences = 0
        total_words = 0
        all_words = []

        for sample in samples:
            sentences = [s.strip() for s in re.split(r'[.!?]+', sample) if s.strip()]
            total_sentences += len(sentences)

            words = sample.split()
            total_words += len(words)
            all_words.extend(words)

        avg_sentence_length = total_words / max(total_sentences, 1)

        unique_words = len(set(all_words))
        vocabulary_richness = unique_words / max(len(all_words), 1)

        formality_score = self._calculate_formality(samples)

        common_phrases = self._find_common_phrases(samples)

        writing_style = self._determine_writing_style(
            avg_sentence_length,
            formality_score,
            vocabulary_richness
        )

        return PatternAnalysisResult(
            avg_sentence_length=avg_sentence_length,
            vocabulary_richness=vocabulary_richness,
            formality_score=formality_score,
            common_phrases=common_phrases,
            writing_style=writing_style
        )

    def get_available_styles(self) -> List[TextStyle]:
        """
        Get available text styles.

        Returns:
            List of available styles
        """
        return [
            TextStyle(
                name="casual",
                description="Casual, friendly tone",
                example="Hey! That sounds great. Let me help you with that.",
                parameters=StyleParameters(tone="casual", formality_level=2)
            ),
            TextStyle(
                name="professional",
                description="Professional, business-like tone",
                example="I would be happy to assist you with that request.",
                parameters=StyleParameters(tone="professional", formality_level=4)
            ),
            TextStyle(
                name="friendly",
                description="Warm, approachable tone",
                example="I'd love to help you out! What can I do for you?",
                parameters=StyleParameters(
                    tone="friendly",
                    formality_level=3,
                    add_empathy=True
                )
            ),
            TextStyle(
                name="concise",
                description="Brief, to-the-point tone",
                example="Got it. I'll handle that.",
                parameters=StyleParameters(tone="concise", formality_level=3)
            )
        ]

    def personalize_for_user(self, user_id: str) -> None:
        """
        Personalize humanization for specific user.

        Args:
            user_id: User identifier
        """
        if user_id not in self._user_personas:
            self._user_personas[user_id] = TextPersona(
                name=f"user_{user_id}",
                formality=0.5,
                enthusiasm=0.5,
                verbosity=0.5
            )
            logger.debug(f"Created persona for user {user_id}")

    def _load_filler_words(self) -> List[str]:
        """Load filler words for natural speech."""
        return [
            "well", "actually", "basically", "honestly", "literally",
            "you know", "I mean", "like", "sort of", "kind of",
            "so", "anyway", "right", "okay"
        ]

    def _load_contractions(self) -> Dict[str, str]:
        """Load common contractions."""
        return {
            "I am": "I'm",
            "you are": "you're",
            "he is": "he's",
            "she is": "she's",
            "it is": "it's",
            "we are": "we're",
            "they are": "they're",
            "I have": "I've",
            "you have": "you've",
            "we have": "we've",
            "they have": "they've",
            "I will": "I'll",
            "you will": "you'll",
            "he will": "he'll",
            "she will": "she'll",
            "we will": "we'll",
            "they will": "they'll",
            "cannot": "can't",
            "do not": "don't",
            "does not": "doesn't",
            "did not": "didn't",
            "is not": "isn't",
            "are not": "aren't",
            "was not": "wasn't",
            "were not": "weren't",
            "have not": "haven't",
            "has not": "hasn't",
            "had not": "hadn't",
            "will not": "won't",
            "would not": "wouldn't",
            "should not": "shouldn't",
            "could not": "couldn't"
        }

    def _load_transition_phrases(self) -> List[str]:
        """Load transition phrases."""
        return [
            "By the way,",
            "Speaking of which,",
            "On another note,",
            "Additionally,",
            "Also,",
            "Furthermore,",
            "Moreover,",
            "In addition,",
            "That said,",
            "Having said that,"
        ]

    def _load_empathy_phrases(self) -> List[str]:
        """Load empathy phrases."""
        return [
            "I understand",
            "That makes sense",
            "I see what you mean",
            "I hear you",
            "That's understandable",
            "I can imagine",
            "That must be",
            "I appreciate that"
        ]

    def _load_humor_elements(self) -> List[str]:
        """Load humor elements."""
        return [
            "😊", "😄", "😅",
            "Ha!", "Haha",
            "That's funny!",
            "Good one!",
            "I see what you did there"
        ]

    def _add_natural_variations(self, text: str, persona: TextPersona) -> str:
        """Add natural variations to text."""
        sentences = [s.strip() for s in re.split(r'([.!?]+)', text) if s.strip()]

        result = []
        for i, sentence in enumerate(sentences):
            if sentence in '.!?':
                result.append(sentence)
                continue

            if random.random() < 0.3 and i < len(sentences) - 1:
                if random.random() < 0.5:
                    sentence = sentence + ","
                else:
                    sentence = sentence + " -"

            result.append(sentence)

        return " ".join(result)

    def _apply_contractions(self, text: str) -> str:
        """Apply contractions to text."""
        result = text
        for full, contracted in self._contractions.items():
            result = re.sub(
                r'\b' + re.escape(full) + r'\b',
                contracted,
                result,
                flags=re.IGNORECASE
            )
        return result

    def _add_filler_words(self, text: str, persona: TextPersona) -> str:
        """Add filler words naturally."""
        sentences = text.split(". ")
        result = []

        for sentence in sentences:
            if random.random() < 0.2 * (1 - persona.formality):
                filler = random.choice(self._filler_words)
                if not sentence.lower().startswith(filler):
                    sentence = f"{filler.capitalize()}, {sentence.lower()}"

            result.append(sentence)

        return ". ".join(result)

    def _adjust_sentence_structure(self, text: str, persona: TextPersona) -> str:
        """Adjust sentence structure based on persona."""
        if persona.verbosity > 0.7:
            text = self._add_elaboration(text)
        elif persona.verbosity < 0.3:
            text = self._make_concise(text)

        return text

    def _add_elaboration(self, text: str) -> str:
        """Add elaboration to text."""
        return text

    def _make_concise(self, text: str) -> str:
        """Make text more concise."""
        text = re.sub(r'\s+', ' ', text)
        text = re.sub(r',\s+', ', ', text)
        return text.strip()

    def _add_personality_touches(self, text: str, persona: TextPersona) -> str:
        """Add personality-specific touches."""
        if "helpful" in persona.personality_traits:
            if not any(phrase in text.lower() for phrase in ["let me", "i can", "i'll"]):
                text = "Let me help you with that. " + text

        if "enthusiastic" in persona.personality_traits:
            text = text.replace(".", "!")

        return text

    def _match_emotional_tone(self, text: str, emotional_state: str) -> str:
        """Match emotional tone of response."""
        if emotional_state == "happy":
            text = text.replace("good", "great")
            text = text.replace("okay", "wonderful")
        elif emotional_state == "sad":
            if random.random() < 0.5:
                empathy = random.choice(self._empathy_phrases)
                text = f"{empathy}. {text}"

        return text

    def _calculate_formality(self, samples: List[str]) -> float:
        """Calculate formality score."""
        formal_indicators = ["please", "thank you", "kindly", "regarding", "furthermore"]
        informal_indicators = ["hey", "yeah", "gonna", "wanna", "stuff"]

        formal_count = sum(
            text.lower().count(word)
            for text in samples
            for word in formal_indicators
        )

        informal_count = sum(
            text.lower().count(word)
            for text in samples
            for word in informal_indicators
        )

        total = formal_count + informal_count
        if total == 0:
            return 0.5

        return formal_count / total

    def _find_common_phrases(self, samples: List[str]) -> List[str]:
        """Find common phrases in samples."""
        phrase_counts: Dict[str, int] = {}

        for sample in samples:
            words = sample.lower().split()
            for i in range(len(words) - 2):
                phrase = " ".join(words[i:i+3])
                phrase_counts[phrase] = phrase_counts.get(phrase, 0) + 1

        common = sorted(phrase_counts.items(), key=lambda x: x[1], reverse=True)
        return [phrase for phrase, count in common[:10] if count > 1]

    def _determine_writing_style(
        self,
        avg_length: float,
        formality: float,
        vocabulary: float
    ) -> str:
        """Determine overall writing style."""
        if formality > 0.7 and avg_length > 20:
            return "formal"
        elif formality < 0.3 and avg_length < 15:
            return "casual"
        elif vocabulary > 0.7:
            return "sophisticated"
        else:
            return "neutral"
