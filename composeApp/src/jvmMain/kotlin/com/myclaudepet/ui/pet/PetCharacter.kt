package com.myclaudepet.ui.pet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import com.myclaudepet.domain.model.Accessory
import com.myclaudepet.domain.model.PetAnimationState
import com.myclaudepet.domain.model.PetMood
import com.myclaudepet.ui.theme.PetColors
import com.myclaudepet.ui.theme.PetDimens

@Composable
fun PetCharacter(
    state: PetAnimationState,
    mood: PetMood,
    walkOffsetX: Float = 0f,
    walkOffsetY: Float = 0f,
    facingRight: Boolean = true,
    accessory: Accessory? = null,
    modifier: Modifier = Modifier,
) {
    val jumpOffsetDp by animateFloatAsState(
        targetValue = if (state == PetAnimationState.Jumping) -28f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "jump-offset",
    )
    // 좌우 반전 (걷기 방향 표현). Walking 상태에서만 의미 있고 다른 상태는 항상 오른쪽 기본.
    val scaleX = if (state == PetAnimationState.Walking && !facingRight) -1f else 1f
    val animatedModifier = modifier
        .offset(
            x = walkOffsetX.dp,
            y = (walkOffsetY + jumpOffsetDp).dp,
        )
        .scale(scaleX = scaleX, scaleY = 1f)
    val sprite = rememberPetSprite(state, accessory)
    if (sprite != null) {
        Image(
            painter = BitmapPainter(sprite),
            contentDescription = state.name,
            contentScale = ContentScale.Fit,
            modifier = animatedModifier.size(PetDimens.PetSize),
        )
    } else {
        PetCharacterCanvasFallback(mood = mood, modifier = animatedModifier)
    }
}

@Composable
private fun PetCharacterCanvasFallback(
    mood: PetMood,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pet-idle")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )

    val moodValue by animateFloatAsState(targetValue = mood.ordinal.toFloat(), label = "mood")

    Canvas(modifier = modifier.size(PetDimens.PetSize)) {
        val w = size.width
        val h = size.height
        val squash = 1f - breath * 0.06f
        val bodySize = Size(w * 0.78f * squash, h * 0.78f)
        val bodyTopLeft = Offset((w - bodySize.width) / 2f, h - bodySize.height - h * 0.05f)
        val color = lerpMoodColor(moodValue)

        drawOval(color, topLeft = bodyTopLeft, size = bodySize)

        val eyeY = bodyTopLeft.y + bodySize.height * 0.42f
        val eyeOffset = bodySize.width * 0.18f
        val eyeCenterX = bodyTopLeft.x + bodySize.width / 2f
        val eyeRadius = bodySize.width * 0.06f
        val eyeOpen = when (mood) {
            PetMood.Sad -> 0.35f
            PetMood.Neutral -> 0.8f
            PetMood.Happy -> 1f
        }
        drawEye(Offset(eyeCenterX - eyeOffset, eyeY), eyeRadius, eyeOpen)
        drawEye(Offset(eyeCenterX + eyeOffset, eyeY), eyeRadius, eyeOpen)

        drawCheek(Offset(eyeCenterX - eyeOffset * 1.6f, eyeY + eyeRadius * 2f), eyeRadius)
        drawCheek(Offset(eyeCenterX + eyeOffset * 1.6f, eyeY + eyeRadius * 2f), eyeRadius)

        drawMouth(
            center = Offset(eyeCenterX, eyeY + eyeRadius * 3.4f),
            width = bodySize.width * 0.24f,
            mood = mood,
        )
    }
}

private fun lerpMoodColor(value: Float): Color = when {
    value < 1f -> lerp(PetColors.BodySad, PetColors.BodyNeutral, value.coerceIn(0f, 1f))
    else -> lerp(PetColors.BodyNeutral, PetColors.BodyHappy, (value - 1f).coerceIn(0f, 1f))
}

private fun lerp(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)

private fun DrawScope.drawEye(center: Offset, radius: Float, openness: Float) {
    if (openness < 0.5f) {
        drawLine(
            color = PetColors.EyeDark,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = radius * 0.6f,
        )
    } else {
        drawCircle(color = PetColors.EyeDark, radius = radius * openness, center = center)
    }
}

private fun DrawScope.drawCheek(center: Offset, radius: Float) {
    drawCircle(color = PetColors.Cheek.copy(alpha = 0.55f), radius = radius, center = center)
}

private fun DrawScope.drawMouth(center: Offset, width: Float, mood: PetMood) {
    val curve = when (mood) {
        PetMood.Happy -> width * 0.35f
        PetMood.Neutral -> width * 0.1f
        PetMood.Sad -> -width * 0.25f
    }
    val path = Path().apply {
        moveTo(center.x - width / 2f, center.y)
        quadraticTo(center.x, center.y + curve, center.x + width / 2f, center.y)
    }
    drawPath(path = path, color = PetColors.EyeDark, style = Stroke(width = width * 0.1f))
}
