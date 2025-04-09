package cz.fit.cvut.feature.translations_context.domain

import androidx.compose.ui.geometry.Rect

internal data class TolgeeKeyMeta(
    val keyId: Long? = null,
    val keyName: String? = null,
    val namespace: String?,
    val position: Rect,
    val screenId: String = ""
)
