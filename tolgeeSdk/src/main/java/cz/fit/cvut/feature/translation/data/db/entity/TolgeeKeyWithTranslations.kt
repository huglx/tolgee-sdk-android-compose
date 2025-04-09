package cz.fit.cvut.feature.translation.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a Tolgee key with its associated translations
 */
internal data class TolgeeKeyWithTranslations(
    @Embedded
    val key: TolgeeKeyEntity,

    @Relation(
        parentColumn = "keyId",
        entityColumn = "keyId"
    )
    val translations: List<TolgeeTranslationEntity> = emptyList()
) 