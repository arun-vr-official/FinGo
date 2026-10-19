package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class AppThemeStyle(
    val code: String,
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    PLAIN(
        "PLAIN",
        "Plain",
        "Classic solid finish (Plain Dark in dark mode, Plain Light in silver)",
        FinEmerald,
        FinBlue
    ),
    AURORA_EMERALD(
        "AURORA_EMERALD",
        "Aurora Emerald",
        "Lush emerald & mint luminous glowing waves with radiant nodes",
        Color(0xFF10B981),
        Color(0xFF06B6D4)
    ),
    CYBER_NEON(
        "CYBER_NEON",
        "Cyber Neon",
        "Electric magenta & neon cyan tech matrix with glowing pins",
        Color(0xFFEC4899),
        Color(0xFF00F5D4)
    ),
    COSMIC_VIOLET(
        "COSMIC_VIOLET",
        "Cosmic Violet",
        "Galactic royal purple & electric indigo celestial beams and stars",
        Color(0xFF8B5CF6),
        Color(0xFF6366F1)
    ),
    SUNSET_AMBER(
        "SUNSET_AMBER",
        "Sunset Amber",
        "Golden amber, warm tangerine, and fiery coral radiant rays",
        Color(0xFFF59E0B),
        Color(0xFFF43F5E)
    ),
    OCEAN_AZURE(
        "OCEAN_AZURE",
        "Ocean Azure",
        "Deep marine sapphire & bright turquoise fluid tidal waves",
        Color(0xFF0284C7),
        Color(0xFF14B8A6)
    ),
    RAINBOW_PRISM(
        "RAINBOW_PRISM",
        "Rainbow Prism",
        "Dynamic multicolored geometric prisms and jewel sparkle confetti",
        Color(0xFFF43F5E),
        Color(0xFFFBBF24)
    ),
    ARCH_GRID(
        "ARCH_GRID",
        "Architectural Grid",
        "High-precision architectural blueprint grid with cyan coordinate pins",
        Color(0xFF0EA5E9),
        Color(0xFF10B981)
    ),
    MINIMAL_DOTS(
        "MINIMAL_DOTS",
        "Minimal Dots",
        "Delicate matrix of points with colorful alternating accent nodes",
        Color(0xFF64748B),
        Color(0xFF3B82F6)
    ),
    MIDNIGHT_ROSE(
        "MIDNIGHT_ROSE",
        "Midnight Rose",
        "Velvet crimson & rose gold shimmering waves with radiant pearl accents",
        Color(0xFFFB7185),
        Color(0xFFE11D48)
    ),
    RETRO_SYNTHWAVE(
        "RETRO_SYNTHWAVE",
        "Neon Horizon",
        "Synthwave retro-futuristic perspective neon grid with glowing sun rays",
        Color(0xFFEC4899),
        Color(0xFFF97316)
    ),
    ZEN_FOREST(
        "ZEN_FOREST",
        "Zen Forest & Jade",
        "Serene organic jade contours & bamboo matcha ripple rings",
        Color(0xFF10B981),
        Color(0xFF84CC16)
    ),
    TROPICAL_CITRUS(
        "TROPICAL_CITRUS",
        "Tropical Citrus",
        "High-energy tangerine, electric lime & golden solar flares",
        Color(0xFFF97316),
        Color(0xFFA3E635)
    ),
    LAVENDER_DREAM(
        "LAVENDER_DREAM",
        "Lavender Dream",
        "Pastel lilac, soft cyan & candy cloud rings with sparkle constellations",
        Color(0xFFA855F7),
        Color(0xFF38BDF8)
    ),
    CHAMPAGNE_GOLD(
        "CHAMPAGNE_GOLD",
        "Champagne & 24K Gold",
        "Ultra-luxury currency guilloché rosettes & shimmering gold banknote facets",
        Color(0xFFF59E0B),
        Color(0xFFFDE68A)
    );

    companion object {
        fun fromCode(code: String): AppThemeStyle {
            return when (code.uppercase()) {
                "PLAIN" -> PLAIN
                "AURORA_EMERALD", "WAVES" -> AURORA_EMERALD
                "CYBER_NEON", "MESH" -> CYBER_NEON
                "COSMIC_VIOLET" -> COSMIC_VIOLET
                "SUNSET_AMBER" -> SUNSET_AMBER
                "OCEAN_AZURE" -> OCEAN_AZURE
                "RAINBOW_PRISM" -> RAINBOW_PRISM
                "ARCH_GRID", "GRID" -> ARCH_GRID
                "MINIMAL_DOTS", "DOTS" -> MINIMAL_DOTS
                "MIDNIGHT_ROSE", "ROSE" -> MIDNIGHT_ROSE
                "RETRO_SYNTHWAVE", "SYNTHWAVE", "HORIZON" -> RETRO_SYNTHWAVE
                "ZEN_FOREST", "FOREST", "JADE" -> ZEN_FOREST
                "TROPICAL_CITRUS", "CITRUS" -> TROPICAL_CITRUS
                "LAVENDER_DREAM", "LAVENDER", "PASTEL" -> LAVENDER_DREAM
                "CHAMPAGNE_GOLD", "GOLD" -> CHAMPAGNE_GOLD
                else -> PLAIN
            }
        }
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = FinEmerald,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = FinBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFBFDBFE),
    tertiary = FinPurple,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = FinRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FinEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = FinBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = FinPurple,
    background = BackgroundLight, // Elegant silver
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = FinRose,
    onError = Color.White
)

/**
 * Modifier to render custom vector pattern textures that adapt cleanly
 * to both Dark and Light (silver) modes with vivid, distinctive colors.
 */
fun Modifier.themePattern(
    style: String,
    isDark: Boolean,
    backgroundColor: Color
): Modifier = this.drawBehind {
    // 1. Draw base background
    drawRect(color = backgroundColor)

    val resolvedStyle = AppThemeStyle.fromCode(style)
    if (resolvedStyle == AppThemeStyle.PLAIN) {
        return@drawBehind
    }

    when (resolvedStyle) {
        AppThemeStyle.PLAIN -> { /* Already drawn */ }

        AppThemeStyle.AURORA_EMERALD -> {
            val emeraldColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFF059669).copy(alpha = 0.24f)
            val cyanColor = if (isDark) Color(0xFF06B6D4).copy(alpha = 0.18f) else Color(0xFF0284C7).copy(alpha = 0.20f)
            val mintGlow = if (isDark) Color(0xFF34D399).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.22f)

            val waveHeight = 72.dp.toPx()
            val wavelength = 140.dp.toPx()
            var yBase = -20f
            var waveIndex = 0

            while (yBase <= size.height + waveHeight) {
                val path = Path()
                path.moveTo(0f, yBase)
                var x = 0f
                val color = if (waveIndex % 2 == 0) emeraldColor else cyanColor
                val strokeW = if (waveIndex % 2 == 0) 1.8.dp.toPx() else 1.2.dp.toPx()

                while (x <= size.width + wavelength) {
                    val crestOffset = if (waveIndex % 2 == 0) 22f else -18f
                    path.quadraticTo(
                        x + wavelength / 4, yBase + crestOffset,
                        x + wavelength / 2, yBase
                    )
                    path.quadraticTo(
                        x + 3 * wavelength / 4, yBase - crestOffset,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }
                drawPath(path = path, color = color, style = Stroke(width = strokeW))

                // Radiant nodes along waves
                if (waveIndex % 2 == 0) {
                    var nodeX = 40.dp.toPx()
                    while (nodeX <= size.width) {
                        drawCircle(color = mintGlow, radius = 3.dp.toPx(), center = Offset(nodeX, yBase))
                        nodeX += 90.dp.toPx()
                    }
                }

                yBase += waveHeight
                waveIndex++
            }
        }

        AppThemeStyle.CYBER_NEON -> {
            val magentaColor = if (isDark) Color(0xFFEC4899).copy(alpha = 0.22f) else Color(0xFFDB2777).copy(alpha = 0.22f)
            val cyanColor = if (isDark) Color(0xFF00F5D4).copy(alpha = 0.20f) else Color(0xFF0891B2).copy(alpha = 0.22f)
            val pulseColor = if (isDark) Color(0xFFF472B6).copy(alpha = 0.35f) else Color(0xFFEC4899).copy(alpha = 0.30f)

            val step = 32.dp.toPx()
            val total = size.width + size.height
            var d = -size.height

            while (d <= total) {
                drawLine(
                    color = magentaColor,
                    start = Offset(d, 0f),
                    end = Offset(d + size.height, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawLine(
                    color = cyanColor,
                    start = Offset(d, size.height),
                    end = Offset(d + size.height, 0f),
                    strokeWidth = 1.2.dp.toPx()
                )
                d += step
            }

            // Glowing neon intersection nodes
            var x = step
            while (x <= size.width) {
                var y = step
                while (y <= size.height) {
                    drawCircle(color = pulseColor, radius = 2.dp.toPx(), center = Offset(x, y))
                    y += step * 2
                }
                x += step * 2
            }
        }

        AppThemeStyle.COSMIC_VIOLET -> {
            val violetColor = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.24f) else Color(0xFF7C3AED).copy(alpha = 0.24f)
            val indigoColor = if (isDark) Color(0xFF6366F1).copy(alpha = 0.18f) else Color(0xFF4F46E5).copy(alpha = 0.20f)
            val starColor = if (isDark) Color(0xFFDDD6FE).copy(alpha = 0.35f) else Color(0xFF6D28D9).copy(alpha = 0.30f)

            val waveHeight = 80.dp.toPx()
            val wavelength = 160.dp.toPx()
            var yBase = 0f
            var idx = 0

            while (yBase <= size.height + waveHeight) {
                val path = Path()
                path.moveTo(0f, yBase)
                var x = 0f
                while (x <= size.width + wavelength) {
                    path.cubicTo(
                        x + wavelength * 0.25f, yBase - 28f,
                        x + wavelength * 0.75f, yBase + 28f,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }
                drawPath(
                    path = path,
                    color = if (idx % 2 == 0) violetColor else indigoColor,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Celestial star crosses
                var starX = (idx * 28.dp.toPx()) % 120.dp.toPx() + 20.dp.toPx()
                while (starX <= size.width) {
                    val starCenter = Offset(starX, yBase + 12f)
                    val starSize = 4.dp.toPx()
                    drawLine(starColor, Offset(starCenter.x - starSize, starCenter.y), Offset(starCenter.x + starSize, starCenter.y), strokeWidth = 1.2.dp.toPx())
                    drawLine(starColor, Offset(starCenter.x, starCenter.y - starSize), Offset(starCenter.x, starCenter.y + starSize), strokeWidth = 1.2.dp.toPx())
                    starX += 110.dp.toPx()
                }

                yBase += waveHeight
                idx++
            }
        }

        AppThemeStyle.SUNSET_AMBER -> {
            val amberColor = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.22f) else Color(0xFFD97706).copy(alpha = 0.24f)
            val coralColor = if (isDark) Color(0xFFF43F5E).copy(alpha = 0.18f) else Color(0xFFE11D48).copy(alpha = 0.20f)
            val sunGlow = if (isDark) Color(0xFFFBBF24).copy(alpha = 0.30f) else Color(0xFFB45309).copy(alpha = 0.25f)

            val spacing = 48.dp.toPx()
            var r = spacing
            val maxR = size.width + size.height

            // Concentric radiating sunburst arcs from top-right corner
            val center = Offset(size.width, 0f)
            var ringIdx = 0
            while (r <= maxR) {
                drawCircle(
                    color = if (ringIdx % 2 == 0) amberColor else coralColor,
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                r += spacing
                ringIdx++
            }

            // Radiating sun ray dots
            var angle = 90.0
            while (angle <= 180.0) {
                val rad = Math.toRadians(angle)
                var dist = 60.dp.toPx()
                while (dist <= maxR) {
                    val dotCenter = Offset(
                        center.x + (dist * Math.cos(rad)).toFloat(),
                        center.y + (dist * Math.sin(rad)).toFloat()
                    )
                    if (dotCenter.x >= 0 && dotCenter.y >= 0 && dotCenter.x <= size.width && dotCenter.y <= size.height) {
                        drawCircle(color = sunGlow, radius = 2.5.dp.toPx(), center = dotCenter)
                    }
                    dist += 80.dp.toPx()
                }
                angle += 18.0
            }
        }

        AppThemeStyle.OCEAN_AZURE -> {
            val azureColor = if (isDark) Color(0xFF0284C7).copy(alpha = 0.22f) else Color(0xFF0369A1).copy(alpha = 0.25f)
            val tealColor = if (isDark) Color(0xFF14B8A6).copy(alpha = 0.18f) else Color(0xFF0D9488).copy(alpha = 0.22f)
            val rippleColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color(0xFF0284C7).copy(alpha = 0.25f)

            val waveHeight = 60.dp.toPx()
            val wavelength = 120.dp.toPx()
            var yBase = 0f
            var waveIdx = 0

            while (yBase <= size.height + waveHeight) {
                val path = Path()
                path.moveTo(0f, yBase)
                var x = 0f
                while (x <= size.width + wavelength) {
                    val amplitude = if (waveIdx % 2 == 0) 18f else 12f
                    path.quadraticTo(
                        x + wavelength / 4, yBase + amplitude,
                        x + wavelength / 2, yBase
                    )
                    path.quadraticTo(
                        x + 3 * wavelength / 4, yBase - amplitude,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }
                val waveCol = if (waveIdx % 2 == 0) azureColor else tealColor
                drawPath(path = path, color = waveCol, style = Stroke(width = 1.4.dp.toPx()))

                // Floating oceanic bubbles
                var bubbleX = 30.dp.toPx() + (waveIdx * 35.dp.toPx()) % 90.dp.toPx()
                while (bubbleX <= size.width) {
                    drawCircle(color = rippleColor, radius = 2.5.dp.toPx(), center = Offset(bubbleX, yBase + 4f))
                    bubbleX += 100.dp.toPx()
                }

                yBase += waveHeight
                waveIdx++
            }
        }

        AppThemeStyle.RAINBOW_PRISM -> {
            val colors = listOf(
                Color(0xFF10B981), // Emerald
                Color(0xFFF59E0B), // Amber
                Color(0xFFF43F5E), // Rose
                Color(0xFF8B5CF6), // Purple
                Color(0xFF06B6D4)  // Cyan
            )

            val step = 42.dp.toPx()
            var x = step / 2
            var rowIdx = 0

            while (x <= size.width + step) {
                var y = step / 2
                var colIdx = 0
                while (y <= size.height + step) {
                    val colorIdx = (rowIdx + colIdx) % colors.size
                    val col = colors[colorIdx].copy(alpha = if (isDark) 0.24f else 0.26f)
                    val offsetY = if (rowIdx % 2 == 1) step / 2 else 0f

                    // Diamond outline
                    val path = Path()
                    val dSize = 10.dp.toPx()
                    val cx = x
                    val cy = y + offsetY
                    path.moveTo(cx, cy - dSize)
                    path.lineTo(cx + dSize, cy)
                    path.lineTo(cx, cy + dSize)
                    path.lineTo(cx - dSize, cy)
                    path.close()

                    drawPath(path = path, color = col, style = Stroke(width = 1.2.dp.toPx()))

                    // Small center jewel sparkle
                    drawCircle(color = col, radius = 1.5.dp.toPx(), center = Offset(cx, cy))

                    y += step
                    colIdx++
                }
                x += step
                rowIdx++
            }
        }

        AppThemeStyle.ARCH_GRID -> {
            val gridColor = if (isDark) Color(0xFF0EA5E9).copy(alpha = 0.16f) else Color(0xFF0284C7).copy(alpha = 0.18f)
            val emeraldPin = if (isDark) Color(0xFF10B981).copy(alpha = 0.30f) else Color(0xFF059669).copy(alpha = 0.32f)

            val step = 32.dp.toPx()
            var x = 0f
            while (x <= size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += step
            }

            // Pin crosshairs & nodes at intersections
            x = 0f
            while (x <= size.width) {
                y = 0f
                while (y <= size.height) {
                    drawCircle(
                        color = emeraldPin,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                    y += step * 2
                }
                x += step * 2
            }
        }

        AppThemeStyle.MINIMAL_DOTS -> {
            val blueDot = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.22f) else Color(0xFF0284C7).copy(alpha = 0.24f)
            val slateDot = if (isDark) Color(0xFF94A3B8).copy(alpha = 0.12f) else Color(0xFF475569).copy(alpha = 0.14f)

            val step = 24.dp.toPx()
            var x = step / 2
            var colIndex = 0

            while (x <= size.width) {
                var y = step / 2
                var rowIndex = 0
                while (y <= size.height) {
                    val offsetY = if (colIndex % 2 == 1) step / 4 else 0f
                    val dotColor = if ((colIndex + rowIndex) % 3 == 0) blueDot else slateDot
                    val dotRadius = if ((colIndex + rowIndex) % 3 == 0) 2.2.dp.toPx() else 1.5.dp.toPx()

                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(x, y + offsetY)
                    )
                    y += step
                    rowIndex++
                }
                x += step
                colIndex++
            }
        }

        AppThemeStyle.MIDNIGHT_ROSE -> {
            val roseColor = if (isDark) Color(0xFFFB7185).copy(alpha = 0.24f) else Color(0xFFE11D48).copy(alpha = 0.25f)
            val crimsonColor = if (isDark) Color(0xFFE11D48).copy(alpha = 0.20f) else Color(0xFFBE123C).copy(alpha = 0.22f)
            val pearlGlow = if (isDark) Color(0xFFFDA4AF).copy(alpha = 0.32f) else Color(0xFFFB7185).copy(alpha = 0.28f)

            val waveHeight = 65.dp.toPx()
            val wavelength = 130.dp.toPx()
            var yBase = -10f
            var waveIdx = 0

            while (yBase <= size.height + waveHeight) {
                val path = Path()
                path.moveTo(0f, yBase)
                var x = 0f
                while (x <= size.width + wavelength) {
                    val crest = if (waveIdx % 2 == 0) 24f else -20f
                    path.cubicTo(
                        x + wavelength * 0.30f, yBase + crest,
                        x + wavelength * 0.70f, yBase - crest,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }
                drawPath(
                    path = path,
                    color = if (waveIdx % 2 == 0) roseColor else crimsonColor,
                    style = Stroke(width = if (waveIdx % 2 == 0) 1.6.dp.toPx() else 1.2.dp.toPx())
                )

                // Shimmering pearl diamond accents
                if (waveIdx % 2 == 0) {
                    var px = 30.dp.toPx() + (waveIdx * 25.dp.toPx()) % 80.dp.toPx()
                    while (px <= size.width) {
                        val dPath = Path()
                        val py = yBase
                        val dSize = 3.5.dp.toPx()
                        dPath.moveTo(px, py - dSize)
                        dPath.lineTo(px + dSize, py)
                        dPath.lineTo(px, py + dSize)
                        dPath.lineTo(px - dSize, py)
                        dPath.close()
                        drawPath(dPath, pearlGlow)
                        px += 95.dp.toPx()
                    }
                }

                yBase += waveHeight
                waveIdx++
            }
        }

        AppThemeStyle.RETRO_SYNTHWAVE -> {
            val magentaColor = if (isDark) Color(0xFFEC4899).copy(alpha = 0.26f) else Color(0xFFDB2777).copy(alpha = 0.26f)
            val orangeColor = if (isDark) Color(0xFFF97316).copy(alpha = 0.24f) else Color(0xFFEA580C).copy(alpha = 0.24f)
            val sunGlow = if (isDark) Color(0xFFFBBF24).copy(alpha = 0.32f) else Color(0xFFF59E0B).copy(alpha = 0.28f)

            // Perspective horizontal grid
            val horizonY = size.height * 0.35f
            var currY = horizonY
            var dy = 8.dp.toPx()
            var lineIdx = 0

            while (currY <= size.height) {
                drawLine(
                    color = if (lineIdx % 2 == 0) magentaColor else orangeColor,
                    start = Offset(0f, currY),
                    end = Offset(size.width, currY),
                    strokeWidth = (1f + (lineIdx * 0.15f)).dp.toPx().coerceAtMost(2.5.dp.toPx())
                )
                currY += dy
                dy *= 1.18f
                lineIdx++
            }

            // Radiating vertical perspective lines from horizon center
            val centerX = size.width / 2f
            val fanCount = 9
            for (i in 0..fanCount) {
                val bottomX = (size.width / fanCount) * i
                drawLine(
                    color = magentaColor.copy(alpha = magentaColor.alpha * 0.85f),
                    start = Offset(centerX, horizonY),
                    end = Offset(bottomX, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Digital sun rays above horizon
            var rayAngle = 20.0
            while (rayAngle <= 160.0) {
                val rad = Math.toRadians(rayAngle)
                val rayLen = size.width * 0.45f
                val rayEndX = centerX + (rayLen * Math.cos(rad)).toFloat()
                val rayEndY = horizonY - (rayLen * Math.sin(rad)).toFloat()
                drawLine(
                    color = sunGlow.copy(alpha = 0.15f),
                    start = Offset(centerX, horizonY),
                    end = Offset(rayEndX, rayEndY),
                    strokeWidth = 1.dp.toPx()
                )
                rayAngle += 20.0
            }
            // Sun glow node
            drawCircle(color = sunGlow.copy(alpha = 0.22f), radius = 24.dp.toPx(), center = Offset(centerX, horizonY))
        }

        AppThemeStyle.ZEN_FOREST -> {
            val jadeColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.24f) else Color(0xFF059669).copy(alpha = 0.25f)
            val bambooColor = if (isDark) Color(0xFF84CC16).copy(alpha = 0.20f) else Color(0xFF65A30D).copy(alpha = 0.22f)
            val leafGold = if (isDark) Color(0xFFEAB308).copy(alpha = 0.28f) else Color(0xFFCA8A04).copy(alpha = 0.26f)

            // Organic topographic contour ripples
            val center1 = Offset(size.width * 0.2f, size.height * 0.25f)
            val center2 = Offset(size.width * 0.85f, size.height * 0.75f)

            var r = 30.dp.toPx()
            var stepIdx = 0
            val maxR = (size.width + size.height) * 0.8f

            while (r <= maxR) {
                val color = if (stepIdx % 2 == 0) jadeColor else bambooColor
                drawCircle(color = color, radius = r, center = center1, style = Stroke(width = 1.3.dp.toPx()))
                drawCircle(color = color, radius = r * 0.85f, center = center2, style = Stroke(width = 1.2.dp.toPx()))
                r += 40.dp.toPx()
                stepIdx++
            }

            // Leaf vein accent nodes
            var lx = 20.dp.toPx()
            while (lx <= size.width) {
                var ly = 40.dp.toPx()
                while (ly <= size.height) {
                    drawCircle(color = leafGold, radius = 2.2.dp.toPx(), center = Offset(lx, ly))
                    ly += 80.dp.toPx()
                }
                lx += 90.dp.toPx()
            }
        }

        AppThemeStyle.TROPICAL_CITRUS -> {
            val tangerineColor = if (isDark) Color(0xFFF97316).copy(alpha = 0.24f) else Color(0xFFEA580C).copy(alpha = 0.25f)
            val limeColor = if (isDark) Color(0xFFA3E635).copy(alpha = 0.22f) else Color(0xFF65A30D).copy(alpha = 0.24f)
            val solarYellow = if (isDark) Color(0xFFFACC15).copy(alpha = 0.32f) else Color(0xFFEAB308).copy(alpha = 0.28f)

            val spacing = 45.dp.toPx()
            val total = size.width + size.height
            var d = -size.height

            // 45-degree energetic rays
            while (d <= total) {
                drawLine(
                    color = tangerineColor,
                    start = Offset(d, 0f),
                    end = Offset(d + size.height, size.height),
                    strokeWidth = 1.4.dp.toPx()
                )
                d += spacing
            }

            // Opposing 135-degree lime rays
            d = 0f
            while (d <= total) {
                drawLine(
                    color = limeColor,
                    start = Offset(d, 0f),
                    end = Offset(d - size.height, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
                d += spacing * 1.5f
            }

            // Citrus sparkle diamond nodes
            var x = spacing
            while (x <= size.width) {
                var y = spacing
                while (y <= size.height) {
                    val p = Path()
                    val s = 3.dp.toPx()
                    p.moveTo(x, y - s)
                    p.lineTo(x + s, y)
                    p.lineTo(x, y + s)
                    p.lineTo(x - s, y)
                    p.close()
                    drawPath(p, solarYellow)
                    y += spacing * 2
                }
                x += spacing * 2
            }
        }

        AppThemeStyle.LAVENDER_DREAM -> {
            val lilacColor = if (isDark) Color(0xFFA855F7).copy(alpha = 0.24f) else Color(0xFF9333EA).copy(alpha = 0.24f)
            val skyColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.20f) else Color(0xFF0284C7).copy(alpha = 0.22f)
            val candyGlow = if (isDark) Color(0xFFF472B6).copy(alpha = 0.30f) else Color(0xFFEC4899).copy(alpha = 0.26f)

            // Flowing cloud ribbon curves
            val waveHeight = 70.dp.toPx()
            val wavelength = 150.dp.toPx()
            var yBase = 0f
            var waveIdx = 0

            while (yBase <= size.height + waveHeight) {
                val path = Path()
                path.moveTo(0f, yBase)
                var x = 0f
                while (x <= size.width + wavelength) {
                    val offset = if (waveIdx % 2 == 0) 22f else -18f
                    path.quadraticTo(
                        x + wavelength * 0.25f, yBase + offset,
                        x + wavelength * 0.5f, yBase
                    )
                    path.quadraticTo(
                        x + wavelength * 0.75f, yBase - offset,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }
                drawPath(path = path, color = if (waveIdx % 2 == 0) lilacColor else skyColor, style = Stroke(width = 1.5.dp.toPx()))

                // Floating pastel bubble rings
                var bubbleX = 35.dp.toPx() + (waveIdx * 30.dp.toPx()) % 100.dp.toPx()
                while (bubbleX <= size.width) {
                    drawCircle(color = candyGlow, radius = 5.dp.toPx(), center = Offset(bubbleX, yBase + 6f), style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = candyGlow, radius = 1.5.dp.toPx(), center = Offset(bubbleX, yBase + 6f))
                    bubbleX += 110.dp.toPx()
                }

                yBase += waveHeight
                waveIdx++
            }
        }

        AppThemeStyle.CHAMPAGNE_GOLD -> {
            val goldColor = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.24f) else Color(0xFFD97706).copy(alpha = 0.26f)
            val champagneColor = if (isDark) Color(0xFFFDE68A).copy(alpha = 0.18f) else Color(0xFFB45309).copy(alpha = 0.20f)
            val sparkleGold = if (isDark) Color(0xFFFBBF24).copy(alpha = 0.35f) else Color(0xFF92400E).copy(alpha = 0.30f)

            // Banknote guilloché harmonized waves
            val waveHeight = 55.dp.toPx()
            val wavelength = 110.dp.toPx()
            var yBase = -10f
            var waveIdx = 0

            while (yBase <= size.height + waveHeight) {
                val path1 = Path()
                val path2 = Path()
                path1.moveTo(0f, yBase)
                path2.moveTo(0f, yBase)

                var x = 0f
                while (x <= size.width + wavelength) {
                    val amp = 16f
                    path1.cubicTo(
                        x + wavelength * 0.25f, yBase + amp,
                        x + wavelength * 0.75f, yBase - amp,
                        x + wavelength, yBase
                    )
                    path2.cubicTo(
                        x + wavelength * 0.25f, yBase - amp,
                        x + wavelength * 0.75f, yBase + amp,
                        x + wavelength, yBase
                    )
                    x += wavelength
                }

                drawPath(path = path1, color = goldColor, style = Stroke(width = 1.3.dp.toPx()))
                drawPath(path = path2, color = champagneColor, style = Stroke(width = 1.1.dp.toPx()))

                // Banknote security rosette crosshairs
                var starX = 40.dp.toPx() + (waveIdx * 35.dp.toPx()) % 80.dp.toPx()
                while (starX <= size.width) {
                    val center = Offset(starX, yBase)
                    val len = 4.dp.toPx()
                    drawLine(sparkleGold, Offset(center.x - len, center.y), Offset(center.x + len, center.y), strokeWidth = 1.2.dp.toPx())
                    drawLine(sparkleGold, Offset(center.x, center.y - len), Offset(center.x, center.y + len), strokeWidth = 1.2.dp.toPx())
                    drawCircle(color = sparkleGold, radius = 1.2.dp.toPx(), center = center)
                    starX += 100.dp.toPx()
                }

                yBase += waveHeight
                waveIdx++
            }
        }
    }
}

@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

