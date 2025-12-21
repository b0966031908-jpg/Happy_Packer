package com.b0966031908gmail.happypacker.ui.packing

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 包裝教學 Fragment（含語音播放功能 + 美化版）
 */
class PackingTutorialFragment : Fragment() {

    private var _binding: FragmentPackingTutorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PackingTutorialViewModel by viewModels()

    // 文字轉語音
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

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

        initTextToSpeech()
        setupObservers()
        setupClickListeners()
        updateUI()
    }

    /**
     * 初始化文字轉語音
     */
    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // 設定語言為繁體中文
                val result = textToSpeech?.setLanguage(Locale.TRADITIONAL_CHINESE)

                isTtsReady = when (result) {
                    TextToSpeech.LANG_MISSING_DATA,
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        // 如果繁體中文不支援，嘗試簡體中文
                        textToSpeech?.setLanguage(Locale.CHINESE)
                        true
                    }
                    else -> true
                }

                if (isTtsReady) {
                    // 設定語速和音調
                    textToSpeech?.setSpeechRate(0.9f)  // 稍慢，便於理解
                    textToSpeech?.setPitch(1.0f)       // 正常音調

                    // 設定播放狀態監聽
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            // 開始播放
                            requireActivity().runOnUiThread {
                                binding.btnPlayAudio.text = "⏸️ 停止"
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            // 播放完成
                            requireActivity().runOnUiThread {
                                binding.btnPlayAudio.text = "🔊 播放"
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            // 播放錯誤
                            requireActivity().runOnUiThread {
                                binding.btnPlayAudio.text = "🔊 播放"
                            }
                        }
                    })
                }
            } else {
                isTtsReady = false
                Toast.makeText(
                    requireContext(),
                    "語音功能初始化失敗",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 觀察資料變化
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentStepIndex.collect { index ->
                    // 切換步驟時停止語音
                    stopSpeaking()
                    updateUI()
                }
            }
        }
    }

    /**
     * 設定按鈕點擊
     */
    private fun setupClickListeners() {
        // 上一步
        binding.btnPrevious.setOnClickListener {
            viewModel.previousStep()
        }

        // 下一步/完成
        binding.btnNext.setOnClickListener {
            if (viewModel.isLastStep()) {
                stopSpeaking()
                findNavController().navigateUp()
            } else {
                viewModel.nextStep()
            }
        }

        // 播放/停止語音
        binding.btnPlayAudio.setOnClickListener {
            if (!isTtsReady) {
                Toast.makeText(
                    requireContext(),
                    "語音功能尚未就緒",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (textToSpeech?.isSpeaking == true) {
                // 正在播放，停止
                stopSpeaking()
            } else {
                // 開始播放
                val currentStep = viewModel.getCurrentStep()
                speak(currentStep.audioText)
            }
        }
    }

    /**
     * 播放語音
     */
    private fun speak(text: String) {
        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "TUTORIAL_${System.currentTimeMillis()}"
        )
    }

    /**
     * 停止播放
     */
    private fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        binding.btnPlayAudio.text = "🔊 播放"
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

        // 👇 更新步驟編號徽章
        binding.tvStepBadge.text = currentStep.stepNumber.toString()

        // 更新按鈕狀態
        val hasPrevious = viewModel.hasPreviousStep()
        binding.btnPrevious.isEnabled = hasPrevious
        binding.btnPrevious.alpha = if (hasPrevious) 1.0f else 0.5f

        binding.btnNext.text = if (viewModel.isLastStep()) {
            "完成"
        } else {
            "下一步"
        }
    }

    /**
     * Fragment 暫停時停止語音
     */
    override fun onPause() {
        super.onPause()
        stopSpeaking()
    }

    /**
     * 釋放資源
     */
    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _binding = null
    }

    companion object {
        fun newInstance() = PackingTutorialFragment()
    }
}