package com.tistory.blackjin.photopicker.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tistory.blackjin.photopicker.R
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_preview.*
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        intent?.run {
            val uri = getParcelableExtra<Uri>(EXTRA_URI)
            if (uri != null) {
                setImageUri(uri)
                initButton()
            }
        }
    }

    private fun setImageUri(uri: Uri) {
        val bitmap = getBitmapFromUri(uri)
        if (bitmap != null) {
            ivPreview.setImageBitmap(bitmap)
        }
    }

    private fun getBitmapFromUri(uri: Uri) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun initButton() {
        btnOverlay.setOnClickListener {
            overlayAndSetPreview()
            btnOverlay.isEnabled = false
        }

        btnExport.setOnClickListener {
            exportPreview()
        }
    }

    private fun overlayAndSetPreview() {
        (ivPreview.drawable as BitmapDrawable).bitmap.let { previewBitmap ->

            val copyPreviewBitmap = previewBitmap.copy(Bitmap.Config.ARGB_8888, true);
            val overlayBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.overlay
            )

            val left = (copyPreviewBitmap.width - overlayBitmap.width) / 2f
            val top = (copyPreviewBitmap.height - overlayBitmap.height) / 2f

            val canvas = Canvas(copyPreviewBitmap)
            canvas.drawBitmap(overlayBitmap, left, top, null)

            ivPreview.setImageBitmap(copyPreviewBitmap)
        }
    }

    /**
     * BlackJin 폴더를 생성해 이미지를 저장합니다.
     * Android Q 부터는 Scoped Storage 적용되기 때문에 분기 처리합니다.
     */
    private fun exportPreview() {
        (ivPreview.drawable as BitmapDrawable).bitmap.let { previewBitmap ->

            val fileName = "BlackJin-${SystemClock.currentThreadTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                exportFile {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/BlackJin")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val item = contentResolver.insert(collection, values)

                    if (item != null) {
                        contentResolver.openFileDescriptor(item, "w", null).use {
                            FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                                previewBitmap.compress(CompressFormat.PNG, 100, outputStream)
                                outputStream.close()
                            }
                        }

                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)

                        contentResolver.update(item, values, null, null)
                    }
                }

            } else {

                exportFile {

                    val path = Environment.getExternalStorageDirectory().absolutePath + "/BlackJin"

                    val storageDir = File(path)

                    if (!storageDir.exists()) {
                        storageDir.mkdirs()
                    }

                    val file = File("$path/$fileName").apply {
                        createNewFile()
                    }

                    val outputStream = FileOutputStream(file)
                    previewBitmap.compress(CompressFormat.PNG, 100, outputStream)
                    outputStream.close()

                    //갤러리에 반영하기
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(file)
                    })
                }
            }
        }
    }

    private fun exportFile(func: () -> Unit) {
        Completable
            .fromCallable {
                func.invoke()
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                showLoadingExport()
            }
            .doOnComplete {
                hideLoadingExport()
                showToast("complete")
            }
            .doOnError {
                hideLoadingExport()
                it.message?.let { message ->
                    showToast(message)
                }
            }
            .subscribe()
    }

    private fun showLoadingExport() {
        btnExport.isEnabled = false
        btnExport.text = resources.getString(R.string.loading)
    }

    private fun hideLoadingExport() {
        btnExport.isEnabled = true
        btnExport.text = resources.getString(R.string.export)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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