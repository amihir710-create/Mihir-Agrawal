package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.TrackSelectScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.gameDao())
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = GameViewModelFactory(application, repository)
                )
                
                val currentScreen by viewModel.currentScreen.collectAsState()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "MainMenu" -> MainMenuScreen(viewModel)
                            "Garage" -> GarageScreen(viewModel)
                            "TrackSelect" -> TrackSelectScreen(viewModel)
                            "GamePlay" -> GamePlayScreen(viewModel)
                            "Settings" -> SettingsScreen(viewModel)
                            else -> MainMenuScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
