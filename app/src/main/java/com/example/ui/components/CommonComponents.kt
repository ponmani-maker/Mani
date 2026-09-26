package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberStatusWarning
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseStatusCritical

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when {
        priority.contains("P1") || priority.equals("Critical", ignoreCase = true) ->
            Triple(RoseContainer.copy(alpha = 0.8f), Color(0xFFFF8DA1), "P1 Critical")
        priority.contains("P2") || priority.equals("High", ignoreCase = true) ->
            Triple(AmberContainer.copy(alpha = 0.8f), Color(0xFFFFC043), "P2 High")
        priority.contains("P3") || priority.equals("Medium", ignoreCase = true) ->
            Triple(CyanPrimaryContainer.copy(alpha = 0.8f), CyanLight, "P3 Medium")
        else ->
            Triple(Color(0xFF334155), Color(0xFF94A3B8), "P4 Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "open" -> Pair(Color(0xFF1E3A8A).copy(alpha = 0.6f), Color(0xFF60A5FA))
        "in progress" -> Pair(AmberContainer.copy(alpha = 0.7f), Color(0xFFFBBF24))
        "resolved" -> Pair(EmeraldContainer.copy(alpha = 0.7f), EmeraldStatusOk)
        "closed" -> Pair(Color(0xFF334155), Color(0xFF94A3B8))
        else -> Pair(Color(0xFF1E293B), Color(0xFF94A3B8))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CopyableCodeBlock(
    command: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$ $command",
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Command", command)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("copy_command_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy command",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    maxStars: Int = 5,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Default.Star else Icons.Outlined.StarOutline
            val tint = if (isSelected) Color(0xFFFBBF24) else Color(0xFF64748B)

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clickable(enabled = onRatingChanged != null) {
                        onRatingChanged?.invoke(i)
                    }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$i stars",
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
