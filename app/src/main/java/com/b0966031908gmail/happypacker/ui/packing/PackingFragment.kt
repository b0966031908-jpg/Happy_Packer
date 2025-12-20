package com.b0966031908gmail.happypacker.ui.packing

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.b0966031908gmail.happypacker.R
import com.b0966031908gmail.happypacker.databinding.FragmentPackingBinding
import com.b0966031908gmail.happypacker.utils.FileHelper
import java.io.File

/**
 * 包裝頁面
 * 功能：
 * 1. 顯示作品列表
 * 2. 點擊作品 → 進入套版頁面
 * 3. 長按作品 → 刪除作品
 * 4. 開始包裝教學
 */
class PackingFragment : Fragment() {

    private var _binding: FragmentPackingBinding? = null
    private val binding get() = _binding!!

    private lateinit var artworkAdapter: ArtworkAdapter
    private var artworksList = mutableListOf<File>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTutorialButton()
        loadArtworks()
    }

    /**
     * 設定 RecyclerView
     */
    private fun setupRecyclerView() {
        artworkAdapter = ArtworkAdapter(
            artworks = emptyList(),
            onArtworkClick = { file ->
                // 👈 點擊作品 → 進入套版頁面
                navigateToSockPreview(file)
            },
            onArtworkLongClick = { file ->
                // 👈 長按作品 → 顯示刪除確認對話框
                showDeleteDialog(file)
            }
        )

        binding.recyclerViewArtworks.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 列網格
            adapter = artworkAdapter
        }
    }

    /**
     * 設定開始教學按鈕
     */
    private fun setupTutorialButton() {
        binding.btnStartTutorial.setOnClickListener {
            // 直接進入包裝教學（不需要選作品）
            findNavController().navigate(
                R.id.action_packingFragment_to_packingTutorialFragment
            )
        }
    }

    /**
     * 載入作品列表
     */
    private fun loadArtworks() {
        artworksList = FileHelper.getAllArtworks(requireContext()).toMutableList()

        if (artworksList.isEmpty()) {
            // 沒有作品，顯示空狀態
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewArtworks.visibility = View.GONE
        } else {
            // 有作品，顯示列表
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewArtworks.visibility = View.VISIBLE
            artworkAdapter.updateArtworks(artworksList)
        }
    }

    /**
     * 導航到襪子套版頁面
     */
    private fun navigateToSockPreview(file: File) {
        val bundle = Bundle().apply {
            putString("filePath", file.absolutePath)  // 👈 使用 "filePath" 參數名
        }

        findNavController().navigate(
            R.id.action_packingFragment_to_sockPreviewFragment,
            bundle
        )
    }

    /**
     * 顯示刪除確認對話框
     */
    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(requireContext())
            .setTitle("刪除作品")
            .setMessage("確定要刪除「${file.nameWithoutExtension}」嗎？")
            .setPositiveButton("刪除") { _, _ ->
                deleteArtwork(file)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 刪除作品
     */
    private fun deleteArtwork(file: File) {
        val success = FileHelper.deleteArtwork(file.absolutePath)

        if (success) {
            Toast.makeText(
                requireContext(),
                "已刪除「${file.nameWithoutExtension}」",
                Toast.LENGTH_SHORT
            ).show()

            // 重新載入作品列表
            loadArtworks()
        } else {
            Toast.makeText(
                requireContext(),
                "刪除失敗",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Fragment 恢復時重新載入作品
     */
    override fun onResume() {
        super.onResume()
        loadArtworks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}