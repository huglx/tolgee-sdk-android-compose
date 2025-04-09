package cz.fit.cvut.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fit.cvut.feature.language.data.db.dao.LanguageDao
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.IOException

abstract class TestTolgeeDB {
    internal lateinit var db: TolgeeDB

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            TolgeeDB::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
} 