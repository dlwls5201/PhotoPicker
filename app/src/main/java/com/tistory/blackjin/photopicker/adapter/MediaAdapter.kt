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
import com.tistory.blackjin.photopicker.model.Gallery

internal class MediaAdapter : RecyclerView.Adapter<BaseViewHolder<ViewDataBinding, Gallery>>() {

    private val items = mutableListOf<Gallery>()

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(data: Gallery)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding, Gallery> {
        return ImageViewHolder(parent).apply {
            onItemClickListener?.let { listener ->
                itemView.setOnClickListener {
                    listener.onItemClick(
                        items[adapterPosition]
                    )
                }
            }
        }
    }


    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<ViewDataBinding, Gallery>, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewDataBinding, Gallery>) {
        holder.recycled()
        super.onViewRecycled(holder)
    }

    fun replaceAll(items: List<Gallery>, useDiffCallback: Boolean = false) {
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

    class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemGalleryBinding, Gallery>(parent, R.layout.item_gallery) {

        override fun bind(data: Gallery) {
            binding.gallery = data
        }

        override fun recycled() {
            Glide.with(itemView).clear(binding.ivImage)
        }
    }
}