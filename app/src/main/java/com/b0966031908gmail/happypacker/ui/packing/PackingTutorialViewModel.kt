package com.b0966031908gmail.happypacker.ui.packing

import androidx.lifecycle.ViewModel
import com.b0966031908gmail.happypacker.data.model.PackingStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PackingTutorialViewModel : ViewModel() {

    // 所有教學步驟
    private val steps = listOf(
        PackingStep(
            stepNumber = 1,
            title = "準備襪子",
            description = "準備好要包裝的襪子",
            audioText = "第一步，準備好要包裝的襪子"
        ),
        PackingStep(
            stepNumber = 2,
            title = "將襪子平放",
            description = "把襪子平放在桌面上",
            audioText = "第二步，把襪子平放在桌面上"
        ),
        PackingStep(
            stepNumber = 3,
            title = "折好襪子",
            description = "將襪子折好並調整形狀",
            audioText = "第三步，將襪子折好並調整形狀"
        ),
        PackingStep(
            stepNumber = 4,
            title = "放入包裝袋",
            description = "將折好的襪子放入包裝袋中",
            audioText = "第四步，將折好的襪子放入包裝袋中"
        ),
        PackingStep(
            stepNumber = 5,
            title = "封口",
            description = "封好包裝袋的開口",
            audioText = "第五步，封好包裝袋的開口"
        ),
        PackingStep(
            stepNumber = 6,
            title = "完成包裝",
            description = "完成！襪子包裝好了",
            audioText = "第六步，完成！襪子包裝好囉"
        )
    )

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex

    fun getCurrentStep(): PackingStep = steps[_currentStepIndex.value]

    fun getTotalSteps(): Int = steps.size

    fun hasNextStep(): Boolean = _currentStepIndex.value < steps.size - 1

    fun hasPreviousStep(): Boolean = _currentStepIndex.value > 0

    fun nextStep() {
        if (hasNextStep()) {
            _currentStepIndex.value++
        }
    }

    fun previousStep() {
        if (hasPreviousStep()) {
            _currentStepIndex.value--
        }
    }

    fun isLastStep(): Boolean = _currentStepIndex.value == steps.size - 1
}