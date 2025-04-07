package com.tudominio.checklistapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Mock data class similar to your InspectionEntity
data class MockInspection(
    val id: String,
    val equipment: String,
    val inspector: String,
    val date: LocalDateTime,
    val conformityPercentage: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {}
) {
    // State for search and filters
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilterOption by remember { mutableStateOf("Todos") }
    val filterOptions = listOf("Todos", "Alta Conformidad", "Media Conformidad", "Baja Conformidad")

    // Create mock data
    val allMockInspections = remember {
        listOf(
            MockInspection(
                id = "1",
                equipment = "CAEX 301",
                inspector = "Juan Pérez",
                date = LocalDateTime.now().minusDays(1),
                conformityPercentage = 95f
            ),
            MockInspection(
                id = "2",
                equipment = "CAEX 302",
                inspector = "María González",
                date = LocalDateTime.now().minusDays(3),
                conformityPercentage = 82f
            ),
            MockInspection(
                id = "3",
                equipment = "CAEX 303",
                inspector = "Pedro Rodríguez",
                date = LocalDateTime.now().minusDays(5),
                conformityPercentage = 65f
            ),
            MockInspection(
                id = "4",
                equipment = "CAEX 304",
                inspector = "Ana López",
                date = LocalDateTime.now().minusDays(7),
                conformityPercentage = 45f
            ),
            MockInspection(
                id = "5",
                equipment = "CAEX 305",
                inspector = "Carlos Martínez",
                date = LocalDateTime.now().minusDays(9),
                conformityPercentage = 92f
            )
        )
    }

    // Apply search and filters
    val filteredInspections = allMockInspections.filter { inspection ->
        // Apply search query
        val matchesSearch = searchQuery.isEmpty() ||
                inspection.equipment.contains(searchQuery, ignoreCase = true) ||
                inspection.inspector.contains(searchQuery, ignoreCase = true)

        // Apply conformity filter
        val matchesFilter = when (selectedFilterOption) {
            "Alta Conformidad" -> inspection.conformityPercentage >= 90f
            "Media Conformidad" -> inspection.conformityPercentage >= 70f && inspection.conformityPercentage < 90f
            "Baja Conformidad" -> inspection.conformityPercentage < 70f
            else -> true // "Todos"
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Inspecciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros"
                        )
                    }
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar por equipo o inspector") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true
            )

            // Filters
            AnimatedVisibility(visible = showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FilterChipGroup(
                            options = filterOptions,
                            selectedOption = selectedFilterOption,
                            onOptionSelected = { selectedFilterOption = it }
                        )
                    }
                }
            }

            // Info message
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Datos de ejemplo. El acceso al historial completo estará disponible en futuras versiones.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Results count
            Text(
                text = "${filteredInspections.size} inspecciones encontradas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Inspections list
            if (filteredInspections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron inspecciones",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInspections) { inspection ->
                        InspectionCard(inspection = inspection)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionCard(inspection: MockInspection) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = inspection.date.format(dateFormatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { /* No action for now */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equipo: ${inspection.equipment}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fecha: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Inspector: ${inspection.inspector}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Conformity indicator
            val conformityColor = when {
                inspection.conformityPercentage >= 90f -> Green
                inspection.conformityPercentage >= 70f -> Yellow
                else -> Red
            }

            Text(
                text = "Conformidad: ${inspection.conformityPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = inspection.conformityPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = conformityColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}