package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Balloon
import com.example.model.EffectType
import com.example.model.Snowflake
import com.example.ui.theme.BalloonAmethyst
import com.example.ui.theme.BalloonCrimson
import com.example.ui.theme.BalloonEmerald
import com.example.ui.theme.BalloonGold
import com.example.ui.theme.BalloonRose
import com.example.ui.theme.BalloonSapphire
import com.example.ui.theme.BalloonTeal
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderLight
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkSurface
import com.example.ui.theme.SlateDarkSurfaceVariant
import com.example.ui.theme.SlateLightBackground
import com.example.ui.theme.SlateLightSurface
import com.example.ui.theme.SlateMutedText
import kotlin.random.Random

private const val EFFECT_DURATION_MS = 5000L

@Composable
fun FormalEffectsScreen(modifier: Modifier = Modifier) {
  val isDark = isSystemInDarkTheme()
  val haptic = LocalHapticFeedback.current

  // Active effect state
  var activeEffect by remember { mutableStateOf(EffectType.NONE) }
  var effectSessionId by remember { mutableStateOf(0L) }
  var elapsedTimeMs by remember { mutableLongStateOf(0L) }

  // Particle sets
  var snowflakes by remember { mutableStateOf<List<Snowflake>>(emptyList()) }
  var balloons by remember { mutableStateOf<List<Balloon>>(emptyList()) }

  // 5-second precision animation ticker
  LaunchedEffect(activeEffect, effectSessionId) {
    if (activeEffect == EffectType.NONE) {
      elapsedTimeMs = 0L
      return@LaunchedEffect
    }

    var startNano = -1L
    while (true) {
      withFrameNanos { frameTimeNano ->
        if (startNano < 0L) {
          startNano = frameTimeNano
        }
        val currentElapsed = (frameTimeNano - startNano) / 1_000_000L
        elapsedTimeMs = currentElapsed
      }
      if (elapsedTimeMs >= EFFECT_DURATION_MS) {
        activeEffect = EffectType.NONE
        elapsedTimeMs = 0L
        break
      }
    }
  }

  // Trigger effect helper
  fun startEffect(type: EffectType) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (type == EffectType.SNOWFLAKES) {
      snowflakes = generateMediumSnowflakes()
    } else if (type == EffectType.BALLOONS) {
      balloons = generateMediumBalloons()
    }
    activeEffect = type
    effectSessionId = System.currentTimeMillis()
    elapsedTimeMs = 0L
  }

  val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

  // Formal color dynamics
  val backgroundColor = if (isDark) SlateDarkBackground else SlateLightBackground
  val cardBg = if (isDark) SlateDarkSurface else SlateLightSurface
  val borderCol = if (isDark) SlateBorder else SlateBorderLight
  val primaryTextColor = if (isDark) PlatinumText else Color(0xFF0F172A)
  val subtitleTextColor = if (isDark) SlateMutedText else Color(0xFF475569)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundColor)
  ) {
    // 1. Full-screen particle animation canvas
    EffectsCanvas(
      activeEffect = activeEffect,
      elapsedTimeMs = elapsedTimeMs,
      snowflakes = snowflakes,
      balloons = balloons,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Formal UI layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          top = statusBarPadding + 16.dp,
          bottom = navBarPadding + 16.dp,
          start = 20.dp,
          end = 20.dp
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top section: Formal Emblem, Header, Subtitle
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Executive Suite Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) SlateDarkSurfaceVariant else Color(0xFFF1F5F9))
            .border(
              width = 1.dp,
              color = if (isDark) GoldPrimary.copy(alpha = 0.5f) else Color(0xFFC5A059),
              shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Official Badge",
            tint = if (isDark) GoldPrimary else Color(0xFF9A7B2C),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "FORMAL PRESENTATION SUITE",
            color = if (isDark) GoldPrimary else Color(0xFF9A7B2C),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
          text = "Visual Effects Console",
          color = primaryTextColor,
          fontSize = 28.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = (-0.5).sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle
        Text(
          text = "Initiate calibrated 5-second dynamic ambient sequences",
          color = subtitleTextColor,
          fontSize = 14.sp,
          textAlign = TextAlign.Center
        )
      }

      // Middle section: Status & Presentation Progress Monitor
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(
              borderCol,
              if (activeEffect != EffectType.NONE) GoldPrimary.copy(alpha = 0.6f) else borderCol
            )
          )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color.Black)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Status Header Line
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
          ) {
            val statusColor = when (activeEffect) {
              EffectType.SNOWFLAKES -> FrostAccent
              EffectType.BALLOONS -> GoldPrimary
              EffectType.NONE -> if (isDark) SlateMutedText else Color(0xFF94A3B8)
            }
            Icon(
              imageVector = Icons.Default.FiberManualRecord,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = when (activeEffect) {
                EffectType.SNOWFLAKES -> "STATUS: SNOWFLAKES IN CASCADE"
                EffectType.BALLOONS -> "STATUS: BALLOONS IN ASCENT"
                EffectType.NONE -> "STATUS: STANDBY — READY"
              },
              color = statusColor,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Description or Countdown
          if (activeEffect != EffectType.NONE) {
            val remainingSec = ((EFFECT_DURATION_MS - elapsedTimeMs).coerceAtLeast(0L) / 100f) / 10f
            val progress = (elapsedTimeMs.toFloat() / EFFECT_DURATION_MS).coerceIn(0f, 1f)

            Text(
              text = String.format("%.1fs remaining", remainingSec),
              color = primaryTextColor,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
              progress = { 1f - progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = if (activeEffect == EffectType.SNOWFLAKES) FrostAccent else GoldPrimary,
              trackColor = if (isDark) SlateDarkSurfaceVariant else Color(0xFFE2E8F0),
              strokeCap = StrokeCap.Round
            )
          } else {
            Text(
              text = "Press an effect button below to commence a 5-second sequence.",
              color = subtitleTextColor,
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
          }
        }
      }

      // Bottom section: The Two Requested Primary Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // "Snowflakes" Button
        FormalEffectButton(
          label = "Snowflakes",
          subtext = "5-second downward falling cascade",
          icon = Icons.Default.AcUnit,
          isActive = activeEffect == EffectType.SNOWFLAKES,
          accentColor = FrostAccent,
          isDark = isDark,
          testTag = "snowflakes_button",
          onClick = { startEffect(EffectType.SNOWFLAKES) }
        )

        // "Balloons" Button
        FormalEffectButton(
          label = "Balloons",
          subtext = "5-second upward floating ascent",
          icon = Icons.Default.Celebration,
          isActive = activeEffect == EffectType.BALLOONS,
          accentColor = if (isDark) GoldPrimary else Color(0xFFD97706),
          isDark = isDark,
          testTag = "balloons_button",
          onClick = { startEffect(EffectType.BALLOONS) }
        )
      }
    }
  }
}

@Composable
private fun FormalEffectButton(
  label: String,
  subtext: String,
  icon: ImageVector,
  isActive: Boolean,
  accentColor: Color,
  isDark: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val surfaceColor by animateColorAsState(
    targetValue = when {
      isActive -> accentColor.copy(alpha = if (isDark) 0.18f else 0.12f)
      isDark -> SlateDarkSurface
      else -> SlateLightSurface
    },
    animationSpec = tween(durationMillis = 200),
    label = "buttonSurfaceColor"
  )

  val borderColor by animateColorAsState(
    targetValue = when {
      isActive -> accentColor
      isDark -> SlateBorder
      else -> SlateBorderLight
    },
    animationSpec = tween(durationMillis = 200),
    label = "buttonBorderColor"
  )

  val iconBgColor by animateColorAsState(
    targetValue = when {
      isActive -> accentColor
      isDark -> SlateDarkSurfaceVariant
      else -> Color(0xFFF1F5F9)
    },
    animationSpec = tween(durationMillis = 200),
    label = "iconBgColor"
  )

  val iconTintColor by animateColorAsState(
    targetValue = when {
      isActive -> Color.White
      isDark -> accentColor
      else -> accentColor
    },
    animationSpec = tween(durationMillis = 200),
    label = "iconTintColor"
  )

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    modifier = Modifier
      .fillMaxWidth()
      .height(72.dp)
      .shadow(
        elevation = if (isActive) 8.dp else 2.dp,
        shape = RoundedCornerShape(16.dp)
      )
      .border(
        width = if (isActive) 2.dp else 1.2.dp,
        color = borderColor,
        shape = RoundedCornerShape(16.dp)
      )
      .clip(RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = accentColor),
        onClick = onClick
      )
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
      ) {
        // Icon Container
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(iconBgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTintColor,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Label and Subtext
        Column {
          Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) PlatinumText else Color(0xFF0F172A),
            letterSpacing = 0.2.sp
          )
          Text(
            text = subtext,
            fontSize = 12.sp,
            color = if (isDark) SlateMutedText else Color(0xFF64748B)
          )
        }
      }

      // Right action pill
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isActive) accentColor.copy(alpha = 0.25f)
            else (if (isDark) SlateDarkSurfaceVariant else Color(0xFFF1F5F9))
          )
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text(
          text = if (isActive) "ACTIVE" else "START",
          color = if (isActive) accentColor else (if (isDark) PlatinumText else Color(0xFF0F172A)),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
    }
  }
}

// Curated particle generators for medium-sized elements
private fun generateMediumSnowflakes(): List<Snowflake> {
  val flakes = mutableListOf<Snowflake>()
  val random = Random(42)

  // 24 medium snowflakes with natural stagger and variance
  for (i in 0 until 24) {
    val startX = (i + random.nextFloat() * 0.8f) / 24f
    val sizeDp = 15f + random.nextFloat() * 8f // Medium size: 15dp to 23dp radius (30dp to 46dp diameter)
    val swayAmp = 18f + random.nextFloat() * 16f
    val swayFreq = 0.8f + random.nextFloat() * 0.6f
    val swayPhase = random.nextFloat() * (2 * Math.PI.toFloat())
    val rotSpeed = -1.2f + random.nextFloat() * 2.4f
    val initialRot = random.nextFloat() * 360f
    val alpha = 0.78f + random.nextFloat() * 0.22f
    // Stagger delay between 0 and 1600ms so they fall continuously
    val delayMs = (random.nextFloat() * 1600f).toLong()

    flakes.add(
      Snowflake(
        id = i,
        startX = startX.coerceIn(0.04f, 0.96f),
        speedY = 1f,
        sizeDp = sizeDp,
        swayAmplitude = swayAmp,
        swayFrequency = swayFreq,
        swayPhase = swayPhase,
        rotationSpeed = rotSpeed,
        initialRotation = initialRot,
        alpha = alpha,
        delayMs = delayMs
      )
    )
  }
  return flakes
}

private fun generateMediumBalloons(): List<Balloon> {
  val balloons = mutableListOf<Balloon>()
  val jewelColors = listOf(
    BalloonSapphire,
    BalloonCrimson,
    BalloonGold,
    BalloonEmerald,
    BalloonAmethyst,
    BalloonRose,
    BalloonTeal
  )
  val random = Random(108)

  // 18 medium balloons with rich jewel tones and natural stagger
  for (i in 0 until 18) {
    val startX = (i + random.nextFloat() * 0.8f) / 18f
    val widthDp = 38f + random.nextFloat() * 10f // Medium width: 38dp to 48dp
    val heightDp = widthDp * (1.25f + random.nextFloat() * 0.15f) // Medium height: ~48dp to 62dp
    val swayAmp = 14f + random.nextFloat() * 18f
    val swayFreq = 0.7f + random.nextFloat() * 0.7f
    val swayPhase = random.nextFloat() * (2 * Math.PI.toFloat())
    val color = jewelColors[i % jewelColors.size]
    val tilt = -6f + random.nextFloat() * 12f
    val delayMs = (random.nextFloat() * 1500f).toLong()

    balloons.add(
      Balloon(
        id = i,
        startX = startX.coerceIn(0.06f, 0.94f),
        speedY = 1f,
        widthDp = widthDp,
        heightDp = heightDp,
        swayAmplitude = swayAmp,
        swayFrequency = swayFreq,
        swayPhase = swayPhase,
        color = color,
        tiltAngle = tilt,
        delayMs = delayMs
      )
    )
  }
  return balloons
}
