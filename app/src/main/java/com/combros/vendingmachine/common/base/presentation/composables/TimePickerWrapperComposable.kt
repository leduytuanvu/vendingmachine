package com.combros.vendingmachine.common.base.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TimePicker

@Composable
fun TimePickerWrapperComposable(
    defaultHour: Int,
    defaultMinute: Int,
    onTimeSelected: (hourOfDay: Int, minute: Int) -> Unit
) {
    AndroidView(factory = { context ->
        TimePicker(context).apply {
            setIs24HourView(true)
            hour = defaultHour
            minute = defaultMinute
            setOnTimeChangedListener { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            }
        }
    })
}