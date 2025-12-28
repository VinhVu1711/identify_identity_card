package com.vinh.identify_identity_card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vinh.identify_identity_card.data.local.DatabaseProvider
import com.vinh.identify_identity_card.data.repo.HistoryRepository
import com.vinh.identify_identity_card.ui.history.HistoryScreen
import com.vinh.identify_identity_card.ui.home.HomeScreen
import com.vinh.identify_identity_card.ui.manual.ManualScreen
import com.vinh.identify_identity_card.ui.manual.ManualViewModel
import com.vinh.identify_identity_card.ui.history.HistoryViewModel
import com.vinh.identify_identity_card.ui.nav.Screen
import com.vinh.identify_identity_card.ui.theme.Identify_Identity_CardTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Identify_Identity_CardTheme {

                // 1️⃣ Init database + repository
                val database = remember {
                    DatabaseProvider.get(applicationContext)
                }
                val repository = remember {
                    HistoryRepository(database.historyDao())
                }

                // 2️⃣ Init ViewModels (manual DI)
                val manualViewModel = remember {
                    ManualViewModel(repository)
                }
                val historyViewModel = remember {
                    HistoryViewModel(repository)
                }

                // 3️⃣ Navigation
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize())
                { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {

                        composable(Screen.Home.route) {
                            HomeScreen(
                                onManual = {
                                    navController.navigate(Screen.Manual.route)
                                },
                                onHistory = {
                                    navController.navigate(Screen.History.route)
                                }
                            )
                        }

                        composable(Screen.Manual.route) {
                            ManualScreen(vm = manualViewModel, onBack = {navController.popBackStack()} )

                        }

                        composable(Screen.History.route) {
                            HistoryScreen(vm = historyViewModel, onBack = {navController.popBackStack()})
                        }
                    }
                }
            }
        }
    }
}
