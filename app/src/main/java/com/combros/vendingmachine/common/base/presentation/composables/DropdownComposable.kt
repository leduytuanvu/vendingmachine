package com.combros.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun DropdownComposable(
    items: List<AnnotatedString>,
    selectedItem: AnnotatedString,
    onItemSelected: (AnnotatedString) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box (modifier = Modifier.border(
        width = 1.dp,
        color = Color.Black,
        shape = MaterialTheme.shapes.small.copy(CornerSize(4.dp))
    )) {
        ClickableText(
            text = selectedItem,
            onClick = { expanded = !expanded },
            style = TextStyle(fontSize = 17.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 17.dp),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(screenWidth - 40.dp)
                .padding(horizontal = 7.dp)
                .wrapContentHeight()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.text, fontSize = 17.sp) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
