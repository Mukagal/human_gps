package com.example.pmadvanced.data.repository

import com.example.pmadvanced.data.local.AuthTokenStore
import com.example.pmadvanced.data.remote.MamanTapApi
import retrofit2.Response

abstract class BaseRepository(
    private val api: MamanTapApi,
    private val tokenStore: AuthTokenStore
) {
    protected suspend fun <T> authenticatedCall(
        block: suspend () -> Response<T>
    ): T? {
        var response = block()
        if (response.code() == 401 && refreshAccessToken()) {
            response = block()
        }
        return response.body().takeIf { response.isSuccessful }
    }

    protected suspend fun isSuccessfulCall(
        block: suspend () -> Response<*>
    ): Boolean {
        var response = block()
        if (response.code() == 401 && refreshAccessToken()) {
            response = block()
        }
        return response.isSuccessful
    }

    private suspend fun refreshAccessToken(): Boolean {
        val refreshToken = tokenStore.refreshToken
        if (refreshToken.isBlank()) return false

        val response = api.refreshToken("Bearer $refreshToken")
        val newToken = response.body()?.accessToken
        if (!response.isSuccessful || newToken.isNullOrBlank()) return false

        tokenStore.saveAccessToken(newToken)
        return true
    }
}
