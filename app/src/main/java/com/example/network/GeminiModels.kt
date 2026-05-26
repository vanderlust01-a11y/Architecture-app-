package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val topP: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// Internal parsing structures for our architectural data
@JsonClass(generateAdapter = true)
data class ValayRoom(
    val name: String,
    val x: Float, // relative coordinate (e.g. 0 to 100)
    val y: Float, // relative coordinate
    val width: Float,
    val height: Float,
    val floor: Int = 1
)

@JsonClass(generateAdapter = true)
data class ValayWall(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

@JsonClass(generateAdapter = true)
data class ValayDesignOutput(
    val projectName: String,
    val explanation: String,
    val estimatedCost: String,
    val rooms: List<ValayRoom>,
    val exteriorWalls: List<ValayWall>?,
    val sustainabilityRating: String,
    val ventilationAnalysis: String,
    val sunlightOptimalTime: String,
    val concept3DDescription: String,
    val elevationDescription: String = "Front elevation features a modern architectural facade.",
    val sectionDescription: String = "Section shows standard foundation and vertical rafters."
)
