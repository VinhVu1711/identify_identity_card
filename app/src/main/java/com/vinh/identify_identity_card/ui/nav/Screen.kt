package com.vinh.identify_identity_card.ui.nav

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Manual : Screen("manual")
    data object History : Screen("history")


    data object ScanEntry : Screen("scan_entry")
    data object ScanCapture : Screen("scan_capture")
    data object ScanResult : Screen("scan_result")
}