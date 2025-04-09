package cz.fit.cvut.feature.translation.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity

@Entity(
    tableName = "tolgee_translations",
    foreignKeys = [
        ForeignKey(
            entity = TolgeeKeyEntity::class,
            parentColumns = ["keyId"],
            childColumns = ["keyId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TolgeeLanguageEntity::class,
            parentColumns = ["languageId"],
            childColumns = ["languageId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["keyId", "languageId"], unique = true),
        Index(value = ["keyId"]),
        Index(value = ["languageId"])
    ]
)
internal data class TolgeeTranslationEntity(
    @PrimaryKey()
    @ColumnInfo(name = "translationId")
    val id: Long = 0,

    @ColumnInfo(name = "keyId")
    val keyId: Long,

    @ColumnInfo(name = "languageId")
    val languageId: Long,

    @ColumnInfo(name = "translationText")
    val text: String?
) 