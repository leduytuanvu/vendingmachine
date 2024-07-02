package com.combros.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
    paddingTop: Dp = 8.dp,
    paddingBottom: Dp = 20.dp,
    onItemSelected: (AnnotatedString) -> Unit,
) {
    if(title.isEmpty()) {
        Spacer(modifier = Modifier.height(paddingTop))
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
    Spacer(modifier = Modifier.height(paddingBottom))
}