package cz.fit.cvut.sdk.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A draggable floating button for debug purposes
 */
@Composable
internal fun DraggableDebugButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean
) {
    // Track button position with state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Start position from bottom right with some padding
    val initialOffsetX = -16f
    val initialOffsetY = -64f

    // Use remember to initialize position only once
    offsetX = remember { initialOffsetX }
    offsetY = remember { initialOffsetY }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        SmallFloatingActionButton(
            onClick = onClick,

            shape = CircleShape,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "Tolgee Debug Mode",
                tint = if (isEnabled) Color.Unspecified else Color.Gray.copy(alpha = 0.6f)

            )
        }
    }
}