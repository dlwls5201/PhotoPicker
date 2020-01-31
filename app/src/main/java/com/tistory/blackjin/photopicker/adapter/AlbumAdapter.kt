package com.tistory.blackjin.photopicker.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.tistory.blackjin.photopicker.R
import com.tistory.blackjin.photopicker.base.BaseViewHolder
import com.tistory.blackjin.photopicker.databinding.ItemAlbumBinding
import com.tistory.blackjin.photopicker.model.Album

class AlbumAdapter : RecyclerView.Adapter<BaseViewHolder<ViewDataBinding, Album>>() {

    private val items = mutableListOf<Album>()

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(data: Album)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding, Album> {
        return AlbumViewHolder(parent).apply {
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

    override fun onBindViewHolder(holder: BaseViewHolder<ViewDataBinding, Album>, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewDataBinding, Album>) {
        holder.recycled()
        super.onViewRecycled(holder)
    }

    fun replaceAll(items: List<Album>) {
        this.items.run {
            clear()
            addAll(items)
        }
        notifyDataSetChanged()
    }

    class AlbumViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemAlbumBinding, Album>(parent, R.layout.item_album) {

        override fun bind(data: Album) {
            binding.album = data
        }

        override fun recycled() { }
    }
}