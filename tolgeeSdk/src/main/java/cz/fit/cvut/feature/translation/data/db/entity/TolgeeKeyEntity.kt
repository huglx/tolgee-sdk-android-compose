package cz.fit.cvut.feature.translation.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a key in the Tolgee translation system
 */
@Entity(tableName = "tolgee_keys")
internal data class TolgeeKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "keyId")
    val keyId: Long = 0,
    @ColumnInfo(name = "keyName")
    val keyName: String = ""
)
