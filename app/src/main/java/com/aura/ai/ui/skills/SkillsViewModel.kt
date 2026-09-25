package com.aura.ai.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.local.entity.SkillEntity
import com.aura.ai.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SkillsViewModel @Inject constructor(
    repo: SkillRepository,
) : ViewModel() {
    val skills: StateFlow<List<SkillEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
