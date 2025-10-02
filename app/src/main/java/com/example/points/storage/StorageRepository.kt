package com.example.points.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun uploadBrandLogo(fileUri: Uri, fileName: String): String {
        val ref = storage.reference.child("${StoragePaths.BRAND_LOGOS_DIR}/$fileName")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadMedia(fileUri: Uri, fileName: String): String {
        val ref = storage.reference.child("${StoragePaths.MEDIA_DIR}/$fileName")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }
}


