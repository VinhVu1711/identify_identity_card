package com.vinh.identify_identity_card.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinh.identify_identity_card.data.repo.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repo: HistoryRepository): ViewModel() {
    val items = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),emptyList())
}