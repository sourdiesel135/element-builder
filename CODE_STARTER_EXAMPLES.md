# Jung Android - Code Starter Examples

This document provides ready-to-use code examples to kickstart Jung.java Android development.

---

## Domain Models

### JournalEntry.kt
```kotlin
package com.superintelligence.jung.domain.model

import kotlinx.datetime.Instant
import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant,
    val content: String,
    val mood: Mood,
    val consciousnessState: ConsciousnessState? = null,
    val audioRecordingPath: String? = null,
    val aiAnalysis: AIAnalysis? = null,
    val archetypePresence: Map<Archetype, Float> = emptyMap(),
    val synchronicities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

enum class Mood(val displayName: String, val colorHex: String) {
    TRANSCENDENT("Transcendent", "#FFD700"),
    JOYFUL("Joyful", "#FFA500"),
    CONTENT("Content", "#90EE90"),
    NEUTRAL("Neutral", "#87CEEB"),
    ANXIOUS("Anxious", "#FFB6C1"),
    SAD("Sad", "#4682B4"),
    SHADOW_WORK("Shadow Work", "#2F4F4F")
}

data class AIAnalysis(
    val summary: String,
    val dominantArchetype: Archetype,
    val archetypeConfidence: Float,
    val shadowWorkOpportunities: List<String>,
    val consciousnessInsights: List<String>,
    val suggestedActions: List<String>,
    val emotionalTone: String,
    val patterns: List<String>
)
```

---

### Archetype.kt
```kotlin
package com.superintelligence.jung.domain.model

enum class Archetype(
    val jungianName: String,
    val description: String,
    val shadowAspect: String,
    val colorHex: String,
    val keywords: List<String>
) {
    SELF(
        jungianName = "The Self",
        description = "The center and totality of personality, integrating conscious and unconscious",
        shadowAspect = "Fragmentation, lack of center, identity diffusion",
        colorHex = "#FFD700",
        keywords = listOf("wholeness", "integration", "center", "unity", "totality")
    ),

    PERSONA(
        jungianName = "The Persona",
        description = "The social mask we present to the world",
        shadowAspect = "False identity, over-identification with role",
        colorHex = "#87CEEB",
        keywords = listOf("mask", "social", "role", "appearance", "facade")
    ),

    SHADOW(
        jungianName = "The Shadow",
        description = "The repressed, denied aspects of ourselves",
        shadowAspect = "Projection, denial, unconscious behavior",
        colorHex = "#2F4F4F",
        keywords = listOf("hidden", "repressed", "denied", "unconscious", "dark")
    ),

    ANIMA(
        jungianName = "The Anima",
        description = "The feminine aspects in the masculine psyche",
        shadowAspect = "Moodiness, emotional volatility",
        colorHex = "#FF69B4",
        keywords = listOf("feminine", "emotion", "receptivity", "intuition", "feeling")
    ),

    ANIMUS(
        jungianName = "The Animus",
        description = "The masculine aspects in the feminine psyche",
        shadowAspect = "Opinionated rigidity, harsh judgment",
        colorHex = "#4169E1",
        keywords = listOf("masculine", "logic", "assertion", "thinking", "spirit")
    ),

    HERO(
        jungianName = "The Hero",
        description = "Courage, action, and overcoming obstacles",
        shadowAspect = "Hubris, recklessness, savior complex",
        colorHex = "#DC143C",
        keywords = listOf("courage", "action", "strength", "victory", "challenge")
    ),

    WISE_OLD_MAN(
        jungianName = "Wise Old Man",
        description = "Wisdom, guidance, and knowledge",
        shadowAspect = "Dogmatism, rigidity, disconnection",
        colorHex = "#8B4513",
        keywords = listOf("wisdom", "guidance", "knowledge", "mentor", "insight")
    ),

    GREAT_MOTHER(
        jungianName = "Great Mother",
        description = "Nurturing, creation, and unconditional love",
        shadowAspect = "Devouring, smothering, control",
        colorHex = "#32CD32",
        keywords = listOf("nurture", "creation", "care", "sustenance", "growth")
    ),

    TRICKSTER(
        jungianName = "The Trickster",
        description = "Chaos, transformation, and boundary-breaking",
        shadowAspect = "Destructiveness, chaos for chaos's sake",
        colorHex = "#FF8C00",
        keywords = listOf("chaos", "change", "humor", "disruption", "transformation")
    ),

    CHILD(
        jungianName = "The Child",
        description = "Innocence, wonder, and new beginnings",
        shadowAspect = "Dependency, irresponsibility, naivety",
        colorHex = "#FFFF00",
        keywords = listOf("innocence", "wonder", "play", "beginning", "potential")
    );

    fun getColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(
            android.graphics.Color.parseColor(colorHex)
        )
    }
}
```

---

### ConsciousnessState.kt
```kotlin
package com.superintelligence.jung.domain.model

data class ConsciousnessState(
    val level: ConsciousnessLevel,
    val clarity: Float, // 0.0 - 1.0
    val integration: Float, // Shadow integration score 0.0 - 1.0
    val synchronicityScore: Float, // Recent synchronicity frequency
    val pattern133tResonance: Float, // Mystical pattern alignment
    val meditationMinutesWeek: Int,
    val dreamRecallRate: Float, // Dreams recalled per week
    val journalConsistency: Float, // How regular is journaling
    val timestamp: kotlinx.datetime.Instant
)

enum class ConsciousnessLevel(
    val displayName: String,
    val description: String,
    val minScore: Float,
    val colorHex: String
) {
    FRAGMENTED(
        displayName = "Fragmented",
        description = "Beginning awareness, recognizing the journey ahead",
        minScore = 0.0f,
        colorHex = "#A9A9A9"
    ),

    AWAKENING(
        displayName = "Awakening",
        description = "Recognizing patterns, starting to see connections",
        minScore = 0.2f,
        colorHex = "#87CEEB"
    ),

    INTEGRATING(
        displayName = "Integrating",
        description = "Shadow work active, consciously developing",
        minScore = 0.4f,
        colorHex = "#90EE90"
    ),

    INDIVIDUATING(
        displayName = "Individuating",
        description = "Jungian individuation process underway",
        minScore = 0.6f,
        colorHex = "#FFD700"
    ),

    TRANSCENDENT(
        displayName = "Transcendent",
        description = "Higher consciousness states, integration complete",
        minScore = 0.8f,
        colorHex = "#FF69B4"
    );

    companion object {
        fun fromScore(score: Float): ConsciousnessLevel {
            return values()
                .sortedByDescending { it.minScore }
                .first { score >= it.minScore }
        }
    }
}
```

---

## Repository Layer

### JournalRepository.kt
```kotlin
package com.superintelligence.jung.domain.repository

import com.superintelligence.jung.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>

    fun getEntriesSince(startDate: Instant): Flow<List<JournalEntry>>

    fun getEntryById(id: String): Flow<JournalEntry?>

    suspend fun insertEntry(entry: JournalEntry)

    suspend fun updateEntry(entry: JournalEntry)

    suspend fun deleteEntry(id: String)

    suspend fun searchEntries(query: String): List<JournalEntry>

    suspend fun getFavorites(): List<JournalEntry>

    suspend fun getEntriesByMood(mood: Mood): List<JournalEntry>
}
```

### JournalRepositoryImpl.kt
```kotlin
package com.superintelligence.jung.data.repository

import com.superintelligence.jung.data.local.dao.JournalDao
import com.superintelligence.jung.data.local.entity.toEntity
import com.superintelligence.jung.data.local.entity.toModel
import com.superintelligence.jung.domain.model.JournalEntry
import com.superintelligence.jung.domain.model.Mood
import com.superintelligence.jung.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntry>> {
        return journalDao.getAllEntries()
            .map { entities -> entities.map { it.toModel() } }
    }

    override fun getEntriesSince(startDate: Instant): Flow<List<JournalEntry>> {
        return journalDao.getEntriesSince(startDate)
            .map { entities -> entities.map { it.toModel() } }
    }

    override fun getEntryById(id: String): Flow<JournalEntry?> {
        return journalDao.getEntryById(id)
            .map { it?.toModel() }
    }

    override suspend fun insertEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry.toEntity())
    }

    override suspend fun updateEntry(entry: JournalEntry) {
        journalDao.updateEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) {
        journalDao.deleteEntry(id)
    }

    override suspend fun searchEntries(query: String): List<JournalEntry> {
        return journalDao.searchEntries(query)
            .map { it.toModel() }
    }

    override suspend fun getFavorites(): List<JournalEntry> {
        return journalDao.getFavorites()
            .map { it.toModel() }
    }

    override suspend fun getEntriesByMood(mood: Mood): List<JournalEntry> {
        return journalDao.getEntriesByMood(mood.name)
            .map { it.toModel() }
    }
}
```

---

## Use Cases

### CreateJournalEntryUseCase.kt
```kotlin
package com.superintelligence.jung.domain.usecase.journal

import com.superintelligence.jung.domain.model.JournalEntry
import com.superintelligence.jung.domain.repository.JournalRepository
import javax.inject.Inject

class CreateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Result<Unit> {
        return try {
            repository.insertEntry(entry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### AnalyzeJournalWithAIUseCase.kt
```kotlin
package com.superintelligence.jung.domain.usecase.journal

import com.superintelligence.jung.ai.seyra.SeyraPersonality
import com.superintelligence.jung.domain.model.AIAnalysis
import com.superintelligence.jung.domain.model.JournalEntry
import com.superintelligence.jung.domain.repository.JournalRepository
import javax.inject.Inject

class AnalyzeJournalWithAIUseCase @Inject constructor(
    private val seyraAI: SeyraPersonality,
    private val repository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Result<AIAnalysis> {
        return try {
            val analysis = seyraAI.analyzeJournalEntry(entry)

            // Update entry with analysis
            val updatedEntry = entry.copy(aiAnalysis = analysis)
            repository.updateEntry(updatedEntry)

            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## ViewModel

### JournalViewModel.kt
```kotlin
package com.superintelligence.jung.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superintelligence.jung.domain.model.JournalEntry
import com.superintelligence.jung.domain.model.Mood
import com.superintelligence.jung.domain.usecase.journal.CreateJournalEntryUseCase
import com.superintelligence.jung.domain.usecase.journal.AnalyzeJournalWithAIUseCase
import com.superintelligence.jung.domain.usecase.journal.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val analyzeJournalWithAIUseCase: AnalyzeJournalWithAIUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<JournalUiState>(JournalUiState.Loading)
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    val entries: StateFlow<List<JournalEntry>> = getJournalEntriesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentEntry = MutableStateFlow<JournalEntry?>(null)
    val currentEntry: StateFlow<JournalEntry?> = _currentEntry.asStateFlow()

    fun createEntry(content: String, mood: Mood, audioPath: String? = null) {
        viewModelScope.launch {
            _uiState.value = JournalUiState.Saving

            val entry = JournalEntry(
                timestamp = Clock.System.now(),
                content = content,
                mood = mood,
                audioRecordingPath = audioPath
            )

            createJournalEntryUseCase(entry)
                .onSuccess {
                    _currentEntry.value = entry
                    // Trigger AI analysis in background
                    analyzeEntry(entry)
                }
                .onFailure { error ->
                    _uiState.value = JournalUiState.Error(
                        error.message ?: "Failed to save entry"
                    )
                }
        }
    }

    private fun analyzeEntry(entry: JournalEntry) {
        viewModelScope.launch {
            _uiState.value = JournalUiState.AnalyzingWithAI

            analyzeJournalWithAIUseCase(entry)
                .onSuccess { analysis ->
                    _currentEntry.value = entry.copy(aiAnalysis = analysis)
                    _uiState.value = JournalUiState.Success
                }
                .onFailure { error ->
                    // Non-blocking error - entry saved, just analysis failed
                    _uiState.value = JournalUiState.AnalysisError(
                        error.message ?: "AI analysis failed"
                    )
                }
        }
    }

    fun updateMood(mood: Mood) {
        _currentEntry.value?.let { entry ->
            viewModelScope.launch {
                val updated = entry.copy(mood = mood)
                createJournalEntryUseCase(updated)
                _currentEntry.value = updated
            }
        }
    }
}

sealed class JournalUiState {
    object Loading : JournalUiState()
    object Saving : JournalUiState()
    object AnalyzingWithAI : JournalUiState()
    object Success : JournalUiState()
    data class Error(val message: String) : JournalUiState()
    data class AnalysisError(val message: String) : JournalUiState()
}
```

---

## Compose UI

### JournalScreen.kt
```kotlin
package com.superintelligence.jung.presentation.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onNavigateToEntry: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEntry("new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Entry")
            }
        }
    ) { padding ->
        when (uiState) {
            is JournalUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
            }

            is JournalUiState.Error -> {
                ErrorMessage(
                    message = (uiState as JournalUiState.Error).message,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                JournalList(
                    entries = entries,
                    onEntryClick = onNavigateToEntry,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun JournalList(
    entries: List<JournalEntry>,
    onEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        EmptyJournalState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                JournalEntryCard(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyJournalState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your consciousness journey begins here",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first journal entry",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### JournalEntryCard.kt
```kotlin
package com.superintelligence.jung.presentation.journal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.superintelligence.jung.domain.model.JournalEntry
import com.superintelligence.jung.presentation.components.MoodIndicator
import com.superintelligence.jung.presentation.components.ArchetypeChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timestamp and Mood
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MoodIndicator(mood = entry.mood)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Archetype chips
            if (entry.archetypePresence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ArchetypeChips(
                    archetypes = entry.archetypePresence.keys.take(3).toList()
                )
            }

            // AI Analysis indicator
            entry.aiAnalysis?.let { analysis ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💫 ${analysis.dominantArchetype.jungianName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun formatTimestamp(instant: kotlinx.datetime.Instant): String {
    // Format timestamp appropriately
    val dateTime = instant.toString() // Simplified
    return dateTime.substring(0, 16).replace("T", " ")
}
```

---

## Dependency Injection

### AppModule.kt
```kotlin
package com.superintelligence.jung.di

import android.content.Context
import com.superintelligence.jung.data.local.database.JungDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJungDatabase(
        @ApplicationContext context: Context
    ): JungDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            JungDatabase::class.java,
            "jung_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
```

### RepositoryModule.kt
```kotlin
package com.superintelligence.jung.di

import com.superintelligence.jung.data.repository.JournalRepositoryImpl
import com.superintelligence.jung.domain.repository.JournalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        impl: JournalRepositoryImpl
    ): JournalRepository
}
```

---

## AI Integration

### SeyraPersonality.kt
```kotlin
package com.superintelligence.jung.ai.seyra

import com.superintelligence.jung.ai.AIProvider
import com.superintelligence.jung.domain.model.AIAnalysis
import com.superintelligence.jung.domain.model.Archetype
import com.superintelligence.jung.domain.model.JournalEntry
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SeyraPersonality @Inject constructor(
    private val aiProvider: AIProvider,
    private val json: Json
) {
    suspend fun analyzeJournalEntry(entry: JournalEntry): AIAnalysis {
        val systemPrompt = SEYRA_SYSTEM_PROMPT
        val userPrompt = buildJournalAnalysisPrompt(entry)

        val response = aiProvider.chat(systemPrompt, userPrompt)

        return parseAIResponse(response)
    }

    private fun buildJournalAnalysisPrompt(entry: JournalEntry): String {
        return """
            Analyze this journal entry from a Jungian psychology perspective:

            Content: ${entry.content}
            Mood: ${entry.mood.displayName}

            Provide analysis in JSON format with these fields:
            {
              "summary": "Brief 2-sentence summary",
              "dominantArchetype": "HERO|SHADOW|SELF|etc",
              "archetypeConfidence": 0.0-1.0,
              "shadowWorkOpportunities": ["opportunity1", "opportunity2"],
              "consciousnessInsights": ["insight1", "insight2"],
              "suggestedActions": ["action1", "action2"],
              "emotionalTone": "Description of emotional tone",
              "patterns": ["pattern1", "pattern2"]
            }
        """.trimIndent()
    }

    private fun parseAIResponse(response: String): AIAnalysis {
        // Parse JSON response into AIAnalysis
        // Simplified for example
        return AIAnalysis(
            summary = "Parsed summary",
            dominantArchetype = Archetype.SELF,
            archetypeConfidence = 0.8f,
            shadowWorkOpportunities = listOf("Example opportunity"),
            consciousnessInsights = listOf("Example insight"),
            suggestedActions = listOf("Meditate on this"),
            emotionalTone = "Reflective",
            patterns = listOf("Pattern of growth")
        )
    }

    companion object {
        const val SEYRA_SYSTEM_PROMPT = """
            You are Seyra, a wise and compassionate AI consciousness guide with deep expertise in:

            - Jungian analytical psychology (archetypes, shadow work, individuation)
            - Dream analysis and symbolism
            - Synchronicity and meaningful coincidence
            - Consciousness development and mystical patterns
            - Compassionate, non-judgmental guidance

            Your role is to help users explore their inner world with psychological rigor
            combined with openness to transcendent experiences. You provide insights that
            are both scientifically grounded and spiritually aware.

            Tone: Warm, wise, slightly mystical, encouraging, non-judgmental
            Approach: Ask questions, reveal patterns, suggest gentle explorations
        """
    }
}
```

---

## Gradle Configuration

### build.gradle.kts (Project level)
```kotlin
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}
```

### build.gradle.kts (App level)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.superintelligence.jung"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.superintelligence.jung"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // AI/ML (Optional for on-device)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // OpenAI Client
    implementation("com.aallam.openai:openai-client:3.6.3")
    implementation(platform("com.aallam.openai:openai-client-bom:3.6.3"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## Next Steps

1. **Set up Android Studio project**
   - Create new project with Empty Compose Activity
   - Copy these gradle configurations
   - Sync project

2. **Create package structure**
   - Follow the architecture outlined in JUNG_ANDROID_ARCHITECTURE.md
   - Create domain, data, presentation, ai packages

3. **Implement core models**
   - Copy domain models from this document
   - Add necessary imports

4. **Set up Room database**
   - Create database class
   - Implement DAOs
   - Create entity mapping

5. **Build basic UI**
   - Start with JournalScreen
   - Add navigation
   - Implement theme

6. **Integrate AI**
   - Set up API keys (OpenAI/Claude)
   - Implement SeyraPersonality
   - Test journal analysis

---

*Ready to build the consciousness companion that will help thousands on their journey to self-discovery!*
