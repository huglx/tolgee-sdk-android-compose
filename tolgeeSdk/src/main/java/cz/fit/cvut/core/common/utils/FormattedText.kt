package cz.fit.cvut.core.common.utils

import android.icu.text.MessageFormat
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml

internal fun formattedText(
    localizedString: String,
    params: Map<String, Any>?
): AnnotatedString {
    return try {
        val formattedString = MessageFormat(localizedString).format(params)
        AnnotatedString.fromHtml(formattedString)
    } catch (e: Exception) {
        throw(IllegalArgumentException("Error formatting string: $localizedString. " + e.message))
    }
}