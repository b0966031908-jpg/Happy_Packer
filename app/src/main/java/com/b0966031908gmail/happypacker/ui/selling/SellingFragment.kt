package com.b0966031908gmail.happypacker.ui.selling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.R
import com.b0966031908gmail.happypacker.databinding.FragmentSellingBinding

/**
 * 販售主頁 Fragment
 */
class SellingFragment : Fragment() {

    private var _binding: FragmentSellingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 👇 改為導航到關卡選擇頁面
        binding.btnStartGame.setOnClickListener {
            findNavController().navigate(
                R.id.action_sellingFragment_to_levelSelectFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}