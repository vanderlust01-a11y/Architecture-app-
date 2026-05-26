package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectName: String,
    val landArea: String,
    val selectedStyle: String,
    val budget: String,
    val timeline: String,
    val airflowDirection: String,
    val sunDirection: String,
    val roomRequirements: String,
    val floors: Int,
    val demands: String,
    val rawGeminiOutput: String, // Stored JSON representing layout coordinates & text description
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
