package com.b0966031908gmail.happypacker.ui.selling

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.b0966031908gmail.happypacker.data.model.GameLevel
import com.b0966031908gmail.happypacker.data.model.Sock
import com.b0966031908gmail.happypacker.databinding.FragmentSellingGameBinding
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 販售遊戲 Fragment
 */
class SellingGameFragment : Fragment() {

    private var _binding: FragmentSellingGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SellingGameViewModel by viewModels()

    // 錢幣數量變數
    private var coin50Count = 0
    private var coin10Count = 0
    private var coin5Count = 0
    private var coin1Count = 0

    // 語音播放
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val handler = Handler(Looper.getMainLooper())

    // 襪子適配器
    private lateinit var sockAdapter: SockAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellingGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTTS()
        setupSockRecyclerView()
        setupClickListeners()
        observeViewModel()

        // 從參數接收關卡編號
        val levelNumber = arguments?.getInt("levelNumber", 1) ?: 1
        viewModel.startLevel(levelNumber)
    }

    private fun initTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.TRADITIONAL_CHINESE)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.9f)
            }
        }
    }

    private fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GAME_${System.currentTimeMillis()}")
        }
    }

    private fun setupSockRecyclerView() {
        sockAdapter = SockAdapter(
            socks = Sock.getAllSocks(),
            onSockClick = { sock ->
                viewModel.selectSock(sock)
            }
        )

        binding.recyclerViewSocks.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sockAdapter
        }
    }

    private fun setupClickListeners() {
        // 答案選項按鈕
        binding.btnOption1.setOnClickListener {
            val answer = binding.btnOption1.text.toString().replace("元", "").toIntOrNull() ?: 0
            viewModel.answerChange(answer)
        }

        binding.btnOption2.setOnClickListener {
            val answer = binding.btnOption2.text.toString().replace("元", "").toIntOrNull() ?: 0
            viewModel.answerChange(answer)
        }

        binding.btnOption3.setOnClickListener {
            val answer = binding.btnOption3.text.toString().replace("元", "").toIntOrNull() ?: 0
            viewModel.answerChange(answer)
        }

        // 輸入答案按鈕
        binding.btnSubmitAnswer.setOnClickListener {
            val input = binding.etAnswer.text.toString()
            if (input.isNotEmpty()) {
                viewModel.answerChangeByInput(input)
                binding.etAnswer.text?.clear()
            } else {
                Toast.makeText(requireContext(), "請輸入金額", Toast.LENGTH_SHORT).show()
            }
        }

        // 下一題按鈕
        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }

        // 返回按鈕
        binding.btnBack.setOnClickListener {
            viewModel.backToLevelSelect()
            findNavController().navigateUp()
        }

        setupCoinButtons()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 觀察遊戲狀態
                launch {
                    viewModel.gameState.collect { state ->
                        handleGameState(state)
                    }
                }

                // 觀察當前客人
                launch {
                    viewModel.currentCustomer.collect { customer ->
                        customer?.let {
                            binding.tvCustomerEmoji.text = it.emoji
                            binding.tvCustomerName.text = it.name
                            binding.tvCustomerSpeech.text = it.getSpeech()
                        }
                    }
                }

                // 觀察關卡
                launch {
                    viewModel.currentLevel.collect { level ->
                        level?.let {
                            binding.tvLevel.text = "第 ${it.levelNumber} 關"
                        }
                    }
                }

                // 觀察分數
                launch {
                    viewModel.totalScore.collect { score ->
                        binding.tvScore.text = "得分: $score"
                    }
                }

                // 觀察剩餘時間
                launch {
                    viewModel.timeRemaining.collect { time ->
                        binding.tvTimer.text = "⏱️ $time"
                        val color = if (time <= 10) {
                            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                        } else {
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        }
                        binding.tvTimer.setTextColor(color)
                    }
                }

                // 觀察當前問題
                launch {
                    viewModel.currentQuestion.collect { question ->
                        question?.let { updateQuestionInfo(it) }
                    }
                }
            }
        }
    }

    private fun updateQuestionInfo(question: GameLevel.Question) {
        val customer = viewModel.currentCustomer.value ?: return

        val calculation = "${customer.quantity} 雙 × ${question.sockPrice}元 = ${question.totalPrice}元"
        binding.tvCalculation.text = calculation
        binding.tvCalculationInput.text = calculation

        binding.tvPaymentInfo.text = "客人給你：${question.paymentAmount}元"
        binding.tvPaymentInfoInput.text = "客人給你：${question.paymentAmount}元"
    }

    private fun handleGameState(state: SellingGameViewModel.GameState) {
        when (state) {
            SellingGameViewModel.GameState.CUSTOMER_SPEAKS -> showCustomerSpeaking()
            SellingGameViewModel.GameState.SELECT_SOCK -> showSockSelection()
            SellingGameViewModel.GameState.CALCULATE_CHANGE -> showChangeQuestion()
            SellingGameViewModel.GameState.CORRECT_ANSWER -> showCorrectAnswer()
            SellingGameViewModel.GameState.WRONG_ANSWER -> showWrongAnswer()
            SellingGameViewModel.GameState.TIME_UP -> showTimeUp()
            SellingGameViewModel.GameState.LEVEL_COMPLETE -> showLevelComplete()
            else -> {}
        }
    }

    private fun showCustomerSpeaking() {
        val customer = viewModel.currentCustomer.value ?: return
        binding.changeQuestionArea.visibility = View.GONE
        binding.coinAssemblyArea.visibility = View.GONE
        binding.inputAnswerArea.visibility = View.GONE
        binding.buttonArea.visibility = View.GONE
        binding.sockSelectionArea.visibility = View.VISIBLE

        speak("歡迎光臨！${customer.getSpeech()}")
        handler.postDelayed({ viewModel.customerFinishedSpeaking() }, 2000)
    }

    private fun showSockSelection() {
        binding.sockSelectionArea.visibility = View.VISIBLE
        binding.changeQuestionArea.visibility = View.GONE
        binding.coinAssemblyArea.visibility = View.GONE
        binding.inputAnswerArea.visibility = View.GONE
        binding.buttonArea.visibility = View.GONE
    }

    private fun showChangeQuestion() {
        val level = viewModel.currentLevel.value ?: return
        val question = viewModel.currentQuestion.value ?: return
        val customer = viewModel.currentCustomer.value ?: return

        binding.sockSelectionArea.visibility = View.GONE
        binding.buttonArea.visibility = View.GONE

        when (level.difficulty) {
            GameLevel.Difficulty.EASY -> {
                binding.changeQuestionArea.visibility = View.VISIBLE
                binding.coinAssemblyArea.visibility = View.GONE
                binding.inputAnswerArea.visibility = View.GONE
                val options = viewModel.getChangeOptions()
                binding.btnOption1.text = "${options[0]}元"
                binding.btnOption2.text = "${options[1]}元"
                binding.btnOption3.text = "${options[2]}元"
                speak("${customer.quantity}雙襪子${question.totalPrice}元，客人給你${question.paymentAmount}元，要找多少錢呢？")
            }
            GameLevel.Difficulty.MEDIUM -> {
                binding.changeQuestionArea.visibility = View.GONE
                binding.coinAssemblyArea.visibility = View.VISIBLE
                binding.inputAnswerArea.visibility = View.GONE
                resetCoinCounts()
                updateCoinQuestionInfo()
                speak("${customer.quantity}雙襪子${question.totalPrice}元，客人給你${question.paymentAmount}元，請用錢幣拼出正確的找零金額")
            }
            GameLevel.Difficulty.HARD -> {
                binding.changeQuestionArea.visibility = View.GONE
                binding.coinAssemblyArea.visibility = View.GONE
                binding.inputAnswerArea.visibility = View.VISIBLE
                binding.etAnswer.text?.clear()
                speak("${customer.quantity}雙襪子${question.totalPrice}元，客人給你${question.paymentAmount}元，請輸入金額")
            }
        }
    }

    private fun showCorrectAnswer() {
        val timeBonus = viewModel.timeRemaining.value
        Toast.makeText(requireContext(), "答對了！+${10 + timeBonus}分 🎉", Toast.LENGTH_SHORT).show()
        speak("答對了！太棒了！")
        binding.buttonArea.visibility = View.VISIBLE
        binding.btnNext.text = "下一題"
        binding.changeQuestionArea.visibility = View.GONE
        binding.coinAssemblyArea.visibility = View.GONE
        binding.inputAnswerArea.visibility = View.GONE
    }

    private fun showWrongAnswer() {
        Toast.makeText(requireContext(), "再試試看！💪", Toast.LENGTH_SHORT).show()
        speak("再試試看")
        handler.postDelayed({ viewModel.retry() }, 1000)
    }

    private fun showTimeUp() {
        Toast.makeText(requireContext(), "時間到！⏰", Toast.LENGTH_SHORT).show()
        speak("時間到了")
        binding.changeQuestionArea.visibility = View.GONE
        binding.coinAssemblyArea.visibility = View.GONE
        binding.inputAnswerArea.visibility = View.GONE
        binding.buttonArea.visibility = View.VISIBLE
        binding.btnNext.text = "重試"
    }

    private fun showLevelComplete() {
        val score = viewModel.totalScore.value
        Toast.makeText(requireContext(), "關卡完成！得分：$score 🏆", Toast.LENGTH_LONG).show()
        speak("恭喜你完成這一關！")
        binding.buttonArea.visibility = View.VISIBLE
        binding.btnNext.visibility = View.GONE
        binding.btnBack.text = "回到關卡選擇"
        binding.sockSelectionArea.visibility = View.GONE
    }

    // ========== 拼錢幣相關 ==========

    private fun setupCoinButtons() {
        binding.btnCoin50Plus.setOnClickListener { coin50Count++; updateCoinDisplay() }
        binding.btnCoin50Minus.setOnClickListener { if (coin50Count > 0) { coin50Count--; updateCoinDisplay() } }
        binding.btnCoin10Plus.setOnClickListener { coin10Count++; updateCoinDisplay() }
        binding.btnCoin10Minus.setOnClickListener { if (coin10Count > 0) { coin10Count--; updateCoinDisplay() } }
        binding.btnCoin5Plus.setOnClickListener { coin5Count++; updateCoinDisplay() }
        binding.btnCoin5Minus.setOnClickListener { if (coin5Count > 0) { coin5Count--; updateCoinDisplay() } }
        binding.btnCoin1Plus.setOnClickListener { coin1Count++; updateCoinDisplay() }
        binding.btnCoin1Minus.setOnClickListener { if (coin1Count > 0) { coin1Count--; updateCoinDisplay() } }
        binding.btnSubmitCoins.setOnClickListener { submitCoinAnswer() }
    }

    private fun updateCoinDisplay() {
        binding.tvCoin50Count.text = "× $coin50Count"
        binding.tvCoin10Count.text = "× $coin10Count"
        binding.tvCoin5Count.text = "× $coin5Count"
        binding.tvCoin1Count.text = "× $coin1Count"
        val total = coin50Count * 50 + coin10Count * 10 + coin5Count * 5 + coin1Count * 1
        binding.tvCoinTotal.text = "總計：${total}元"
    }

    private fun resetCoinCounts() {
        coin50Count = 0; coin10Count = 0; coin5Count = 0; coin1Count = 0
        updateCoinDisplay()
    }

    private fun submitCoinAnswer() {
        val total = coin50Count * 50 + coin10Count * 10 + coin5Count * 5 + coin1Count * 1
        val question = viewModel.currentQuestion.value ?: return
        if (total == question.correctChange) {
            viewModel.answerChange(total)
        } else {
            Toast.makeText(requireContext(), "金額不對哦！再試試看 💪", Toast.LENGTH_SHORT).show()
            speak("金額不對，再試試看")
        }
    }

    private fun updateCoinQuestionInfo() {
        val question = viewModel.currentQuestion.value ?: return
        val customer = viewModel.currentCustomer.value ?: return
        val calculation = "${customer.quantity} 雙 × ${question.sockPrice}元 = ${question.totalPrice}元"
        binding.tvCalculationCoin.text = calculation
        binding.tvPaymentInfoCoin.text = "客人給你：${question.paymentAmount}元"
    }

    override fun onPause() { super.onPause(); tts?.stop(); viewModel.stopTimer() }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.stop(); tts?.shutdown()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}