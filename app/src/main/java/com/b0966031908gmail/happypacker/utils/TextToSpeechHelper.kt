package com.b0966031908gmail.happypacker.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * 文字轉語音工具類
 * 用於播放教學步驟的語音說明
 */
class TextToSpeechHelper(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    // 語音播放狀態監聽
    private var onSpeakingStarted: (() -> Unit)? = null
    private var onSpeakingDone: (() -> Unit)? = null

    /**
     * 初始化 TTS
     */
    fun initialize(onInitialized: (Boolean) -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // 設定語言為繁體中文（台灣）
                val result = textToSpeech?.setLanguage(Locale.TRADITIONAL_CHINESE)

                isInitialized = when (result) {
                    TextToSpeech.LANG_MISSING_DATA,
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        Log.e("TTS", "繁體中文不支援，嘗試使用簡體中文")
                        // 如果繁體中文不支援，嘗試簡體中文
                        textToSpeech?.setLanguage(Locale.CHINESE)
                        true
                    }
                    else -> true
                }

                // 設定語音參數
                textToSpeech?.setSpeechRate(0.9f)  // 語速（0.5 - 2.0，1.0 是正常速度）
                textToSpeech?.setPitch(1.0f)        // 音調（0.5 - 2.0，1.0 是正常音調）

                // 設定監聽器
                setupUtteranceListener()

                onInitialized(isInitialized)
            } else {
                Log.e("TTS", "TTS 初始化失敗")
                isInitialized = false
                onInitialized(false)
            }
        }
    }

    /**
     * 設定語音播放進度監聽器
     */
    private fun setupUtteranceListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // 開始播放
                onSpeakingStarted?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                // 播放完成
                onSpeakingDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                // 播放錯誤
                Log.e("TTS", "語音播放錯誤: $utteranceId")
                onSpeakingDone?.invoke()
            }
        })
    }

    /**
     * 播放文字
     * @param text 要播放的文字
     */
    fun speak(text: String) {
        if (!isInitialized) {
            Log.e("TTS", "TTS 尚未初始化")
            return
        }

        // 停止當前播放（如果有）
        stop()

        // 開始播放新的文字
        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,  // 清除佇列，立即播放
            null,
            "TUTORIAL_SPEECH"  // utteranceId
        )
    }

    /**
     * 停止播放
     */
    fun stop() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
    }

    /**
     * 檢查是否正在播放
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    /**
     * 設定語速
     * @param rate 語速（0.5 - 2.0）
     */
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }

    /**
     * 設定音調
     * @param pitch 音調（0.5 - 2.0）
     */
    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }

    /**
     * 設定播放開始監聽
     */
    fun setOnSpeakingStarted(callback: () -> Unit) {
        onSpeakingStarted = callback
    }

    /**
     * 設定播放完成監聽
     */
    fun setOnSpeakingDone(callback: () -> Unit) {
        onSpeakingDone = callback
    }

    /**
     * 釋放資源
     */
    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}