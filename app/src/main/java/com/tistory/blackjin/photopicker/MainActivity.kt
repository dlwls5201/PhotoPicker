package com.tistory.blackjin.photopicker

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.tistory.blackjin.photopicker.adapter.GridSpacingItemDecoration
import com.tistory.blackjin.photopicker.adapter.MediaAdapter
import com.tistory.blackjin.photopicker.model.Album
import com.tistory.blackjin.photopicker.model.Media
import com.tistory.blackjin.photopicker.util.GalleryUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val permissionCheckRead by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private lateinit var mediaAdapter: MediaAdapter

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupMediaRecyclerView()
        setupBottomView()
        chkPermission()
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Timber.e("permission reject")
                    return
                }
            }
            loadMedia()
        }
    }

    private fun chkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(permissionCheckRead == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
                showRequestPermission()
            } else {
                // 권한 있음
                loadMedia()
            }
        }
    }

    private fun showRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter().apply {
            onItemClickListener = object : MediaAdapter.OnItemClickListener {
                override fun onItemClick(data: Media) {
                    goToPreview(data.uri)
                }
            }
        }

        with(rvMedia) {
            layoutManager = GridLayoutManager(this@MainActivity, IMAGE_SPAN_COUNT)
            addItemDecoration(GridSpacingItemDecoration(IMAGE_SPAN_COUNT, 4))
            itemAnimator = null
            adapter = mediaAdapter
        }
    }

    //TODO goToPreview
    private fun goToPreview(uri: Uri) {
        Timber.d("uri : $uri")
    }

    private fun setupBottomView() {
        llSelectedAlbum.setOnClickListener {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun loadMedia() {
        disposable = GalleryUtil.getMedia(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albumList: List<Album> ->
                setSelectedAlbum(albumList)
            }
    }

    private fun setSelectedAlbum(albumList: List<Album>) {
        //albumList's first index is "ALL"
        val album = albumList[0]
        mediaAdapter.replaceAll(album.mediaUris)
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000

        private const val IMAGE_SPAN_COUNT = 3
    }

}
