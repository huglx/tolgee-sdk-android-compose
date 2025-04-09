package cz.fit.cvut.core.common.utils

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton object for managing text encoding/decoding mappings
 */
internal object TextEncodingMap {
    private val textToNumber = ConcurrentHashMap<String, Int>()
    private val numberToText = ConcurrentHashMap<Int, String>()
    private var nextNumber = 1

    /**
     * Encodes text to a unique number
     * If text already exists in mapping, returns existing number
     * Otherwise creates new mapping
     */
    fun encodeTextToNumber(text: String): Int {
        return textToNumber.getOrPut(text) {
            val number = nextNumber++
            numberToText[number] = text
            number
        }
    }

    /**
     * Decodes number back to text
     * Returns null if number is not found in mapping
     */
    fun decodeNumberToText(number: Int): String? {
        return numberToText[number]
    }

    /**
     * Clears all mappings
     */
    fun clear() {
        textToNumber.clear()
        numberToText.clear()
        nextNumber = 1
    }
} 