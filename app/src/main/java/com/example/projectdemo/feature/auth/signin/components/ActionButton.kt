package com.example.projectdemo.feature.auth.signin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectdemo.R
import com.example.projectdemo.ui.theme.Pink40
import com.example.projectdemo.ui.theme.ProjectDemoTheme

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    isNavigationArrowVisible: Boolean,
    onClicked: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    shadowColor: Color = Color.Black
) {
    Button(
        onClick = onClicked,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = shadowColor
            ),
        colors = colors,
        shape = RoundedCornerShape(percent = 50)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (isNavigationArrowVisible) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_right_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
private fun ActionButtonPreview_NavigationVisible(modifier: Modifier = Modifier) {
    ProjectDemoTheme() {
        ActionButton(
            modifier = modifier,
            text = "Action text",
            isNavigationArrowVisible = true,
            onClicked = {},
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Pink40
            ),
            shadowColor = Pink40
        )

    }

}

@Composable
private fun ActionButtonPreview(modifier: Modifier = Modifier) {
    ProjectDemoTheme() {
        ActionButton(
            modifier = modifier,
            text = "Action text",
            isNavigationArrowVisible = false,
            onClicked = {},
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Pink40
            ),
            shadowColor = Pink40
        )

    }

}