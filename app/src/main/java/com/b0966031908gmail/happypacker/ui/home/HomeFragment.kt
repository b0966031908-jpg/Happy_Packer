package com.b0966031908gmail.happypacker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.b0966031908gmail.happypacker.R
import com.b0966031908gmail.happypacker.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }
    private fun setupClickListeners() {
        binding.cardCanvas.setOnClickListener {
            viewModel.onButtonClick(HomeButton.CANVAS)
        }

        binding.cardPacking.setOnClickListener {
            viewModel.onButtonClick(HomeButton.PACKING)
        }

        binding.cardSelling.setOnClickListener {
            viewModel.onButtonClick(HomeButton.SELLING)
        }
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HomeUiState.Idle -> {
                        // 閒置
                    }
                    is HomeUiState.NavigateTo -> {
                        handleNavigation(state.button)
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun handleNavigation(button: HomeButton) {
        val action = when (button) {
            HomeButton.CANVAS -> R.id.action_home_to_canvas
            HomeButton.PACKING -> R.id.action_home_to_packing
            HomeButton.SELLING -> R.id.action_home_to_selling
        }
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}