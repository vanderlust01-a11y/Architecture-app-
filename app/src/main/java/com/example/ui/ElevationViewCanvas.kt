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

@OptIn(ExperimentalTextApi::class)
@Composable
fun ElevationViewCanvas(
    style: String,
    floors: Int,
    airflowDir: String,
    sunDir: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Professional cad board background
    val darkBlueCad = Color(0xFFFDF8F6)
    val accentPurple = Color(0xFF6750A4)
    val dividerColor = Color(0xFFCAC4D0)
    val shadowColor = Color(0x1F1D1B1E)

    // Palette depending on style
    val wallColor = when (style.lowercase()) {
        "modern" -> Color(0xFFE5E1E6)
        "minimalist" -> Color(0xFFF4EFF4)
        "tudor" -> Color(0xFFFFFBF7)
        "classic" -> Color(0xFFFAF0E6)
        "eco" -> Color(0xFFF2F0EB)
        else -> Color(0xFFECEFF1)
    }

    val trimColor = when (style.lowercase()) {
        "modern" -> Color(0xFF2D2E30)
        "minimalist" -> Color(0xFF49454F)
        "tudor" -> Color(0xFF5D4037)
        "classic" -> Color(0xFF8B0000)
        "eco" -> Color(0xFF388E3C)
        else -> Color(0xFF37474F)
    }

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
            val originX = width / 2f
            val houseWidth = width * 0.55f
            val leftBound = originX - (houseWidth / 2f)
            val rightBound = originX + (houseWidth / 2f)

            // Dynamic floor heights scaling
            val totalFloors = floors.coerceIn(1, 4)
            val floorHeight = when (totalFloors) {
                1 -> 140f
                2 -> 90f
                3 -> 65f
                else -> 50f
            }

            // 1. Draw Cad Grid Behind the elevation view
            val gridStep = 40f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            var gx = 0f
            while (gx < width) {
                drawLine(
                    color = dividerColor.copy(alpha = 0.3f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy < height) {
                drawLine(
                    color = dividerColor.copy(alpha = 0.3f),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Draw ground terrain vector
            drawLine(
                color = trimColor.copy(alpha = 0.8f),
                start = Offset(20f, groundY),
                end = Offset(width - 20f, groundY),
                strokeWidth = 4f
            )
            // Hatching ground
            for (i in 40 until (width - 40).toInt() step 20) {
                drawLine(
                    color = trimColor.copy(alpha = 0.4f),
                    start = Offset(i.toFloat(), groundY),
                    end = Offset(i.toFloat() - 8f, groundY + 10f),
                    strokeWidth = 1.5f
                )
            }

            // 3. Draw Building Envelope Levels
            for (f in 0 until totalFloors) {
                val baseLevelY = groundY - (f * floorHeight)
                val topLevelY = baseLevelY - floorHeight

                // Draw main wall panel
                drawRect(
                    color = wallColor,
                    topLeft = Offset(leftBound, topLevelY),
                    size = Size(houseWidth, floorHeight)
                )

                // Render structural outline on edges
                drawRect(
                    color = trimColor,
                    topLeft = Offset(leftBound, topLevelY),
                    size = Size(houseWidth, floorHeight),
                    style = Stroke(width = 2.5f)
                )

                // Style Specific Facade Overlay Details
                when (style.lowercase()) {
                    "modern" -> {
                        // Drawing metal cladding slats or brick patterns
                        for (cx in (leftBound + 20f).toInt()..rightBound.toInt() step 50) {
                            drawLine(
                                color = trimColor.copy(alpha = 0.15f),
                                start = Offset(cx.toFloat(), topLevelY),
                                end = Offset(cx.toFloat(), baseLevelY),
                                strokeWidth = 1f
                            )
                        }
                    }
                    "minimalist" -> {
                        // Very plain wood vertical cladding on outer edge
                        val woodSlatLeft = leftBound + 25f
                        val woodSlatRight = woodSlatLeft + 60f
                        drawRect(
                            color = Color(0xFFD7CCC8),
                            topLeft = Offset(woodSlatLeft, topLevelY),
                            size = Size(60f, floorHeight)
                        )
                        for (wx in woodSlatLeft.toInt()..woodSlatRight.toInt() step 8) {
                            drawLine(
                                color = Color(0xFF8D6E63),
                                start = Offset(wx.toFloat(), topLevelY),
                                end = Offset(wx.toFloat(), baseLevelY),
                                strokeWidth = 1f
                            )
                        }
                    }
                    "tudor" -> {
                        // Drawing traditional half-timber framing lines
                        drawLine(
                            color = trimColor,
                            start = Offset(leftBound, topLevelY),
                            end = Offset(rightBound, baseLevelY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = trimColor,
                            start = Offset(rightBound, topLevelY),
                            end = Offset(leftBound, baseLevelY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = trimColor,
                            start = Offset(leftBound + (houseWidth / 2f), topLevelY),
                            end = Offset(leftBound + (houseWidth / 2f), baseLevelY),
                            strokeWidth = 2f
                        )
                    }
                    "classic" -> {
                        // Classical block work pattern lines
                        for (cy in (topLevelY + 15f).toInt()..baseLevelY.toInt() step 20) {
                            drawLine(
                                color = trimColor.copy(alpha = 0.15f),
                                start = Offset(leftBound, cy.toFloat()),
                                end = Offset(rightBound, cy.toFloat()),
                                strokeWidth = 1f
                            )
                        }
                    }
                    "eco" -> {
                        // Accent green garden trellis covering left 20%
                        val plantWidth = houseWidth * 0.18f
                        drawRect(
                            color = Color(0xFFE8F5E9),
                            topLeft = Offset(leftBound + 10f, topLevelY),
                            size = Size(plantWidth, floorHeight)
                        )
                        // Creeping vines dots
                        for (py in topLevelY.toInt()..baseLevelY.toInt() step 12) {
                            for (px in (leftBound + 10f).toInt()..(leftBound + 10f + plantWidth).toInt() step 12) {
                                drawCircle(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.7f),
                                    radius = 3.5f + (py % 3),
                                    center = Offset(px.toFloat() + (py % 5), py.toFloat())
                                )
                            }
                        }
                    }
                }

                // Windows generation per floor
                val windowWidth = 35f
                val windowHeight = 25f
                val wY = topLevelY + (floorHeight - windowHeight) / 2f

                // Modern large glazed facades
                if (style.lowercase() == "modern" || style.lowercase() == "minimalist") {
                    // Left window
                    val winLeftX = leftBound + 35f
                    drawRect(
                        color = Color(0xB3E0F7FA),
                        topLeft = Offset(winLeftX, wY),
                        size = Size(windowWidth * 1.5f, windowHeight)
                    )
                    drawRect(
                        color = trimColor,
                        topLeft = Offset(winLeftX, wY),
                        size = Size(windowWidth * 1.5f, windowHeight),
                        style = Stroke(width = 1.5f)
                    )
                    // Glass glare lines
                    drawLine(Color.White, Offset(winLeftX + 5f, wY + windowHeight - 5f), Offset(winLeftX + 20f, wY + 5f), strokeWidth = 1.5f)

                    // Right window
                    val winRightX = rightBound - 35f - (windowWidth * 1.5f)
                    drawRect(
                        color = Color(0xB3E0F7FA),
                        topLeft = Offset(winRightX, wY),
                        size = Size(windowWidth * 1.5f, windowHeight)
                    )
                    drawRect(
                        color = trimColor,
                        topLeft = Offset(winRightX, wY),
                        size = Size(windowWidth * 1.5f, windowHeight),
                        style = Stroke(width = 1.5f)
                    )
                    drawLine(Color.White, Offset(winRightX + 5f, wY + windowHeight - 5f), Offset(winRightX + 20f, wY + 5f), strokeWidth = 1.5f)
                } else {
                    // Classic panel sash windows
                    val winLeftX = leftBound + 45f
                    drawRect(
                        color = Color(0x66E0F7FA),
                        topLeft = Offset(winLeftX, wY),
                        size = Size(windowWidth, windowHeight)
                    )
                    drawRect(
                        color = trimColor,
                        topLeft = Offset(winLeftX, wY),
                        size = Size(windowWidth, windowHeight),
                        style = Stroke(width = 1.5f)
                    )
                    // Grids
                    drawLine(trimColor, Offset(winLeftX + (windowWidth/2f), wY), Offset(winLeftX + (windowWidth/2f), wY + windowHeight), 1f)
                    drawLine(trimColor, Offset(winLeftX, wY + (windowHeight/2f)), Offset(winLeftX + windowWidth, wY + (windowHeight/2f)), 1f)

                    val winRightX = rightBound - 45f - windowWidth
                    drawRect(
                        color = Color(0x66E0F7FA),
                        topLeft = Offset(winRightX, wY),
                        size = Size(windowWidth, windowHeight)
                    )
                    drawRect(
                        color = trimColor,
                        topLeft = Offset(winRightX, wY),
                        size = Size(windowWidth, windowHeight),
                        style = Stroke(width = 1.5f)
                    )
                    drawLine(trimColor, Offset(winRightX + (windowWidth/2f), wY), Offset(winRightX + (windowWidth/2f), wY + windowHeight), 1f)
                    drawLine(trimColor, Offset(winRightX, wY + (windowHeight/2f)), Offset(winRightX + windowWidth, wY + (windowHeight/2f)), 1f)
                }
            }

            // 4. Draw Main Entrance Front Door on Ground Level
            val doorWidth = 32f
            val doorHeight = 44f
            val doorX = originX - (doorWidth / 2f)
            val doorY = groundY - doorHeight

            // Draw door frame
            drawRect(
                color = when (style.lowercase()) {
                    "tudor" -> Color(0xFF5D4037)
                    "eco" -> Color(0xFF81C784)
                    else -> Color(0xFFC2185B) // bold entry color accent
                },
                topLeft = Offset(doorX, doorY),
                size = Size(doorWidth, doorHeight)
            )
            drawRect(
                color = trimColor,
                topLeft = Offset(doorX, doorY),
                size = Size(doorWidth, doorHeight),
                style = Stroke(width = 2.5f)
            )
            // Door knob
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 2f,
                center = Offset(doorX + doorWidth - 6f, doorY + (doorHeight / 2f))
            )

            // 5. Draw Roof Structure (topLevelY of floor floors-1)
            val roofBaseY = groundY - (totalFloors * floorHeight)

            // Dynamic Roof styles
            when (style.lowercase()) {
                "tudor", "classic" -> {
                    // Pitched Triangular roof
                    val roofPeakY = roofBaseY - 60f
                    val path = Path().apply {
                        moveTo(leftBound - 15f, roofBaseY)
                        lineTo(originX, roofPeakY)
                        lineTo(rightBound + 15f, roofBaseY)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = trimColor
                    )
                    drawPath(
                        path = path,
                        color = trimColor.copy(alpha = 0.5f),
                        style = Stroke(width = 3f)
                    )
                }
                "eco" -> {
                    // Slanted solar green flat roof
                    val roofPath = Path().apply {
                        moveTo(leftBound - 5f, roofBaseY)
                        lineTo(rightBound + 5f, roofBaseY - 12f)
                        lineTo(rightBound + 5f, roofBaseY + 5f)
                        lineTo(leftBound - 5f, roofBaseY + 5f)
                        close()
                    }
                    drawPath(
                        path = roofPath,
                        color = Color(0xFF455A64) // slate tile
                    )
                    // Tiny tilted solar panels on Eco Roof
                    val sx1 = leftBound + 40f
                    val sy1 = roofBaseY - 3f
                    drawRect(
                        color = Color(0xFF1A237E),
                        topLeft = Offset(sx1, sy1 - 10f),
                        size = Size(50f, 10f)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(sx1, sy1 - 10f),
                        size = Size(50f, 10f),
                        style = Stroke(width = 1f)
                    )
                }
                else -> {
                    // Modern / Minimalist Sleek Flat Roof Parapet
                    drawRect(
                        color = trimColor,
                        topLeft = Offset(leftBound - 5f, roofBaseY - 12f),
                        size = Size(houseWidth + 10f, 12f)
                    )
                }
            }

            // 6. Draw Height measurements reference vertical lines & ticks on LHS
            val annotX = leftBound - 50f
            drawLine(
                color = accentPurple,
                start = Offset(annotX, groundY + 15f),
                end = Offset(annotX, roofBaseY - 20f),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            // Measurement Ticks
            val totalHeightFeet = totalFloors * 10
            for (f in 0..totalFloors) {
                // tick positions
                val ly = groundY - (f * floorHeight)
                drawLine(
                    color = accentPurple,
                    start = Offset(annotX - 6f, ly),
                    end = Offset(annotX + 6f, ly),
                    strokeWidth = 1.5f
                )

                val elevLabel = if (f == 0) "EL +0.0'" else "EL +${f * 10}.0'"
                val elevTextLayout = textMeasurer.measure(
                    text = elevLabel,
                    style = TextStyle(color = accentPurple, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )
                drawText(
                    elevTextLayout,
                    topLeft = Offset(annotX - elevTextLayout.size.width - 8f, ly - (elevTextLayout.size.height / 2f))
                )
            }

            // Label main screen code
            val titleLayout = textMeasurer.measure(
                text = "${style.uppercase()} ELEVATION | HEIGHT: ${totalHeightFeet}FT",
                style = TextStyle(
                    color = Color(0xFF21005D),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                )
            )
            drawText(
                titleLayout,
                topLeft = Offset((width - titleLayout.size.width) / 2f, height - 30f)
            )
        }
    }
}
