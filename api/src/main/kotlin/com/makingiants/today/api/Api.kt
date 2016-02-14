package com.makingiants.today.api

import android.content.Context
import android.support.annotation.IntDef
import android.support.annotation.VisibleForTesting
import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.makingiants.today.api.error_handling.ApiErrorHandler
import com.squareup.okhttp.OkHttpClient
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Every SERVICE that is going to be created should be done by [Api.create] func.
 * and not using [Api.sRestAdapter].
 *
 *
 * Each request throw an [ApiException] if something happens.

 * @see Api.init
 * @see Api.create
 */
object Api {
    const val LOG_LEVEL_NONE = 0L
    const val LOG_LEVEL_BASIC = 1L
    const val LOG_LEVEL_FULL = 2L

    @IntDef(LOG_LEVEL_NONE, LOG_LEVEL_BASIC, LOG_LEVEL_FULL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    private var sRestAdapter: RestAdapter? = null
    private var mWeakContext: WeakReference<Context>? = null

    fun init(context: Context, host: String, @LogLevel logLevel: Long = LOG_LEVEL_NONE) {
        val okHttpClient = OkHttpClient()
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS) // Initial value: 10
        okHttpClient.setWriteTimeout(30, TimeUnit.SECONDS)
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS)

        mWeakContext = WeakReference(context)
        sRestAdapter = RestAdapter.Builder()
                .setEndpoint(host)
                .setClient(OkClient(okHttpClient))
                .setConverter(GsonConverter(gson()))
                .setErrorHandler(ApiErrorHandler())
                .build()

        sRestAdapter?.setLogLevel(when (logLevel) {
            LOG_LEVEL_BASIC -> RestAdapter.LogLevel.BASIC
            LOG_LEVEL_FULL -> RestAdapter.LogLevel.FULL
            else -> RestAdapter.LogLevel.NONE
        })
    }

    fun <T> create(service: Class<T>): T? = sRestAdapter?.create(service)

    @VisibleForTesting
    fun gson(): Gson = GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create()
}
