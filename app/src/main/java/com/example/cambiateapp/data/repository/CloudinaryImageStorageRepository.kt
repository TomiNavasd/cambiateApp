package com.example.cambiateapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.cambiateapp.data.remote.CloudinaryConfig
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.repository.ImageStorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryImageStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageStorageRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun subirImagen(uri: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bytes = leerBytes(uri)
                Result.success(subir(bytes))
            } catch (e: CancellationException) {
                throw e
            } catch (e: DomainException) {
                Result.failure(e)
            } catch (e: IOException) {
                Result.failure(DomainException.SinConexion)
            } catch (e: Exception) {
                Result.failure(DomainException.Desconocido(e))
            }
        }

    private fun leerBytes(uri: String): ByteArray {
        val stream = try {
            context.contentResolver.openInputStream(Uri.parse(uri))
        } catch (e: FileNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        }
        return stream?.use { it.readBytes() }
            ?: throw DomainException.Validacion(CampoInvalido.FOTO)
    }

    private fun subir(bytes: ByteArray): String {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
            .addFormDataPart("file", "prenda.jpg", bytes.toRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url(CloudinaryConfig.UPLOAD_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val texto = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw DomainException.Desconocido(
                    IOException("Cloudinary respondió ${response.code}: $texto")
                )
            }
            return JSONObject(texto).getString("secure_url")
        }
    }
}