package cz.fit.cvut.feature.translation.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity

internal data class TolgeeTranslationWithLanguage(
    @Embedded
    val translation: TolgeeTranslationEntity,

    @Relation(
        parentColumn = "languageId",
        entityColumn = "languageId"
    )
    val language: TolgeeLanguageEntity
) 