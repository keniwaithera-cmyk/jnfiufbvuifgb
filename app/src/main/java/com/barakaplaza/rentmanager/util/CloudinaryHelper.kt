package com.barakaplaza.rentmanager.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object CloudinaryHelper {
    private val client = OkHttpClient()

    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, uri) ?: return@withContext null
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", AppConstants.CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder", "baraka_plaza/houses")
                .build()
            val req = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${AppConstants.CLOUDINARY_CLOUD_NAME}/image/upload")
                .post(body).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) JSONObject(resp.body?.string() ?: "").optString("secure_url") else null
        } catch (e: Exception) { null }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val tmp = File.createTempFile("house_", ".jpg", context.cacheDir)
            FileOutputStream(tmp).use { input.copyTo(it) }
            tmp
        } catch (e: Exception) { null }
    }
}
