package com.tudominio.checklistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.tudominio.checklistapp.navigation.SetupNavGraph
import com.tudominio.checklistapp.ui.theme.ChecklistAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChecklistAppTheme {
                // Crear un NavController para manejar la navegación
                val navController = rememberNavController()

                // Configurar el grafo de navegación con el NavController
                SetupNavGraph(navController = navController)
            }
        }
    }
}