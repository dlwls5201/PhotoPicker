package com.tistory.blackjin.photopicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        intent?.run {
            val uri = getParcelableExtra<Uri>(EXTRA_URI)
            if(uri != null) {
                setImageUri(uri)
                initButton()
            }
        }
    }

    private fun setImageUri(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(ivPreview)
    }

    private fun initButton() {
        btnOverlay.setOnClickListener {

        }
        btnExport.setOnClickListener {

        }
    }

    companion object {

        const val EXTRA_URI = "uri"

        fun startPreviewActivity(context: Context, uri: Uri) {
            context.startActivity(
                Intent(context, PreviewActivity::class.java).apply {
                    putExtra(EXTRA_URI, uri)
                }
            )
        }
    }
}