package com.b0966031908gmail.happypacker.ui.packing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.databinding.FragmentPackingTutorialBinding
import com.b0966031908gmail.happypacker.utils.TextToSpeechHelper
import kotlinx.coroutines.launch

/**
 * 包裝教學 Fragment（含語音播放功能）
 */
class PackingTutorialFragment : Fragment() {

    private var _binding: FragmentPackingTutorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PackingTutorialViewModel by viewModels()

    // 語音播放工具
    private lateinit var ttsHelper: TextToSpeechHelper
    private var isTtsInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackingTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化語音功能
        initializeTextToSpeech()

        // 設定觀察者和監聽器
        setupObservers()
        setupClickListeners()
        updateUI()
    }

    /**
     * 初始化文字轉語音
     */
    private fun initializeTextToSpeech() {
        ttsHelper = TextToSpeechHelper(requireContext())

        // 初始化 TTS
        ttsHelper.initialize { success ->
            isTtsInitialized = success

            if (success) {
                // 設定語音播放回調
                ttsHelper.setOnSpeakingStarted {
                    // 播放開始時更新按鈕
                    requireActivity().runOnUiThread {
                        binding.btnPlayAudio.text = "⏸️ 停止"
                    }
                }

                ttsHelper.setOnSpeakingDone {
                    // 播放完成時更新按鈕
                    requireActivity().runOnUiThread {
                        binding.btnPlayAudio.text = "🔊 播放"
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "語音功能初始化失敗，請確認系統支援中文語音",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * 觀察 ViewModel 資料變化
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentStepIndex.collect { index ->
                    // 切換步驟時停止語音
                    ttsHelper.stop()
                    updateUI()
                }
            }
        }
    }

    /**
     * 設定按鈕點擊事件
     */
    private fun setupClickListeners() {
        // 上一步
        binding.btnPrevious.setOnClickListener {
            viewModel.previousStep()
        }

        // 下一步/完成
        binding.btnNext.setOnClickListener {
            if (viewModel.isLastStep()) {
                // 停止語音並返回
                ttsHelper.stop()
                findNavController().navigateUp()
            } else {
                viewModel.nextStep()
            }
        }

        // 播放/停止語音
        binding.btnPlayAudio.setOnClickListener {
            handleAudioPlayback()
        }
    }

    /**
     * 處理語音播放/停止
     */
    private fun handleAudioPlayback() {
        if (!isTtsInitialized) {
            Toast.makeText(
                requireContext(),
                "語音功能尚未就緒，請稍候",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (ttsHelper.isSpeaking()) {
            // 正在播放，則停止
            ttsHelper.stop()
            binding.btnPlayAudio.text = "🔊 播放"
        } else {
            // 未播放，則開始播放
            val currentStep = viewModel.getCurrentStep()
            ttsHelper.speak(currentStep.audioText)
        }
    }

    /**
     * 更新 UI
     */
    private fun updateUI() {
        val currentStep = viewModel.getCurrentStep()
        val totalSteps = viewModel.getTotalSteps()

        // 更新進度
        binding.tvProgress.text = "步驟 ${currentStep.stepNumber} / $totalSteps"

        // 更新步驟資訊
        binding.tvStepTitle.text = currentStep.title
        binding.tvDescription.text = currentStep.description

        // 更新圖片
        currentStep.imageResId?.let { imageResId ->
            binding.ivStepImage.setImageResource(imageResId)
            binding.tvPlaceholder.visibility = View.GONE
        } ?: run {
            binding.ivStepImage.setImageResource(android.R.color.transparent)
            binding.tvPlaceholder.visibility = View.VISIBLE
        }

        // 更新按鈕狀態
        updateButtonStates()
    }

    /**
     * 更新按鈕狀態
     */
    private fun updateButtonStates() {
        // 上一步按鈕
        val hasPrevious = viewModel.hasPreviousStep()
        binding.btnPrevious.isEnabled = hasPrevious
        binding.btnPrevious.alpha = if (hasPrevious) 1.0f else 0.5f

        // 下一步/完成按鈕
        binding.btnNext.text = if (viewModel.isLastStep()) {
            "完成"
        } else {
            "下一步"
        }

        // 播放按鈕
        binding.btnPlayAudio.text = if (ttsHelper.isSpeaking()) {
            "⏸️ 停止"
        } else {
            "🔊 播放"
        }
    }

    /**
     * Fragment 暫停時停止語音
     */
    override fun onPause() {
        super.onPause()
        ttsHelper.stop()
    }

    /**
     * 釋放資源
     */
    override fun onDestroyView() {
        super.onDestroyView()
        ttsHelper.shutdown()
        _binding = null
    }

    companion object {
        fun newInstance() = PackingTutorialFragment()
    }
}