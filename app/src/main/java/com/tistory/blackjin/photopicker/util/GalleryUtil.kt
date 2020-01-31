package com.tistory.blackjin.photopicker.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.tistory.blackjin.photopicker.R
import com.tistory.blackjin.photopicker.model.Album
import com.tistory.blackjin.photopicker.model.Gallery
import io.reactivex.Single
import timber.log.Timber
import java.util.*

object GalleryUtil {

    private const val INDEX_MEDIA_URI = MediaStore.MediaColumns._ID
    private const val INDEX_DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED

    //private const val DISPLAY_NAME = MediaStore.Images.Media.DISPLAY_NAME
    private const val albumName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME

    fun getMedia(context: Context): Single<List<Album>> {
        return Single.create { emitter ->
            try {

                val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                val sortOrder = "$INDEX_DATE_ADDED DESC"
                val projection = arrayOf(INDEX_MEDIA_URI, albumName, INDEX_DATE_ADDED)
                val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)

                val albumList: List<Album> = cursor?.let {

                    Timber.d(Arrays.toString(it.columnNames))

                    val totalImageList =
                        generateSequence { if (cursor.moveToNext()) cursor else null }
                            .map(GalleryUtil::getImage)
                            .filterNotNull()
                            .toList()

                    Timber.d("$totalImageList")

                    val albumList: List<Album> = totalImageList.asSequence()
                        .groupBy { media -> media.albumName }
                        /*.toSortedMap(Comparator { albumName1: String, albumName2: String ->
                            if (albumName2 == "Camera") {
                                1
                            } else {
                                albumName1.compareTo(albumName2, true)
                            }
                        })*/
                        .map(GalleryUtil::getAlbum)
                        .toList()

                    val totalAlbum = totalImageList.run {
                        val albumName = context.getString(R.string.image_picker_album_all)
                        Album(
                            albumName,
                            this
                        )
                    }

                    mutableListOf(totalAlbum).apply {
                        addAll(albumList)
                    }

                } ?: emptyList()

                cursor?.close()
                emitter.onSuccess(albumList)
            } catch (exception: Exception) {
                emitter.onError(exception)
            }

        }
    }

    private fun getAlbum(entry: Map.Entry<String, List<Gallery>>): Album {
        return Album(entry.key, entry.value)
    }

    private fun getImage(cursor: Cursor): Gallery? =
        try {
            cursor.run {

                val idColumn = getColumnIndex(INDEX_MEDIA_URI)
                val id = cursor.getLong(idColumn)

                val folderName = getString(getColumnIndex(albumName))

                val mediaUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                Gallery(folderName, mediaUri)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
}