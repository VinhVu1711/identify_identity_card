package com.vinh.identify_identity_card.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vinh.identify_identity_card.utils.toDdMmYyyy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    textValue: String,      // hiển thị dd/MM/yyyy
    onOpen: () -> Unit
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = textValue,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            onOpen()
                        }
                    }
                }
            }
    )
}