package cl.duocuc.pagoflex.data.remote

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val URL_POR_DEFECTO = "http://10.0.2.2:8080/"

    @Volatile
    private var servidor: HttpUrl = URL_POR_DEFECTO.toHttpUrl()

    val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val original = cadena.request()
                val url = original.url.newBuilder()
                    .scheme(servidor.scheme)
                    .host(servidor.host)
                    .port(servidor.port)
                    .build()
                cadena.proceed(original.newBuilder().url(url).build())
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_POR_DEFECTO)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun cambiarServidor(url: String) {
        url.toHttpUrlOrNull()?.let { servidor = it }
    }

    fun <T> crear(api: Class<T>): T = retrofit.create(api)
}
