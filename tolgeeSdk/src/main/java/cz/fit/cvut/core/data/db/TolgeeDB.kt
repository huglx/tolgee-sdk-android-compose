package cz.fit.cvut.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.fit.cvut.feature.language.data.db.dao.LanguageDao
import cz.fit.cvut.feature.translation.data.db.dao.TolgeeDao
import cz.fit.cvut.feature.translation.data.db.entity.TolgeeKeyEntity
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity
import cz.fit.cvut.feature.translation.data.db.entity.TolgeeTranslationEntity

@Database(
    entities = [
        TolgeeKeyEntity::class,
        TolgeeTranslationEntity::class,
        TolgeeLanguageEntity::class,
    ],
    version = 9,
    exportSchema = true
)
internal abstract class TolgeeDB : RoomDatabase() {
    abstract fun tolgeeDao(): TolgeeDao
    abstract fun languageDao(): LanguageDao

    companion object {
        @Volatile
        private var INSTANCE: TolgeeDB? = null

        fun instance(context: Context): TolgeeDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TolgeeDB::class.java,
                    "tolgee_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
