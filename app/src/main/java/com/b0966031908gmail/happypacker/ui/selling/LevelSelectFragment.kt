package com.b0966031908gmail.happypacker.ui.selling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.R
import com.b0966031908gmail.happypacker.databinding.FragmentLevelSelectBinding

/**
 * 關卡選擇 Fragment
 */
class LevelSelectFragment : Fragment() {

    private var _binding: FragmentLevelSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLevelButtons()
    }

    private fun setupLevelButtons() {
        // 第 1-9 關按鈕
        binding.btnLevel1.setOnClickListener { startLevel(1) }
        binding.btnLevel2.setOnClickListener { startLevel(2) }
        binding.btnLevel3.setOnClickListener { startLevel(3) }
        binding.btnLevel4.setOnClickListener { startLevel(4) }
        binding.btnLevel5.setOnClickListener { startLevel(5) }
        binding.btnLevel6.setOnClickListener { startLevel(6) }
        binding.btnLevel7.setOnClickListener { startLevel(7) }
        binding.btnLevel8.setOnClickListener { startLevel(8) }
        binding.btnLevel9.setOnClickListener { startLevel(9) }
    }

    private fun startLevel(levelNumber: Int) {
        val bundle = bundleOf("levelNumber" to levelNumber)
        findNavController().navigate(
            R.id.action_levelSelectFragment_to_sellingGameFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}