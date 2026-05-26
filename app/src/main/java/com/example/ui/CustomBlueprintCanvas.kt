package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@OptIn(ExperimentalTextApi::class)
@Composable
fun CustomBlueprintCanvas(
    rooms: List<ValayRoom>,
    airflowDir: String,
    sunDir: String,
    onRoomsUpdated: (List<ValayRoom>) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedRoomIndex by remember { mutableStateOf(-1) }

    // Map Room coordinates to editable states
    var roomsState by remember(rooms) { mutableStateOf(rooms) }

    // Colors
    val gridColor = Color(0xFFE8DEF8)
    val wallColor = Color(0xFF6750A4)
    val wallFillColor = Color(0x1F6750A4)
    val selectedWallColor = Color(0xFFD0BCFE)
    val selectedFillColor = Color(0x33D0BCFE)
    val textColor = Color(0xFF1D1B1E)
    val blueprintBlue = Color(0xFFFDF8F6)
    val greenEco = Color(0xFF21005D)
    val goldSun = Color(0xFFFFA000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(blueprintBlue)
            .border(1.dp, Color(0x66CAC4D0))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(roomsState) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            // Convert touch offsets to relative coordinates (0..100)
                            val rx = (offset.x / width) * 100f
                            val ry = (offset.y / height) * 100f

                            // Find which room was tapped (checks boundary)
                            selectedRoomIndex = roomsState.indexOfFirst { r ->
                                rx >= r.x && rx <= (r.x + r.width) &&
                                        ry >= r.y && ry <= (r.y + r.height)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (selectedRoomIndex != -1) {
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()

                                val dx = (dragAmount.x / width) * 100f
                                val dy = (dragAmount.y / height) * 100f

                                roomsState = roomsState.mapIndexed { idx, room ->
                                    if (idx == selectedRoomIndex) {
                                        // Restrict boundaries to stay within 0..100 relative space
                                        val newX = (room.x + dx).coerceIn(0f, 100f - room.width)
                                        val newY = (room.y + dy).coerceIn(0f, 100f - room.height)
                                        room.copy(x = newX, y = newY)
                                    } else {
                                        room
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            selectedRoomIndex = -1
                            onRoomsUpdated(roomsState)
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Blueprint Drafting Grid Lines
            val gridSpacing = 30f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

            // Draw vertical grid lines
            var x = 0f
            while (x < canvasWidth) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            // Draw horizontal grid lines
            var y = 0f
            while (y < canvasHeight) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // 2. Draw outer scale ruler borders
            drawRect(
                color = Color(0xFF6750A4),
                size = size,
                style = Stroke(width = 4f)
            )

            // Draw ruler tick marks
            val scaleCount = 20
            for (i in 0..scaleCount) {
                val tickX = (canvasWidth / scaleCount) * i
                val tickY = (canvasHeight / scaleCount) * i

                // Top & Bottom Ticks
                drawLine(
                    color = Color(0xFF6750A4),
                    start = Offset(tickX, 0f),
                    end = Offset(tickX, 15f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF6750A4),
                    start = Offset(tickX, canvasHeight),
                    end = Offset(tickX, canvasHeight - 15f),
                    strokeWidth = 2f
                )

                // Left & Right Ticks
                drawLine(
                    color = Color(0xFF6750A4),
                    start = Offset(0f, tickY),
                    end = Offset(15f, tickY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF6750A4),
                    start = Offset(canvasWidth, tickY),
                    end = Offset(canvasWidth - 15f, tickY),
                    strokeWidth = 2f
                )
            }

            // 3. Draw Rooms
            roomsState.forEachIndexed { index, room ->
                val rx = (room.x / 100f) * canvasWidth
                val ry = (room.y / 100f) * canvasHeight
                val rWidth = (room.width / 100f) * canvasWidth
                val rHeight = (room.height / 100f) * canvasHeight

                val isSelected = index == selectedRoomIndex
                val colorStroke = if (isSelected) selectedWallColor else wallColor
                val colorFill = if (isSelected) selectedFillColor else wallFillColor

                // Room background
                drawRect(
                    color = colorFill,
                    topLeft = Offset(rx, ry),
                    size = Size(rWidth, rHeight)
                )

                // Room border stroke
                drawRect(
                    color = colorStroke,
                    topLeft = Offset(rx, ry),
                    size = Size(rWidth, rHeight),
                    style = Stroke(width = 3f)
                )

                // Draw tiny dotted helper vectors inside rooms for design aesthetics
                drawLine(
                    color = colorStroke.copy(alpha = 0.4f),
                    start = Offset(rx, ry),
                    end = Offset(rx + rWidth, ry + rHeight),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )

                // Draw Room Labels with dimension estimation based on relative scale (1 unit = 0.3 meters or 1 foot)
                val widthFeet = (room.width * 0.4f).toInt()
                val heightFeet = (room.height * 0.4f).toInt()
                val labelLines = listOf(
                    room.name,
                    "$widthFeet' × $heightFeet'"
                )

                // Use a modern monospaced display typography for CAD labels
                labelLines.forEachIndexed { idx, txt ->
                    val textLayoutResult = textMeasurer.measure(
                        text = txt,
                        style = TextStyle(
                            color = if (isSelected) selectedWallColor else textColor,
                            fontSize = if (idx == 0) 11.sp else 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                    // Center the text inside the room
                    val textX = rx + (rWidth - textLayoutResult.size.width) / 2f
                    val textY = ry + (rHeight - (textLayoutResult.size.height * labelLines.size)) / 2f + (idx * textLayoutResult.size.height)
                    
                    // Don't draw if the room is too cramped to avoid cluttering low-res screen renders
                    if (rWidth > textLayoutResult.size.width + 10f && rHeight > textLayoutResult.size.height * 2f) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(textX, textY)
                        )
                    }
                }
            }

            // 4. Draw Airflow path vectors (Airflow direction arrows)
            // Draw indicators on screen based on the style
            val arrowPath = Path()
            // airflow direction vector logic
            val airflowAngle = when (airflowDir.lowercase()) {
                "north" -> 270f
                "south" -> 90f
                "east" -> 0f
                "west" -> 180f
                "northeast" -> 315f
                "northwest" -> 225f
                "southeast" -> 45f
                "southwest" -> 135f
                else -> 45f // default diagonal
            }

            // Draw airflow entry visual flow representation
            val airflowIntensityText = "AIRFLOW: $airflowDir"
            val textLayoutAirflow = textMeasurer.measure(
                text = airflowIntensityText,
                style = TextStyle(
                    color = greenEco,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
            drawText(
                textLayoutResult = textLayoutAirflow,
                topLeft = Offset(20f, canvasHeight - 25f)
            )

            // Draw Wind Icon / Compass
            drawCircle(
                color = greenEco.copy(alpha = 0.3f),
                radius = 16f,
                center = Offset(160f, canvasHeight - 20f),
                style = Stroke(width = 2f)
            )
            drawLine(
                color = greenEco,
                start = Offset(160f, canvasHeight - 30f),
                end = Offset(160f, canvasHeight - 10f),
                strokeWidth = 2f
            )
            drawLine(
                color = greenEco,
                start = Offset(150f, canvasHeight - 20f),
                end = Offset(170f, canvasHeight - 20f),
                strokeWidth = 2f
            )

            // 5. Draw Sun Insulation orbit (Sun direction indicator)
            val sunIntensityText = "SUN PATH: $sunDir"
            val textLayoutSun = textMeasurer.measure(
                text = sunIntensityText,
                style = TextStyle(
                    color = goldSun,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
            drawText(
                textLayoutResult = textLayoutSun,
                topLeft = Offset(canvasWidth - textLayoutSun.size.width - 20f, canvasHeight - 25f)
            )

            // Solar indicator
            drawCircle(
                color = goldSun,
                radius = 8f,
                center = Offset(canvasWidth - textLayoutSun.size.width - 40f, canvasHeight - 20f)
            )
        }
    }
}
