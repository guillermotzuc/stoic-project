package com.proyecto360.health.data

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

class PhraseRepository(context: Context) {
    private val appContext = context.applicationContext

    private val phrases: List<StoicPhrase> by lazy { loadPhrases() }

    fun phraseOfTheDay(): StoicPhrase {
        if (phrases.isEmpty()) {
            return StoicPhrase("Vive de acuerdo con la naturaleza.")
        }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return phrases[dayOfYear % phrases.size]
    }

    fun randomPhrase(excluding: String? = null): StoicPhrase {
        if (phrases.isEmpty()) {
            return StoicPhrase("Vive de acuerdo con la naturaleza.")
        }
        if (phrases.size == 1) return phrases.first()
        var candidate = phrases.random()
        var attempts = 0
        while (candidate.phrase == excluding && attempts < 10) {
            candidate = phrases.random()
            attempts++
        }
        return candidate
    }

    private fun loadPhrases(): List<StoicPhrase> {
        return try {
            appContext.assets.open("stoicphrases.json").bufferedReader().use { reader ->
                val json = JSONArray(reader.readText())
                buildList {
                    for (i in 0 until json.length()) {
                        val obj = json.getJSONObject(i)
                        val text = obj.optString("phrase").trim()
                        if (text.isNotEmpty()) add(StoicPhrase(text))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
