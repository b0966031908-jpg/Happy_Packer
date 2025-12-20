package com.b0966031908gmail.happypacker.ui.packing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.b0966031908gmail.happypacker.databinding.ItemArtworkBinding
import com.b0966031908gmail.happypacker.utils.FileHelper
import java.io.File

/**
 * 作品列表 Adapter
 * 支援點擊和長按刪除
 */
class ArtworkAdapter(
    private var artworks: List<File>,
    private val onArtworkClick: (File) -> Unit,
    private val onArtworkLongClick: (File) -> Unit  // 👈 新增長按回調
) : RecyclerView.Adapter<ArtworkAdapter.ArtworkViewHolder>() {

    inner class ArtworkViewHolder(
        private val binding: ItemArtworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            // 載入圖片
            val bitmap = FileHelper.loadArtwork(file.absolutePath)
            bitmap?.let {
                binding.ivArtwork.setImageBitmap(it)
            }

            // 顯示檔名（去掉副檔名）
            binding.tvFileName.text = file.nameWithoutExtension

            // 👇 點擊事件：進入套版頁面
            binding.root.setOnClickListener {
                onArtworkClick(file)
            }

            // 👇 長按事件：刪除作品
            binding.root.setOnLongClickListener {
                onArtworkLongClick(file)
                true  // 返回 true 表示事件已處理
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val binding = ItemArtworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        holder.bind(artworks[position])
    }

    override fun getItemCount(): Int = artworks.size

    /**
     * 更新作品列表
     */
    fun updateArtworks(newArtworks: List<File>) {
        artworks = newArtworks
        notifyDataSetChanged()
    }
}