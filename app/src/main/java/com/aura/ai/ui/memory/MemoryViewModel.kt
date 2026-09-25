package com.aura.ai.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.local.entity.MemoryCategory
import com.aura.ai.data.local.entity.MemoryEntity
import com.aura.ai.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val repo: MemoryRepository,
) : ViewModel() {

    val memories: StateFlow<List<MemoryEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repo.remember(content, MemoryCategory.PREFERENCE, importance = 0.7f, source = "user")
        }
    }

    fun delete(id: Long) = viewModelScope.launch { repo.forget(id) }
}
