//package cz.fit.cvut.feature.testing
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
//import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
//import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
//import cz.fit.cvut.feature.translation.data.TranslationsRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import android.app.ActivityManager
//import android.content.Context
//import android.os.Debug
//import android.util.Log
//import java.text.SimpleDateFormat
//import java.util.*
//
//class StressTestViewModel(
//    private val translationsRepository: TranslationsRepository
//) : ViewModel() {
//    private val _state = MutableStateFlow<StressTestState>(StressTestState.Idle)
//    val state = _state.asStateFlow()
//
//    private val _memoryUsage = MutableStateFlow<String>("")
//    val memoryUsage = _memoryUsage.asStateFlow()
//
//    private val testLanguages = listOf(
//        TolgeeLanguageModel(
//            id = 1,
//            name = "English",
//            originalName = "English",
//            tag = "en",
//            flagEmoji = "🇬🇧",
//            isBase = true
//        ),
//        TolgeeLanguageModel(
//            id = 2,
//            name = "Czech",
//            originalName = "Čeština",
//            tag = "cs",
//            flagEmoji = "🇨🇿",
//            isBase = false
//        )
//    )
//
//    fun runBatchInsertTest(context: Context, keyCount: Int) {
//        viewModelScope.launch {
//            _state.value = StressTestState.Running
//            val startTime = System.currentTimeMillis()
//
//            try {
//                // Create test keys
//                val keys = (1..keyCount).map { id ->
//                    TolgeeKeyModel(
//                        keyId = id.toLong(),
//                        keyName = "test.key.$id",
//                        translations = testLanguages.associate { lang ->
//                            lang.tag to TolgeeTranslationModel(
//                                text = "Translation for key $id in ${lang.name}",
//                                language = lang
//                            )
//                        }
//                    )
//                }
//
//                // Save keys and measure time
//                val insertTime = measureTimeBlock {
//                    translationsRepository.clearLocalCache()
//                    keys.forEach { key ->
//                        translationsRepository.saveKey(key)
//                    }
//                }
//
//                // Measure memory usage
//                updateMemoryUsage(context)
//
//                val endTime = System.currentTimeMillis()
//                _state.value = StressTestState.Completed(
//                    totalTime = endTime - startTime,
//                    insertTime = insertTime,
//                    keyCount = keyCount
//                )
//            } catch (e: Exception) {
//                _state.value = StressTestState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    fun runRenderTest(context: Context, keyCount: Int) {
//        viewModelScope.launch {
//            _state.value = StressTestState.Running
//            val startTime = System.currentTimeMillis()
//
//            try {
//                // Create and save test keys first
//                val keys = (1..keyCount).map { id ->
//                    TolgeeKeyModel(
//                        keyId = id.toLong(),
//                        keyName = "test.key.$id",
//                        translations = testLanguages.associate { lang ->
//                            lang.tag to TolgeeTranslationModel(
//                                text = "Translation for key $id in ${lang.name}",
//                                language = lang
//                            )
//                        }
//                    )
//                }
//
//                translationsRepository.clearLocalCache()
//                keys.forEach { key ->
//                    translationsRepository.saveKey(key)
//                }
//
//                // Measure render time
//                val renderTime = measureTimeBlock {
//                    // Force UI update by getting all keys
//                    translationsRepository.getAllKeys().collect { _ ->
//                        // Simulate UI render time
//                        delay(16) // Simulate one frame time
//                    }
//                }
//
//                // Measure memory usage
//                updateMemoryUsage(context)
//
//                val endTime = System.currentTimeMillis()
//                _state.value = StressTestState.Completed(
//                    totalTime = endTime - startTime,
//                    insertTime = renderTime,
//                    keyCount = keyCount
//                )
//            } catch (e: Exception) {
//                _state.value = StressTestState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    private fun updateMemoryUsage(context: Context) {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val memoryInfo = ActivityManager.MemoryInfo()
//        activityManager.getMemoryInfo(memoryInfo)
//
//        val nativeHeapSize = Debug.getNativeHeapSize()
//        val nativeHeapFreeSize = Debug.getNativeHeapFreeSize()
//        val usedMemInBytes = nativeHeapSize - nativeHeapFreeSize
//
//        val memoryUsage = """
//            App Memory Usage: ${usedMemInBytes / 1024 / 1024} MB
//            Available Memory: ${memoryInfo.availMem / 1024 / 1024} MB
//            Total Memory: ${memoryInfo.totalMem / 1024 / 1024} MB
//            Low Memory: ${memoryInfo.lowMemory}
//        """.trimIndent()
//
//        _memoryUsage.value = memoryUsage
//
//        // Log detailed memory info
//        Log.d("StressTest", """
//            Memory Usage Report (${getCurrentTimestamp()}):
//            $memoryUsage
//            Native Heap Size: ${nativeHeapSize / 1024 / 1024} MB
//            Native Heap Free: ${nativeHeapFreeSize / 1024 / 1024} MB
//        """.trimIndent())
//    }
//
//    private fun getCurrentTimestamp(): String {
//        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
//    }
//
//    private suspend fun measureTimeBlock(block: suspend () -> Unit): Long {
//        val startTime = System.nanoTime()
//        block()
//        val endTime = System.nanoTime()
//        return (endTime - startTime) / 1_000_000 // Convert to milliseconds
//    }
//}
//
//sealed interface StressTestState {
//    object Idle : StressTestState
//    object Running : StressTestState
//    data class Completed(
//        val totalTime: Long,
//        val insertTime: Long,
//        val keyCount: Int
//    ) : StressTestState
//    data class Error(val message: String) : StressTestState
//}
//
//@Composable
//fun StressTestScreen(
//    viewModel: StressTestViewModel
//) {
//    val state by viewModel.state.collectAsState()
//    val memoryUsage by viewModel.memoryUsage.collectAsState()
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text("SDK Stress Testing")
//
//        Button(
//            onClick = { viewModel.runBatchInsertTest(context, 1000) }
//        ) {
//            Text("Run Batch Insert Test (1000 keys)")
//        }
//
//        Button(
//            onClick = { viewModel.runBatchInsertTest(context, 10000) }
//        ) {
//            Text("Run Batch Insert Test (10000 keys)")
//        }
//
//        Button(
//            onClick = { viewModel.runRenderTest(context, 1000) }
//        ) {
//            Text("Run Render Test (1000 keys)")
//        }
//
//        when (val currentState = state) {
//            is StressTestState.Idle -> {
//                Text("Ready to run tests")
//            }
//            is StressTestState.Running -> {
//                CircularProgressIndicator()
//                Text("Test in progress...")
//            }
//            is StressTestState.Completed -> {
//                Text("Test completed:")
//                Text("Total time: ${currentState.totalTime}ms")
//                Text("Operation time: ${currentState.insertTime}ms")
//                Text("Keys processed: ${currentState.keyCount}")
//            }
//            is StressTestState.Error -> {
//                Text("Error: ${currentState.message}")
//            }
//        }
//
//        if (memoryUsage.isNotEmpty()) {
//            Text("Memory Usage:")
//            Text(memoryUsage)
//        }
//    }
//}