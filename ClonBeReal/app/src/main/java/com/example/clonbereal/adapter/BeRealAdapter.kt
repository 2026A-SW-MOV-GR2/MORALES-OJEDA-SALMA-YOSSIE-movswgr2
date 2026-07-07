package com.example.clonbereal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clonbereal.databinding.ItemBerealPostBinding
import com.example.clonbereal.data.BeRealPost
import com.example.clonbereal.data.PostRepository

class BeRealAdapter(private val repository: PostRepository) : ListAdapter<BeRealPost, BeRealAdapter.BeRealViewHolder>(BeRealDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeRealViewHolder {
        val binding = ItemBerealPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BeRealViewHolder(binding, repository)
    }

    override fun onBindViewHolder(holder: BeRealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BeRealViewHolder(
        private val binding: ItemBerealPostBinding,
        private val repository: PostRepository
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: BeRealPost) {
            binding.imgUserAvatar.setImageResource(post.userAvatarResId)
            binding.tvUsername.text = post.username
            binding.tvTime.text = post.timePosted

            binding.imgMain.setImageBitmap(repository.loadBitmap(post.mainImagePath))
            binding.imgSelfie.setImageBitmap(repository.loadBitmap(post.selfieImagePath))

            binding.tvCommentCount.text =
                if (post.commentCount == 0) "Añade un comentario…"
                else "Ver los ${post.commentCount} comentarios…"

            binding.imgCommentAvatar.setImageResource(post.userAvatarResId)
        }
    }

    class BeRealDiffCallback : DiffUtil.ItemCallback<BeRealPost>() {
        override fun areItemsTheSame(oldItem: BeRealPost, newItem: BeRealPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BeRealPost, newItem: BeRealPost) = oldItem == newItem
    }
}
