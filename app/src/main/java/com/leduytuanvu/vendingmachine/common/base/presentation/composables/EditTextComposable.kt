package com.leduytuanvu.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.leduytuanvu.vendingmachine.core.util.Logger
import kotlinx.coroutines.delay

@Composable
fun EditTextComposable(
    initText: String = "",
    keyboardTypeNumber: Boolean = false,
    keyboardTypePassword: Boolean = false,
    onTextChanged: (String) -> Unit,
) {
//    var text by remember { mutableStateOf(TextFieldValue(initText)) }
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val focusManager = LocalFocusManager.current
//
//    if(keyboardTypeNumber) {
//        TextField(
//            value = text,
//            onValueChange = { newText ->
//                text = newText
//                onTextChanged(newText.text)
//            },
//            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
//                detectTapGestures (
//                    onDoubleTap = {
//                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
//                        keyboardController?.show()
//                    }
//                )
//            },
//            textStyle = TextStyle(fontSize = 20.sp),
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//                imeAction = ImeAction.Done
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    keyboardController?.hide()
//                }
//            )
//        )
//    } else if(keyboardTypePassword) {
//        TextField(
//            value = text,
//            onValueChange = { newText ->
//                text = newText
//                onTextChanged(newText.text)
//            },
//            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
//                detectTapGestures (
//                    onDoubleTap = {
//                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
//                        keyboardController?.show()
//                    }
//                )
//            },
//            textStyle = TextStyle(fontSize = 20.sp),
//            visualTransformation = PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Password,
//                imeAction = ImeAction.Done
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    keyboardController?.hide()
//                }
//            )
//        )
//    } else {
//        TextField(
//            value = text,
//            onValueChange = { newText ->
//                text = newText
//                onTextChanged(newText.text)
//            },
//            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
//                detectTapGestures (
//                    onDoubleTap = {
//                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
//                        keyboardController?.show()
//                    }
//                )
//            },
//            textStyle = TextStyle(fontSize = 20.sp),
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Text,
//                imeAction = ImeAction.Done
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    keyboardController?.hide()
//                }
//            )
//        )
//    }
    var text by remember { mutableStateOf(TextFieldValue(initText)) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onTextChanged(newText.text)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        Logger.debug("onTap")
                        keyboardController?.show()
                    },
                    onDoubleTap = {
                        Logger.debug("onDoubleTap")
                        keyboardController?.show()
//                        focusRequester.requestFocus()
                    }
                )
            },
        textStyle = TextStyle(fontSize = 20.sp),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = when {
                keyboardTypeNumber -> KeyboardType.Number
                keyboardTypePassword -> KeyboardType.Password
                else -> KeyboardType.Text
            },
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (keyboardTypePassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        )
    )
}