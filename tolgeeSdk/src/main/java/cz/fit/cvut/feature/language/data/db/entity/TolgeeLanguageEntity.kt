package cz.fit.cvut.feature.language.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tolgee_languages")
internal data class TolgeeLanguageEntity(
    @PrimaryKey
    @ColumnInfo(name = "languageId")
    val id: Long,

    @ColumnInfo(name = "languageName")
    val name: String,

    @ColumnInfo(name = "originalName")
    val originalName: String,

    @ColumnInfo(name = "languageTag")
    val tag: String,

    @ColumnInfo(name = "flagEmoji")
    val flagEmoji: String,

    @ColumnInfo(name = "isBase")
    val isBase: Boolean
)