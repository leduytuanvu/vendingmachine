package com.leduytuanvu.vendingmachine.common.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TitleAndDropdownComposable(
    title: String,
    items: List<AnnotatedString>,
    selectedItem: AnnotatedString,
    onItemSelected: (AnnotatedString) -> Unit,
) {
    if(title.isEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
    } else {
        Spacer(modifier = Modifier.height(12.dp))
        BodyTextComposable(title = title)
        Spacer(modifier = Modifier.height(14.dp))
    }
    DropdownComposable(
        items = items,
        selectedItem = selectedItem
    ) {
        onItemSelected(it)
    }
    Spacer(modifier = Modifier.height(20.dp))
}