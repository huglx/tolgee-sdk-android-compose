package cz.fit.cvut.feature.init.data.source

import android.content.Context
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Implementation that reads translations from assets
 */
internal class AssetPreloadedTranslationsDataSource(
    private val context: Context,
    private val sdkMode: TolgeeSdkMode
) : PreloadedTranslationsDataSource {

    companion object {
        private const val PRELOADED_SQL_FILENAME = "preloaded_translations.sql"
    }

    override fun isAvailable(): Boolean {
        // Only available in RELEASE mode and if SQL file exists
        if (sdkMode != TolgeeSdkMode.RELEASE) {
            return false
        }

        return assetExists(PRELOADED_SQL_FILENAME)
    }

    override suspend fun getSqlStatements(): ResultWrapper<List<String>> {
        if (!isAvailable()) {
            return ResultWrapper.Error("Preloaded translations are not available")
        }

        try {
            // Read the SQL file
            val sqlScript = readAssetFile(PRELOADED_SQL_FILENAME)

            // Split the SQL script into individual statements
            val statements = sqlScript.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { "$it;" } // Add back the semicolon

            return ResultWrapper.Success(statements)
        } catch (e: Exception) {
            return ResultWrapper.Error(
                message = "Failed to load preloaded translations SQL: ${e.message}",
                throwable = e
            )
        }
    }

    /**
     * Read the content of an asset file as a string
     */
    private fun readAssetFile(filename: String): String {
        return context.assets.open(filename).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }

    /**
     * Check if an asset file exists
     */
    private fun assetExists(assetName: String): Boolean {
        return try {
            context.assets.open(assetName).use { true }
        } catch (e: Exception) {
            false
        }
    }
}