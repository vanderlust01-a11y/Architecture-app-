package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ValayRoom
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun ThreeDViewCanvas(
    rooms: List<ValayRoom>,
    totalFloors: Int,
    airflowDir: String,
    sunDir: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    // Rotation angle around the vertical Axis (in degrees)
    var rotationAngle by remember { mutableStateOf(45f) }
    // Viewing pitch (tilt) factor
    var pitchFactor by remember { mutableStateOf(0.5f) }

    val wireframeColor = Color(0xFF6750A4)
    val gridColor = Color(0xFFE8DEF8)
    val background3D = Color(0xFFFDF8F6)
    val slabColor = Color(0x1F6750A4)
    val wallColor = Color(0x2E6750A4)
    val text3DColor = Color(0xFF1D1B1E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background3D)
            .border(1.dp, Color(0x33CAC4D0))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag horizontal splits to rotation, vertical to pitch
                    rotationAngle = (rotationAngle + dragAmount.x * 0.5f) % 360f
                    pitchFactor = (pitchFactor - dragAmount.y * 0.002f).coerceIn(0.2f, 0.8f)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 50f // push down slightly for stacking

            // Draw interactive helper instructions
            val instructions = "DRAG TO ROTATE 3D CONCEPT SKETCH"
            val textLayout = textMeasurer.measure(
                text = instructions,
                style = TextStyle(
                    color = text3DColor.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset((width - textLayout.size.width) / 2f, 10f)
            )

            // Convert raw 3D point (X, Y, Z) into rotated 2D projection on Canvas
            fun project(x3d: Float, y3d: Float, z3d: Float): Offset {
                // Center relative: relative point coordinates from 0 to 100 on X and Y, scaled
                val rawX = (x3d - 50f) * 4.5f
                val rawY = (y3d - 50f) * 4.5f
                val rawZ = z3d * 90f // Stack height offset per floor

                // Apply rotation around Z-axis
                val rad = Math.toRadians(rotationAngle.toDouble())
                val cosR = cos(rad).toFloat()
                val sinR = sin(rad).toFloat()

                val rotatedX = rawX * cosR - rawY * sinR
                val rotatedY = rawX * sinR + rawY * cosR

                // Apply isometric tilt projection
                val projX = centerX + rotatedX
                val projY = centerY + (rotatedY * pitchFactor) - rawZ

                return Offset(projX, projY)
            }

            // 1. Draw Ground Base Plate Grid
            val baseLevels = listOf(0f)
            baseLevels.forEach { z ->
                val p00 = project(0f, 0f, z)
                val p10 = project(100f, 0f, z)
                val p11 = project(100f, 100f, z)
                val p01 = project(0f, 100f, z)

                // Fill site slab boundary
                val slabPath = Path().apply {
                    moveTo(p00.x, p00.y)
                    lineTo(p10.x, p10.y)
                    lineTo(p11.x, p11.y)
                    lineTo(p01.x, p01.y)
                    close()
                }
                drawPath(slabPath, color = slabColor)
                drawPath(slabPath, color = Color(0xFF6750A4).copy(alpha = 0.6f), style = Stroke(width = 2f))

                // Draw subdivision grids inside slab
                for (i in 1..4) {
                    val ratio = i * 20f
                    // Draw lines across X-axis subdivision
                    val pXStart = project(ratio, 0f, z)
                    val pXEnd = project(ratio, 100f, z)
                    drawLine(color = gridColor, start = pXStart, end = pXEnd, strokeWidth = 1f)

                    // Draw lines across Y-axis subdivision
                    val pYStart = project(0f, ratio, z)
                    val pYEnd = project(100f, ratio, z)
                    drawLine(color = gridColor, start = pYStart, end = pYEnd, strokeWidth = 1f)
                }
            }

            // 2. Project and draw Rooms for each floor
            // To make it look gorgeous, we separate rooms by floors and render them with height slabs!
            for (f in 0 until totalFloors) {
                // If rooms are empty, we split generic rooms based on floor index
                val floorRooms = if (rooms.isEmpty()) {
                    emptyList()
                } else {
                    // Split rooms into floors if they have custom properties, or clone styled boxes
                    rooms
                }

                // Render floor divider slabs
                if (f > 0) {
                    val zHeight = f * 1.6f
                    val s00 = project(0f, 0f, zHeight)
                    val s10 = project(100f, 0f, zHeight)
                    val s11 = project(100f, 100f, zHeight)
                    val s01 = project(0f, 100f, zHeight)

                    val dividerPath = Path().apply {
                        moveTo(s00.x, s00.y)
                        lineTo(s10.x, s10.y)
                        lineTo(s11.x, s11.y)
                        lineTo(s01.x, s01.y)
                        close()
                    }
                    drawPath(dividerPath, color = slabColor.copy(alpha = 0.15f))
                    drawPath(dividerPath, color = Color(0xFF6750A4).copy(alpha = 0.3f), style = Stroke(width = 1f))
                }

                floorRooms.forEach { room ->
                    // Base elevation of the floor
                    val zBase = f * 1.6f
                    val zRoof = zBase + 1.2f // room wall height indicator

                    // Room 2D bounds
                    val rx = room.x
                    val ry = room.y
                    val rw = room.width
                    val rh = room.height

                    // 3D Box vertices (8 points total)
                    // Bottom 4 vertices
                    val b00 = project(rx, ry, zBase)
                    val b10 = project(rx + rw, ry, zBase)
                    val b11 = project(rx + rw, ry + rh, zBase)
                    val b01 = project(rx, ry + rh, zBase)

                    // Top 4 vertices (roof)
                    val t00 = project(rx, ry, zRoof)
                    val t10 = project(rx + rw, ry, zRoof)
                    val t11 = project(rx + rw, ry + rh, zRoof)
                    val t01 = project(rx, ry + rh, zRoof)

                    // Color accent for wireframe details
                    val roomAccentColor = when (room.name.lowercase()) {
                        "living room", "hall" -> Color(0xFF673AB7)
                        "kitchen" -> Color(0xFFFF5722)
                        "master bedroom", "bedroom" -> Color(0xFFE91E63)
                        "bathroom", "toilet" -> Color(0xFF00BCD4)
                        else -> wireframeColor
                    }

                    // Fill wall elevations (Facing segments) to add real volumetric depth!
                    val leftWall = Path().apply {
                        moveTo(b00.x, b00.y)
                        lineTo(b01.x, b01.y)
                        lineTo(t01.x, t01.y)
                        lineTo(t00.x, t00.y)
                        close()
                    }
                    val rightWall = Path().apply {
                        moveTo(b01.x, b01.y)
                        lineTo(b11.x, b11.y)
                        lineTo(t11.x, t11.y)
                        lineTo(t01.x, t01.y)
                        close()
                    }

                    drawPath(leftWall, color = roomAccentColor.copy(alpha = 0.12f))
                    drawPath(rightWall, color = roomAccentColor.copy(alpha = 0.15f))

                    // Draw bottom room bounds
                    drawLine(color = roomAccentColor, start = b00, end = b10, strokeWidth = 1f)
                    drawLine(color = roomAccentColor, start = b10, end = b11, strokeWidth = 1f)
                    drawLine(color = roomAccentColor, start = b11, end = b01, strokeWidth = 1f)
                    drawLine(color = roomAccentColor, start = b01, end = b00, strokeWidth = 1f)

                    // Draw top room bounds (roof frame)
                    drawLine(color = roomAccentColor, start = t00, end = t10, strokeWidth = 2f)
                    drawLine(color = roomAccentColor, start = t10, end = t11, strokeWidth = 2f)
                    drawLine(color = roomAccentColor, start = t11, end = t01, strokeWidth = 2f)
                    drawLine(color = roomAccentColor, start = t01, end = t00, strokeWidth = 2f)

                    // Vertical studs (Corner columns of room volume)
                    drawLine(color = roomAccentColor, start = b00, end = t00, strokeWidth = 1.5f)
                    drawLine(color = roomAccentColor, start = b10, end = t10, strokeWidth = 1.5f)
                    drawLine(color = roomAccentColor, start = b11, end = t11, strokeWidth = 1.5f)
                    drawLine(color = roomAccentColor, start = b01, end = t01, strokeWidth = 1.5f)

                    // Draw interactive 3D text floating tags!
                    val roomLabelResult = textMeasurer.measure(
                        text = room.name,
                        style = TextStyle(
                            color = text3DColor,
                            fontSize = 8.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                    // Place text in center of roof
                    val centerRoofX = (t00.x + t11.x) / 2f - (roomLabelResult.size.width / 2f)
                    val centerRoofY = (t00.y + t11.y) / 2f - (roomLabelResult.size.height / 2f)
                    
                    if (rw > 15f && rh > 15f) {
                        drawText(
                            textLayoutResult = roomLabelResult,
                            topLeft = Offset(centerRoofX, centerRoofY)
                        )
                    }
                }
            }

            // 3. Draw Solar insulation compass (sun alignment relative vectors)
            val anglesun = when (sunDir.lowercase()) {
                "east" -> 0f
                "west" -> 180f
                "north" -> 270f
                "south" -> 90f
                else -> 45f
            }
            val sunRad = Math.toRadians((rotationAngle + anglesun).toDouble())
            val sunOffsetX = cos(sunRad).toFloat() * 160f
            val sunOffsetY = sin(sunRad).toFloat() * 60f

            val sunPos = Offset(centerX + sunOffsetX, centerY - 150f + sunOffsetY)
            // Sun globe glow
            drawCircle(color = Color(0xFFFFD54F), radius = 10f, center = sunPos)
            drawCircle(color = Color(0x33FFD54F), radius = 22f, center = sunPos)

            // Sun path ray indicator pointing to center of building
            drawLine(
                color = Color(0x4DFFD54F),
                start = sunPos,
                end = Offset(centerX, centerY - 40f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            )

            // Airflow simulation: draw moving air streams around building!
            // Dotted green flowing vectors going in a direction
            val airColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
            val airRad = Math.toRadians((rotationAngle + when (airflowDir.lowercase()) {
                "north" -> 270f
                "south" -> 90f
                "east" -> 0f
                "west" -> 180f
                else -> 45f
            }).toDouble())

            val flowDirX = cos(airRad).toFloat()
            val flowDirY = sin(airRad).toFloat()

            // Draw multiple parallel flow vectors
            for (offsetIndex in -3..3) {
                val lateralSpacing = offsetIndex * 50f
                val airStartCoord = project(
                    50f - flowDirX * 100f + flowDirY * lateralSpacing * 0.1f,
                    50f - flowDirY * 100f - flowDirX * lateralSpacing * 0.1f,
                    0.4f
                )
                val airEndCoord = project(
                    50f + flowDirX * 100f + flowDirY * lateralSpacing * 0.1f,
                    50f + flowDirY * 100f - flowDirX * lateralSpacing * 0.1f,
                    0.4f
                )

                drawLine(
                    color = airColor,
                    start = airStartCoord,
                    end = airEndCoord,
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            }
        }
    }
}
