package me.arianb.storm_robot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LabeledIconImage(iconVector: ImageVector, label: String) =
    LabeledIconImageHelper(
        image = { modifier, contentScale, contentDescription ->
            Image(
                imageVector = iconVector,
                modifier = modifier,
                contentScale = contentScale,
                contentDescription = contentDescription
            )
        },
        label = label
    )

@Composable
fun LabeledIconImage(iconPainter: Painter, label: String) =
    LabeledIconImageHelper(
        image = { modifier, contentScale, contentDescription ->
            Image(
                painter = iconPainter,
                modifier = modifier,
                contentScale = contentScale,
                contentDescription = contentDescription
            )
        },
        label = label
    )


@Composable
fun LabeledIconImageHelper(
    image: @Composable ((Modifier, ContentScale, String?) -> Unit),
    label: String
) {
    val modifiers = remember { Modifier.size(64.dp) }
    val contentScale = remember { ContentScale.Inside }
    val contentDescription = remember { null }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        image(modifiers, contentScale, contentDescription)
        Text(
            label,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
