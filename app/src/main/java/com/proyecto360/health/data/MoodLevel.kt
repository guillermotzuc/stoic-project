package com.proyecto360.health.data

enum class MoodLevel(
    val id: String,
    val emoji: String,
    val weather: String,
    val label: String,
    val description: String,
    /** 5 = mejor (radiante), 1 = más bajo (ansioso) */
    val score: Int
) {
    RADIANT(
        id = "radiant",
        emoji = "😄",
        weather = "☀️",
        label = "Radiante / Muy feliz",
        description = "Estado óptimo, alta satisfacción, energía positiva.",
        score = 5
    ),
    NEUTRAL_POSITIVE(
        id = "neutral_positive",
        emoji = "🙂",
        weather = "🌤️",
        label = "Neutro-positivo",
        description = "Tranquilo, en paz, día normal con buen balance.",
        score = 4
    ),
    NEUTRAL(
        id = "neutral",
        emoji = "😐",
        weather = "☁️",
        label = "Neutro",
        description = "Apático, indiferente o fatigado sin carga emocional fuerte.",
        score = 3
    ),
    SAD(
        id = "sad",
        emoji = "😔",
        weather = "🌧️",
        label = "Triste / Desanimado",
        description = "Conectado con la pena, falta de energía o desmotivación.",
        score = 2
    ),
    ANXIOUS(
        id = "anxious",
        emoji = "😰",
        weather = "🌩️",
        label = "Ansiado / Abrumado",
        description = "Altos niveles de estrés, preocupación o sobreestimulación.",
        score = 1
    );

    companion object {
        fun fromId(id: String): MoodLevel? = entries.find { it.id == id }

        fun nearestByScore(average: Double): MoodLevel {
            val rounded = average.toInt().coerceIn(1, 5)
            return entries.minBy { kotlin.math.abs(it.score - rounded) }
        }
    }
}
