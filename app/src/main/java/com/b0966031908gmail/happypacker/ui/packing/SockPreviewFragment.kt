package com.b0966031908gmail.happypacker.ui.packing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.R
import com.b0966031908gmail.happypacker.databinding.FragmentSockPreviewBinding
import com.b0966031908gmail.happypacker.utils.FileHelper
import java.util.Locale

class SockPreviewFragment : Fragment() {

    private var _binding: FragmentSockPreviewBinding? = null
    private val binding get() = _binding!!

    private var currentScale = 1.0f
    private var currentX = 0f
    private var currentY = 0f

    private val scaleStep = 0.15f
    private val moveStep = 25f

    private var filePath: String? = null

    // TTS 語音
    private var tts: TextToSpeech? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSockPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 接收檔案路徑
        filePath = arguments?.getString("filePath")

        initTTS()
        loadArtwork()
        setupControls()

        // 語音提示
        handler.postDelayed({
            speak("調整圖案的位置和大小，然後按確認！")
        }, 500)
    }

    /**
     * 初始化 TTS
     */
    private fun initTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.TAIWAN
                tts?.setSpeechRate(0.85f)
            }
        }
    }

    /**
     * 播放語音
     */
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }

    /**
     * 顯示短訊息提示
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun loadArtwork() {
        filePath?.let { path ->
            val bitmap = FileHelper.loadArtwork(path)
            if (bitmap != null) {
                binding.ivArtwork.setImageBitmap(bitmap)
            } else {
                showToast("載入失敗")
                findNavController().navigateUp()
            }
        } ?: run {
            showToast("沒有選擇圖案")
            findNavController().navigateUp()
        }
    }

    private fun setupControls() {
        // 放大
        binding.btnZoomIn.setOnClickListener {
            if (currentScale < 3.0f) {
                currentScale += scaleStep
                updateArtwork()
                showToast("放大 ➕")
            } else {
                showToast("已經最大了！")
            }
        }

        // 縮小
        binding.btnZoomOut.setOnClickListener {
            if (currentScale > 0.3f) {
                currentScale -= scaleStep
                updateArtwork()
                showToast("縮小 ➖")
            } else {
                showToast("已經最小了！")
            }
        }

        // 上移
        binding.btnMoveUp.setOnClickListener {
            currentY -= moveStep
            updateArtwork()
            showToast("往上 ▲")
        }

        // 下移
        binding.btnMoveDown.setOnClickListener {
            currentY += moveStep
            updateArtwork()
            showToast("往下 ▼")
        }

        // 左移
        binding.btnMoveLeft.setOnClickListener {
            currentX -= moveStep
            updateArtwork()
            showToast("往左 ◀")
        }

        // 右移
        binding.btnMoveRight.setOnClickListener {
            currentX += moveStep
            updateArtwork()
            showToast("往右 ▶")
        }

        // 重置
        binding.previewCard.setOnClickListener {
            currentScale = 1.0f
            currentX = 0f
            currentY = 0f
            updateArtwork()
            showToast("已重置 ⟲")
            speak("位置已重置")
        }

        // 取消
        binding.btnCancel.setOnClickListener {
            showToast("取消")
            findNavController().navigateUp()
        }

        // 確認 - 進入包裝教學
        binding.btnConfirm.setOnClickListener {
            showToast("確認！準備進入包裝教學 ✓")
            speak("太棒了！圖案確認了！現在來學習包裝襪子！")
            handler.postDelayed({
                // 導航到包裝教學頁面
                findNavController().navigate(R.id.action_sockPreview_to_packingTutorial)
            }, 2000)
        }
    }

    private fun updateArtwork() {
        binding.ivArtwork.scaleX = currentScale
        binding.ivArtwork.scaleY = currentScale
        binding.ivArtwork.translationX = currentX
        binding.ivArtwork.translationY = currentY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.stop()
        tts?.shutdown()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}