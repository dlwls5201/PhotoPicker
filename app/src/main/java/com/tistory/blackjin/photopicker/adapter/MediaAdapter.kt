package com.tistory.blackjin.photopicker.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tistory.blackjin.photopicker.R
import com.tistory.blackjin.photopicker.base.BaseDiffUtilCallback
import com.tistory.blackjin.photopicker.base.BaseViewHolder
import com.tistory.blackjin.photopicker.databinding.ItemGalleryBinding
import com.tistory.blackjin.photopicker.model.Media

internal class MediaAdapter : RecyclerView.Adapter<BaseViewHolder<ViewDataBinding, Media>>() {

    private val items = mutableListOf<Media>()

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(data: Media)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding, Media> {
        return ImageViewHolder(parent).apply {
            onItemClickListener?.let { listener ->
                itemView.setOnClickListener {
                    listener.onItemClick(
                        getItem(adapterPosition)
                    )
                }
            }
        }
    }

    private fun getItem(position: Int) = items[getItemPosition(position)]

    private fun getItemPosition(adapterPosition: Int) = adapterPosition

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<ViewDataBinding, Media>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewDataBinding, Media>) {
        holder.recycled()
        super.onViewRecycled(holder)
    }

    fun replaceAll(items: List<Media>, useDiffCallback: Boolean = false) {
        val diffCallback = BaseDiffUtilCallback(this.items, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items.run {
            clear()
            addAll(items)
        }
        if (useDiffCallback) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    inner class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemGalleryBinding, Media>(parent, R.layout.item_gallery) {

        override fun bind(data: Media) {
            binding.run {
                media = data
            }
        }

        override fun recycled() {
            Glide.with(itemView).clear(binding.ivImage)
        }
    }
}