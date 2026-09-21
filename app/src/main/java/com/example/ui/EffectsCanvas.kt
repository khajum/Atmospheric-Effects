package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.Balloon
import com.example.model.EffectType
import com.example.model.Snowflake
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EffectsCanvas(
  activeEffect: EffectType,
  elapsedTimeMs: Long,
  snowflakes: List<Snowflake>,
  balloons: List<Balloon>,
  modifier: Modifier = Modifier
) {
  if (activeEffect == EffectType.NONE) return

  Canvas(modifier = modifier.fillMaxSize()) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

    when (activeEffect) {
      EffectType.SNOWFLAKES -> {
        drawSnowflakes(
          snowflakes = snowflakes,
          elapsedTimeMs = elapsedTimeMs,
          canvasWidth = canvasWidth,
          canvasHeight = canvasHeight
        )
      }
      EffectType.BALLOONS -> {
        drawBalloons(
          balloons = balloons,
          elapsedTimeMs = elapsedTimeMs,
          canvasWidth = canvasWidth,
          canvasHeight = canvasHeight
        )
      }
      EffectType.NONE -> { /* No-op */ }
    }
  }
}

private fun DrawScope.drawSnowflakes(
  snowflakes: List<Snowflake>,
  elapsedTimeMs: Long,
  canvasWidth: Float,
  canvasHeight: Float
) {
  val totalDuration = 5000f

  for (flake in snowflakes) {
    // Determine progress [0..1]
    // Normalized progress with flake delay and speed factor
    val localTime = (elapsedTimeMs - flake.delayMs).coerceAtLeast(0L).toFloat()
    val cycleProgress = (localTime / (totalDuration * 0.95f)).coerceIn(0f, 1.15f)

    // Medium snowflake radius in pixels (approx 15dp to 24dp)
    val radiusPx = flake.sizeDp.dp.toPx()
    val totalTravel = canvasHeight + radiusPx * 3f

    // Falling from top to bottom
    val currentY = -radiusPx * 1.5f + totalTravel * cycleProgress

    // Horizontal sway
    val swayPx = flake.swayAmplitude.dp.toPx()
    val swayOffset = sin((localTime * flake.swayFrequency * 0.005f) + flake.swayPhase) * swayPx
    val currentX = (flake.startX * canvasWidth) + swayOffset

    // Rotation angle
    val currentRotation = flake.initialRotation + (localTime * flake.rotationSpeed * 0.08f)

    // Fade in and out at extremities
    val fadeAlpha = when {
      cycleProgress < 0.08f -> (cycleProgress / 0.08f) * flake.alpha
      cycleProgress > 0.95f -> ((1.15f - cycleProgress) / 0.20f).coerceIn(0f, 1f) * flake.alpha
      else -> flake.alpha
    }

    if (currentY >= -radiusPx * 2f && currentY <= canvasHeight + radiusPx * 2f) {
      drawSnowflakeCrystal(
        centerX = currentX,
        centerY = currentY,
        radius = radiusPx,
        rotationDeg = currentRotation,
        alpha = fadeAlpha
      )
    }
  }
}

private fun DrawScope.drawSnowflakeCrystal(
  centerX: Float,
  centerY: Float,
  radius: Float,
  rotationDeg: Float,
  alpha: Float
) {
  val strokeColor = Color(0xFFF0F9FF).copy(alpha = alpha.coerceIn(0.1f, 1f))
  val mainStrokeWidth = (radius * 0.12f).coerceAtLeast(2.5f)
  val branchStrokeWidth = (radius * 0.09f).coerceAtLeast(1.8f)

  rotate(degrees = rotationDeg, pivot = Offset(centerX, centerY)) {
    // 6-fold dendritic symmetry
    val branches = 6
    val angleStep = (2 * PI / branches).toFloat()

    for (i in 0 until branches) {
      val angle = i * angleStep
      val cosA = cos(angle)
      val sinA = sin(angle)

      val armEndX = centerX + radius * cosA
      val armEndY = centerY + radius * sinA

      // Main spine
      drawLine(
        color = strokeColor,
        start = Offset(centerX, centerY),
        end = Offset(armEndX, armEndY),
        strokeWidth = mainStrokeWidth,
        cap = StrokeCap.Round
      )

      // Inner chevron branches at 45% of arm
      val innerDist = radius * 0.45f
      val innerBranchLen = radius * 0.32f
      drawChevronBranch(
        originX = centerX + innerDist * cosA,
        originY = centerY + innerDist * sinA,
        armAngle = angle,
        branchLength = innerBranchLen,
        color = strokeColor,
        strokeWidth = branchStrokeWidth
      )

      // Outer chevron branches at 75% of arm
      val outerDist = radius * 0.75f
      val outerBranchLen = radius * 0.24f
      drawChevronBranch(
        originX = centerX + outerDist * cosA,
        originY = centerY + outerDist * sinA,
        armAngle = angle,
        branchLength = outerBranchLen,
        color = strokeColor,
        strokeWidth = branchStrokeWidth
      )

      // Tip diamond accent
      val tipDist = radius * 0.92f
      drawCircle(
        color = strokeColor,
        radius = radius * 0.08f,
        center = Offset(centerX + tipDist * cosA, centerY + tipDist * sinA)
      )
    }

    // Central geometric hub
    drawCircle(
      color = strokeColor,
      radius = radius * 0.20f,
      center = Offset(centerX, centerY),
      style = Stroke(width = mainStrokeWidth)
    )
    drawCircle(
      color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 1f)),
      radius = radius * 0.08f,
      center = Offset(centerX, centerY),
      style = Fill
    )
  }
}

private fun DrawScope.drawChevronBranch(
  originX: Float,
  originY: Float,
  armAngle: Float,
  branchLength: Float,
  color: Color,
  strokeWidth: Float
) {
  val branchAngleOffset = (PI / 4).toFloat() // 45 degrees

  // Left branchlet
  val leftAngle = armAngle - branchAngleOffset
  drawLine(
    color = color,
    start = Offset(originX, originY),
    end = Offset(
      originX + branchLength * cos(leftAngle),
      originY + branchLength * sin(leftAngle)
    ),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round
  )

  // Right branchlet
  val rightAngle = armAngle + branchAngleOffset
  drawLine(
    color = color,
    start = Offset(originX, originY),
    end = Offset(
      originX + branchLength * cos(rightAngle),
      originY + branchLength * sin(rightAngle)
    ),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawBalloons(
  balloons: List<Balloon>,
  elapsedTimeMs: Long,
  canvasWidth: Float,
  canvasHeight: Float
) {
  val totalDuration = 5000f

  for (balloon in balloons) {
    val localTime = (elapsedTimeMs - balloon.delayMs).coerceAtLeast(0L).toFloat()
    val cycleProgress = (localTime / (totalDuration * 0.95f)).coerceIn(0f, 1.15f)

    val widthPx = balloon.widthDp.dp.toPx()
    val heightPx = balloon.heightDp.dp.toPx()
    val totalTravel = canvasHeight + heightPx * 3f

    // Rising from bottom to top
    val currentY = (canvasHeight + heightPx * 1.5f) - (totalTravel * cycleProgress)

    // Horizontal sway
    val swayPx = balloon.swayAmplitude.dp.toPx()
    val swayOffset = sin((localTime * balloon.swayFrequency * 0.004f) + balloon.swayPhase) * swayPx
    val currentX = (balloon.startX * canvasWidth) + swayOffset

    // Dynamic tilt wobble
    val tilt = balloon.tiltAngle + sin(localTime * 0.003f + balloon.swayPhase) * 4f

    if (currentY >= -heightPx * 2f && currentY <= canvasHeight + heightPx * 2.5f) {
      drawSingleBalloon(
        centerX = currentX,
        centerY = currentY,
        widthPx = widthPx,
        heightPx = heightPx,
        color = balloon.color,
        tiltDeg = tilt,
        localTime = localTime
      )
    }
  }
}

private fun DrawScope.drawSingleBalloon(
  centerX: Float,
  centerY: Float,
  widthPx: Float,
  heightPx: Float,
  color: Color,
  tiltDeg: Float,
  localTime: Float
) {
  rotate(degrees = tiltDeg, pivot = Offset(centerX, centerY)) {
    val halfW = widthPx / 2f
    val halfH = heightPx / 2f

    // 1. Dangling wavy string trailing below balloon knot
    val stringStart = Offset(centerX, centerY + halfH + 6.dp.toPx())
    val stringLength = 40.dp.toPx()
    val stringPath = Path().apply {
      moveTo(stringStart.x, stringStart.y)
      val wave1 = sin(localTime * 0.006f) * 6.dp.toPx()
      val wave2 = cos(localTime * 0.005f) * 8.dp.toPx()
      cubicTo(
        stringStart.x + wave1, stringStart.y + stringLength * 0.33f,
        stringStart.x - wave2, stringStart.y + stringLength * 0.66f,
        stringStart.x + wave1 * 0.5f, stringStart.y + stringLength
      )
    }
    drawPath(
      path = stringPath,
      color = Color(0xCCCBD5E1),
      style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    )

    // 2. Balloon Body Shape (egg-shaped)
    val balloonPath = Path().apply {
      moveTo(centerX, centerY - halfH)
      // Right upper curve down to knot base
      cubicTo(
        centerX + halfW * 1.05f, centerY - halfH * 0.9f,
        centerX + halfW * 1.02f, centerY + halfH * 0.3f,
        centerX + halfW * 0.22f, centerY + halfH
      )
      // Knot base flat connector
      lineTo(centerX - halfW * 0.22f, centerY + halfH)
      // Left lower curve up to apex
      cubicTo(
        centerX - halfW * 1.02f, centerY + halfH * 0.3f,
        centerX - halfW * 1.05f, centerY - halfH * 0.9f,
        centerX, centerY - halfH
      )
      close()
    }

    // Balloon fill with 3D gradient lighting
    val balloonBrush = Brush.radialGradient(
      colors = listOf(
        color.copy(alpha = 0.98f),
        color.copy(alpha = 0.95f),
        color.copy(red = color.red * 0.72f, green = color.green * 0.72f, blue = color.blue * 0.72f)
      ),
      center = Offset(centerX - halfW * 0.3f, centerY - halfH * 0.35f),
      radius = halfW * 1.4f
    )
    drawPath(path = balloonPath, brush = balloonBrush)

    // Balloon subtle outline for crisp edge definition
    drawPath(
      path = balloonPath,
      color = Color.White.copy(alpha = 0.22f),
      style = Stroke(width = 1.dp.toPx())
    )

    // 3. Specular highlight on upper-left curve (gives realistic 3D gloss)
    val highlightPath = Path().apply {
      val hLeft = centerX - halfW * 0.62f
      val hTop = centerY - halfH * 0.72f
      val hW = halfW * 0.38f
      val hH = halfH * 0.52f
      addOval(
        androidx.compose.ui.geometry.Rect(
          left = hLeft,
          top = hTop,
          right = hLeft + hW,
          bottom = hTop + hH
        )
      )
    }
    rotate(degrees = -25f, pivot = Offset(centerX - halfW * 0.45f, centerY - halfH * 0.45f)) {
      drawPath(
        path = highlightPath,
        color = Color.White.copy(alpha = 0.45f),
        style = Fill
      )
    }

    // 4. Balloon Knot at bottom
    val knotPath = Path().apply {
      val knotW = 6.dp.toPx()
      val knotH = 6.dp.toPx()
      val knotY = centerY + halfH
      moveTo(centerX, knotY)
      lineTo(centerX - knotW, knotY + knotH)
      lineTo(centerX + knotW, knotY + knotH)
      close()
    }
    val knotColor = Color(
      red = color.red * 0.65f,
      green = color.green * 0.65f,
      blue = color.blue * 0.65f
    )
    drawPath(path = knotPath, color = knotColor)
  }
}
