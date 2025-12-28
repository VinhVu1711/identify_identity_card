package com.vinh.identify_identity_card.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vinh.identify_identity_card.domain.model.Gender
import com.vinh.identify_identity_card.domain.model.toDisplayVi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    label: String,
    valueText: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // ✅ anchor
            readOnly = true,
            value = valueText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

