package com.example.app_rpg.viewmodel

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

private const val TAG = "AppRpgViewModel"

internal fun tratadorDeErro(mensagem: String) = CoroutineExceptionHandler { _, erro ->
    Log.e(TAG, mensagem, erro)
}
