package com.tistory.blackjin.photopicker

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.tistory.blackjin.photopicker.adapter.AlbumAdapter
import com.tistory.blackjin.photopicker.adapter.GridSpacingItemDecoration
import com.tistory.blackjin.photopicker.adapter.MediaAdapter
import com.tistory.blackjin.photopicker.databinding.ActivityMainBinding
import com.tistory.blackjin.photopicker.model.Album
import com.tistory.blackjin.photopicker.model.Gallery
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

    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaAdapter: MediaAdapter

    private lateinit var albumAdapter: AlbumAdapter

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupMediaRecyclerView()
        setupAlbumRecyclerView()
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

    private fun setupBottomView() {
        llSelectedAlbum.setOnClickListener {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
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
                override fun onItemClick(data: Gallery) {
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

    private fun goToPreview(uri: Uri) {
        PreviewActivity.startPreviewActivity(this, uri)
    }

    private fun setupAlbumRecyclerView() {
        albumAdapter = AlbumAdapter().apply {
            onItemClickListener = object : AlbumAdapter.OnItemClickListener {
                override fun onItemClick(data: Album) {
                    setSelectedAlbum(data)
                    closeDrawer()
                }

            }
        }

        with(rvAlbum) {
            adapter = albumAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayout.VERTICAL))
            itemAnimator = null
        }
    }

    private fun loadMedia() {
        disposable = GalleryUtil.getMedia(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albumList: List<Album> ->
                albumAdapter.replaceAll(albumList)

                //albumList's first index is "ALL"
                setSelectedAlbum(albumList[0])
                closeDrawer()
            }
    }

    private fun setSelectedAlbum(album: Album) {
        binding.selectedAlbum = album
        mediaAdapter.replaceAll(album.galleryUris)
    }

    private fun closeDrawer() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000

        private const val IMAGE_SPAN_COUNT = 3
    }

}
