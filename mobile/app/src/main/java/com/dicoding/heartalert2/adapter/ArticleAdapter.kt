package com.dicoding.heartalert2.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.ArticlesItem
import com.dicoding.heartalert2.databinding.ItemArticleBinding


class ArticleAdapter(
    private var articles: List<ArticlesItem>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: ArticlesItem) {
            // Setting up the title, date, and description text
            binding.tvTitle.text = article.title

            // Load image using Glide into ImageView
            Glide.with(binding.root.context)
                .load(article.imageUrl)
                .placeholder(R.drawable.placeholder_image) // Placeholder saat loading
                .into(binding.articleImageView)

            // Handle click events inside the adapter itself
            itemView.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.referenceLink))
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = articles.size

    fun updateArticles(newArticles: List<ArticlesItem>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(articles, newArticles))
        articles = newArticles
        diffResult.dispatchUpdatesTo(this)
    }

    private class DiffUtilCallback(private val oldList: List<ArticlesItem>, private val newList: List<ArticlesItem>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}