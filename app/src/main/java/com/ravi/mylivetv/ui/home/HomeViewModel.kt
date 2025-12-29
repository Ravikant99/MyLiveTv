package com.ravi.mylivetv.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ravi.mylivetv.utils.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedTab = savedStateHandle.get<Int>(KEY_SELECTED_TAB) ?: 0,
            selectedItemIndex = savedStateHandle.get<Int>(KEY_SELECTED_ITEM) ?: -1
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectTab(tabIndex: Int) {
        savedStateHandle[KEY_SELECTED_TAB] = tabIndex
        _uiState.value = _uiState.value.copy(
            selectedTab = tabIndex,
            selectedItemIndex = -1 // Reset selection when tab changes
        )
        savedStateHandle[KEY_SELECTED_ITEM] = -1
    }

    fun selectItem(itemIndex: Int) {
        savedStateHandle[KEY_SELECTED_ITEM] = itemIndex
        _uiState.value = _uiState.value.copy(selectedItemIndex = itemIndex)
    }

    companion object {
        private const val KEY_SELECTED_TAB = "selected_tab"
        private const val KEY_SELECTED_ITEM = "selected_item"
    }
}


