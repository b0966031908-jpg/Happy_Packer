package com.b0966031908gmail.happypacker.ui.canvas

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.databinding.FragmentCanvasBinding
import kotlinx.coroutines.launch

class CanvasFragment : Fragment() {

    private var _binding: FragmentCanvasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CanvasViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCanvasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupColorPicker()
        setupBrushSize()
        setupToolButtons()
        observeViewModel()
    }

    private fun setupColorPicker() {
        val colorMap = mapOf(
            binding.colorBlack to Pair(Color.BLACK, "黑色"),
            binding.colorRed to Pair(Color.RED, "紅色"),
            binding.colorBlue to Pair(Color.BLUE, "藍色"),
            binding.colorGreen to Pair(Color.GREEN, "綠色"),
            binding.colorYellow to Pair(Color.YELLOW, "黃色"),
            binding.colorOrange to Pair(Color.parseColor("#FF9800"), "橘色"),
            binding.colorPurple to Pair(Color.parseColor("#9C27B0"), "紫色"),
            binding.colorPink to Pair(Color.parseColor("#E91E63"), "粉紅色")
        )

        colorMap.forEach { (view, colorData) ->
            val (color, colorName) = colorData
            view.setOnClickListener {
                selectedColor = color
                binding.canvasView.setColor(color)

                resetColorScale()
                view.scaleX = 1.2f
                view.scaleY = 1.2f

                Toast.makeText(requireContext(), "選擇${colorName}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMoreColors.setOnClickListener {
            showColorPickerDialog()
        }
    }
    // 更多顏色選擇
    private fun showColorPickerDialog() {
        val colors = arrayOf(
            "#FF1744", "#F50057", "#D500F9", "#651FFF",
            "#3D5AFE", "#2979FF", "#00B0FF", "#00E5FF",
            "#1DE9B6", "#00E676", "#76FF03", "#C6FF00",
            "#FFEA00", "#FFC400", "#FF9100", "#FF3D00"
        )

        val colorNames = arrayOf(
            "深紅", "桃紅", "紫紅", "深紫",
            "深藍", "亮藍", "天藍", "青藍",
            "青綠", "亮綠", "黃綠", "檸檬",
            "亮黃", "金黃", "橙紅", "火紅"
        )

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("選擇顏色")
        builder.setItems(colorNames) { _, which ->
            val color = Color.parseColor(colors[which])
            selectedColor = color
            binding.canvasView.setColor(color)
            Toast.makeText(requireContext(), "選擇${colorNames[which]}", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun resetColorScale() {
        listOf(
            binding.colorBlack, binding.colorRed, binding.colorBlue,
            binding.colorGreen, binding.colorYellow, binding.colorOrange,
            binding.colorPurple, binding.colorPink
        ).forEach {
            it.scaleX = 1f
            it.scaleY = 1f
        }
    }

    private fun setupBrushSize() {
        binding.seekbarBrushSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val strokeWidth = (progress.coerceAtLeast(10)).toFloat()
                binding.canvasView.setStrokeWidth(strokeWidth)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val size = when {
                    seekBar?.progress ?: 0 < 30 -> "細"
                    seekBar?.progress ?: 0 < 60 -> "中"
                    else -> "粗"
                }
                Toast.makeText(requireContext(), "筆刷${size}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupToolButtons() {
        binding.btnPen.setOnClickListener {
            binding.canvasView.setPen()
            viewModel.setTool(DrawingTool.PEN)
            Toast.makeText(requireContext(), "畫筆模式", Toast.LENGTH_SHORT).show()

            binding.btnPen.isEnabled = false
            binding.btnEraser.isEnabled = true
        }

        binding.btnEraser.setOnClickListener {
            binding.canvasView.setEraser()
            viewModel.setTool(DrawingTool.ERASER)
            Toast.makeText(requireContext(), "橡皮擦模式", Toast.LENGTH_SHORT).show()

            binding.btnPen.isEnabled = true
            binding.btnEraser.isEnabled = false
        }

        // 新增油漆桶按鈕
        binding.btnFill.setOnClickListener {
            // 取得當前選擇的顏色（預設黑色）
            val currentColor = getCurrentSelectedColor()
            binding.canvasView.fillWithColor(currentColor)
            Toast.makeText(requireContext(), "背景填充", Toast.LENGTH_SHORT).show()
        }

        binding.btnClear.setOnClickListener {
            Toast.makeText(requireContext(), "清空畫布", Toast.LENGTH_SHORT).show()
            binding.canvasView.clearCanvas()
        }

        binding.btnSave.setOnClickListener {
            saveArtwork()
        }
    }

    private var selectedColor = Color.BLACK

    private fun getCurrentSelectedColor(): Int {
        return selectedColor
    }

    private fun saveArtwork() {
        val bitmap = binding.canvasView.getBitmap()
        if (bitmap != null) {
            showSaveDialog(bitmap)  // 應該呼叫這個
        } else {
            Toast.makeText(requireContext(), "無法取得畫作", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSaveDialog(bitmap: Bitmap) {
        val input = android.widget.EditText(requireContext())
        input.hint = "輸入檔案名稱"

        val defaultName = java.text.SimpleDateFormat("MMdd_HHmm", java.util.Locale.getDefault())
            .format(java.util.Date())
        input.setText("我的襪子_$defaultName")

        // 讓文字全選（方便修改）
        input.selectAll()

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("儲存作品")
        builder.setMessage("請輸入檔案名稱")
        builder.setView(input)

        builder.setPositiveButton("儲存") { _, _ ->
            val fileName = input.text.toString().trim()
            if (fileName.isNotEmpty()) {
                viewModel.saveArtworkWithName(requireContext(), bitmap, fileName)
            } else {
                Toast.makeText(requireContext(), "檔名不能空白", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("取消", null)
        builder.show()

        // 自動彈出鍵盤
        input.requestFocus()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CanvasUiState.Drawing -> {
                        // 繪圖中
                    }
                    is CanvasUiState.Saving -> {
                        Toast.makeText(requireContext(), "儲存中...", Toast.LENGTH_SHORT).show()
                    }
                    is CanvasUiState.SaveSuccess -> {
                        Toast.makeText(requireContext(), "儲存成功！", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                        viewModel.resetState()
                    }
                    is CanvasUiState.SaveError -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}