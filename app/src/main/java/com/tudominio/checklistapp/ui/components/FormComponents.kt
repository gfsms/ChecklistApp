package com.tudominio.checklistapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Campo de texto personalizado para los formularios de la aplicación.
 * Incluye etiqueta, campo de entrada y mensaje de error opcional.
 *
 * @param value Valor actual del campo
 * @param onValueChange Función que se llama cuando cambia el valor
 * @param label Etiqueta del campo
 * @param isError Indica si el campo tiene un error
 * @param errorMessage Mensaje a mostrar en caso de error
 * @param keyboardType Tipo de teclado a mostrar
 * @param imeAction Acción del botón de entrada del teclado
 * @param onImeAction Función que se llama al presionar el botón de acción del teclado
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() },
                onGo = { onImeAction() }
            ),
            singleLine = true
        )

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Campo de texto numérico especializado para valores numéricos como el horómetro.
 *
 * @param value Valor actual del campo
 * @param onValueChange Función que se llama cuando cambia el valor
 * @param label Etiqueta del campo
 * @param isError Indica si el campo tiene un error
 * @param errorMessage Mensaje a mostrar en caso de error
 * @param imeAction Acción del botón de entrada del teclado
 * @param onImeAction Función que se llama al presionar el botón de acción del teclado
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun NumericTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "",
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LabeledTextField(
        value = value,
        onValueChange = { newValue ->
            // Solo aceptamos números
            if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                onValueChange(newValue)
            }
        },
        label = label,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Number,
        imeAction = imeAction,
        onImeAction = onImeAction,
        modifier = modifier
    )
}