package com.example.model

import androidx.compose.ui.graphics.Color

enum class EffectType {
  NONE,
  SNOWFLAKES,
  BALLOONS
}

data class Snowflake(
  val id: Int,
  val startX: Float, // Relative X [0..1]
  val speedY: Float, // Fall speed in pixels/ms
  val sizeDp: Float, // Radius in dp (medium size: 14dp - 22dp)
  val swayAmplitude: Float, // Horizontal sway width in pixels
  val swayFrequency: Float, // Sway cycle speed
  val swayPhase: Float, // Initial phase offset
  val rotationSpeed: Float, // Rotation speed in deg/ms
  val initialRotation: Float,
  val alpha: Float,
  val branchesCount: Int = 6,
  val delayMs: Long = 0L // Delay before entering
)

data class Balloon(
  val id: Int,
  val startX: Float, // Relative X [0..1]
  val speedY: Float, // Rise speed in pixels/ms
  val widthDp: Float, // Medium width: 38dp - 50dp
  val heightDp: Float, // Medium height: 48dp - 64dp
  val swayAmplitude: Float, // Horizontal sway width in pixels
  val swayFrequency: Float, // Sway cycle speed
  val swayPhase: Float, // Initial phase offset
  val color: Color,
  val tiltAngle: Float, // Slight natural tilt
  val delayMs: Long = 0L // Delay before entering
)
