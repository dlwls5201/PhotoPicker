package com.tistory.blackjin.photopicker.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        if (position >= 0) {
            val column = position % spanCount
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            outRect.top = spacing
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = 0
            outRect.bottom = 0
        }
    }
}
