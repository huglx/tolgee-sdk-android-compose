package cz.fit.cvut.core.common.utils

import android.util.Log
import cz.fit.cvut.core.common.utils.Constants.END_MARKER
import cz.fit.cvut.core.common.utils.Constants.START_MARKER

internal fun encodeId(id: Long): String {
    val binaryString = id.toString(2)
    return binaryString.map {
        when (it) {
            '0' -> START_MARKER // Zero-Width Space
            '1' -> END_MARKER // Zero-Width Non-Joiner
            else -> throw IllegalStateException("Unexpected binary digit")
        }
    }.joinToString("")
}

/**
 * Decodes a binary ID from a string of invisible markers
 *
 * @param encodedText Text containing START_MARKER (0) and END_MARKER (1) characters
 * @return Decoded long value or null if no valid markers found
 */
internal fun decodeId(encodedText: String): Long? {
    val binaryString = encodedText.mapNotNull { char ->
        when (char) {
            START_MARKER -> '0'
            END_MARKER -> '1'
            else -> null  // Skip any other characters
        }
    }.joinToString("")

    if (binaryString.isEmpty()) {
        return null
    }

    return try {
        binaryString.toLong(2)
    } catch (e: Exception) {
        Log.e("TolgeeUtils", "Error decoding binary string: $binaryString", e)
        null
    }
}

/**
 * Encodes text using invisible markers
 * Text is first converted to a unique number using TextEncodingMap
 * Then the number is encoded using START_MARKER and END_MARKER
 * 
 * @param text Text to encode
 * @return Encoded text with invisible markers
 */
internal fun encodeText(text: String): String {
    val number = TextEncodingMap.encodeTextToNumber(text)
    val binaryString = number.toString(2)
    return binaryString.map {
        when (it) {
            '0' -> START_MARKER
            '1' -> END_MARKER
            else -> throw IllegalStateException("Unexpected binary digit")
        }
    }.joinToString("")
}

/**
 * Decodes text from a string of invisible markers
 * First decodes the number from markers
 * Then converts the number back to text using TextEncodingMap
 * 
 * @param encodedText Text containing START_MARKER and END_MARKER characters
 * @return Decoded text or null if no valid markers found or text mapping not found
 */
internal fun decodeText(encodedText: String): String? {
    val binaryString = encodedText.mapNotNull { char ->
        when (char) {
            START_MARKER -> '0'
            END_MARKER -> '1'
            else -> null  // Skip any other characters
        }
    }.joinToString("")

    if (binaryString.isEmpty()) {
        return null
    }

    return try {
        val number = binaryString.toInt(2)
        TextEncodingMap.decodeNumberToText(number)
    } catch (e: Exception) {
        Log.e("TolgeeUtils", "Error decoding text from binary string: $binaryString", e)
        null
    }
}

/**
 * Combines encoded ID and text into a single string
 * 
 * @param id ID to encode
 * @param text Text to encode
 * @return Combined encoded string
 */
internal fun encodeIdAndText(id: Long, text: String): String {
    return encodeId(id) + encodeText(text)
}

/**
 * Decodes both ID and text from a combined encoded string
 * 
 * @param encodedString Combined encoded string
 * @return Pair of decoded ID and text, or null if decoding fails
 */
internal fun decodeIdAndText(encodedString: String): Pair<Long, String>? {
    // First try to decode ID (it's at the start of the string)
    val id = decodeId(encodedString) ?: return null
    
    // Find where ID encoding ends and text encoding begins
    val idLength = id.toString(2).length
    val textStart = idLength
    
    // Decode the rest as text
    val text = decodeText(encodedString.substring(textStart)) ?: return null
    
    return Pair(id, text)
}

