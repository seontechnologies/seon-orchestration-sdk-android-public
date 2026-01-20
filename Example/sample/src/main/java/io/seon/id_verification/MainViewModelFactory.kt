package io.seon.orchestration_sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.seon.orch_sdk.pub.OrchestrationService
import io.seon.orchestration_sample.MainViewModel

class MainViewModelFactory(
    private val orchestrationService: OrchestrationService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(orchestrationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
