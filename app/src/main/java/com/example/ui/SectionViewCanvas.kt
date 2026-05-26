package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ValayRoom

@OptIn(ExperimentalTextApi::class)
@Composable
fun SectionViewCanvas(
    rooms: List<ValayRoom>,
    floors: Int,
    style: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val darkBlueCad = Color(0xFFFDF8F6)
    val accentPurple = Color(0xFF6750A4)
    val dividerColor = Color(0xFFCAC4D0)
    val strokePrimary = Color(0xFF1D1B1E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBlueCad)
            .border(1.dp, dividerColor.copy(alpha = 0.5f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height

            val groundY = height - 60f
            val houseWidth = width * 0.65f
            val leftBound = (width - houseWidth) / 2f
            val rightBound = leftBound + houseWidth

            val totalFloors = floors.coerceIn(1, 4)
            val floorHeight = when (totalFloors) {
                1 -> 140f
                2 -> 90f
                3 -> 65f
                else -> 50f
            }

            // 1. Draw Cad Drafting Grid Lines
            val gridStep = 45f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            var gx = 0f
            while (gx < width) {
                drawLine(
                    color = dividerColor.copy(alpha = 0.25f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy < height) {
                drawLine(
                    color = dividerColor.copy(alpha = 0.25f),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Concrete slab foundation bed
            val foundationDepth = 22f
            drawRect(
                color = Color(0xFFE0E0E0),
                topLeft = Offset(leftBound - 15f, groundY),
                size = Size(houseWidth + 30f, foundationDepth)
            )
            // Concrete foundation hatching lines
            for (i in (leftBound - 10f).toInt()..(rightBound + 10f).toInt() step 15) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(i.toFloat(), groundY),
                    end = Offset(i.toFloat() + 10f, groundY + foundationDepth),
                    strokeWidth = 1f
                )
            }
            // Outline foundation
            drawRect(
                color = strokePrimary,
                topLeft = Offset(leftBound - 15f, groundY),
                size = Size(houseWidth + 30f, foundationDepth),
                style = Stroke(width = 2f)
            )

            // 3. Draw Left & Right major structural columns / beams
            drawLine(
                color = strokePrimary,
                start = Offset(leftBound, groundY),
                end = Offset(leftBound, groundY - (totalFloors * floorHeight)),
                strokeWidth = 4f
            )
            drawLine(
                color = strokePrimary,
                start = Offset(rightBound, groundY),
                end = Offset(rightBound, groundY - (totalFloors * floorHeight)),
                strokeWidth = 4f
            )

            // 4. Draw Intermediate slabs & ceilings for each floor
            for (f in 0 until totalFloors) {
                val currentFloorY = groundY - (f * floorHeight)
                val topFloorY = currentFloorY - floorHeight

                // Draw floor slab line
                drawLine(
                    color = strokePrimary,
                    start = Offset(leftBound, currentFloorY),
                    end = Offset(rightBound, currentFloorY),
                    strokeWidth = 3f
                )

                // Fill room cutouts on this active floor (translated from 2D coordinates)
                val floorRooms = rooms.filter { it.floor == f + 1 }
                floorRooms.forEach { room ->
                    // Scale internal x coordinates (relative 0..100) to live inside house boundary
                    val relativeXScale = houseWidth / 100f
                    val rxMin = leftBound + (room.x * relativeXScale)
                    val rxMax = rxMin + (room.width * relativeXScale)

                    // Draw Room partition cross-section
                    drawRect(
                        color = Color(0x1F6750A4),
                        topLeft = Offset(rxMin, topFloorY + 2f),
                        size = Size(rxMax - rxMin, floorHeight - 2f)
                    )

                    // Draw internal division studs (doors / details)
                    drawLine(
                        color = accentPurple.copy(alpha = 0.5f),
                        start = Offset(rxMin, topFloorY),
                        end = Offset(rxMin, currentFloorY),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = accentPurple.copy(alpha = 0.5f),
                        start = Offset(rxMax, topFloorY),
                        end = Offset(rxMax, currentFloorY),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )

                    // Room label inside section slice
                    val textLayout = textMeasurer.measure(
                        text = room.name,
                        style = TextStyle(
                            color = Color(0xFF1D1B1E),
                            fontSize = 8.5.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                    val textX = rxMin + ((rxMax - rxMin) - textLayout.size.width) / 2f
                    val textY = topFloorY + (floorHeight - textLayout.size.height) / 2f

                    if (rxMax - rxMin > textLayout.size.width + 8f) {
                        drawText(textLayout, topLeft = Offset(textX, textY))
                    }
                }
            }

            // 5. Draw Insulated Roof Truss Structure (triangular trusses on top level)
            val roofBaseY = groundY - (totalFloors * floorHeight)
            val roofHeight = 45f
            val roofPeakY = roofBaseY - roofHeight

            // Draw principal rafters (斜) and tie beam (平)
            val trianglePath = Path().apply {
                moveTo(leftBound - 10f, roofBaseY)
                lineTo(width / 2f, roofPeakY)
                lineTo(rightBound + 10f, roofBaseY)
                close()
            }
            drawPath(trianglePath, color = strokePrimary, style = Stroke(width = 2.5f))

            // Web-bracing members (truss lines inside triangular attic void)
            drawLine(strokePrimary, Offset(width / 2f, roofPeakY), Offset(width / 2f, roofBaseY), 2f)
            drawLine(strokePrimary, Offset(width / 2f, roofPeakY), Offset(leftBound + (houseWidth * 0.25f), roofBaseY), 1.5f)
            drawLine(strokePrimary, Offset(width / 2f, roofPeakY), Offset(leftBound + (houseWidth * 0.75f), roofBaseY), 1.5f)
            drawLine(strokePrimary, Offset(leftBound + (houseWidth * 0.25f), roofBaseY), Offset(leftBound + (houseWidth * 0.12f), roofBaseY - 15f), 1.5f)
            drawLine(strokePrimary, Offset(rightBound - (houseWidth * 0.25f), roofBaseY), Offset(rightBound - (houseWidth * 0.12f), roofBaseY - 15f), 1.5f)

            // Insulation hatch shading inside truss
            drawPath(trianglePath, color = Color(0x11FF5722))

            // 6. Draw side Level dimension bubbles on RHS
            val annotX = rightBound + 35f
            drawLine(
                color = accentPurple,
                start = Offset(annotX, groundY + 15f),
                end = Offset(annotX, roofBaseY - 20f),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            for (f in 0..totalFloors) {
                val ly = groundY - (f * floorHeight)
                drawLine(
                    color = accentPurple,
                    start = Offset(annotX - 6f, ly),
                    end = Offset(annotX + 6f, ly),
                    strokeWidth = 1.5f
                )

                val elevLabel = if (f == 0) "SLAB +0.0'" else "LVL $f +${f * 10}.0'"
                val textLayout = textMeasurer.measure(
                    text = elevLabel,
                    style = TextStyle(color = accentPurple, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )
                drawText(textLayout, topLeft = Offset(annotX + 8f, ly - (textLayout.size.height / 2f)))
            }

            // Annotation text description
            val sectionTitleLayout = textMeasurer.measure(
                text = "${style.uppercase()} SECTION DRAFT | SCALE 1:50 | ${totalFloors} FLRS",
                style = TextStyle(
                    color = Color(0xFF21005D),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                )
            )
            drawText(
                sectionTitleLayout,
                topLeft = Offset((width - sectionTitleLayout.size.width) / 2f, height - 30f)
            )
        }
    }
}
