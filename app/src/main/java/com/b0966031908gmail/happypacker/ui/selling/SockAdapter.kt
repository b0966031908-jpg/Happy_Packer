package com.b0966031908gmail.happypacker.ui.selling

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.b0966031908gmail.happypacker.data.model.Sock
import com.b0966031908gmail.happypacker.databinding.ItemSockCardBinding

/**
 * 襪子卡片適配器
 */
class SockAdapter(
    private val socks: List<Sock>,
    private val onSockClick: (Sock) -> Unit
) : RecyclerView.Adapter<SockAdapter.SockViewHolder>() {

    inner class SockViewHolder(
        private val binding: ItemSockCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sock: Sock) {
            // 設定背景顏色
            binding.sockCard.setCardBackgroundColor(Color.parseColor(sock.colorCode))

            // 設定文字
            binding.tvSockColor.text = "${sock.colorName}襪子"
            binding.tvSockPrice.text = "${sock.price}元"

            // 點擊事件
            binding.sockCard.setOnClickListener {
                onSockClick(sock)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SockViewHolder {
        val binding = ItemSockCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SockViewHolder, position: Int) {
        holder.bind(socks[position])
    }

    override fun getItemCount(): Int = socks.size
}