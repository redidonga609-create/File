package com.example.myapplication

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

// Состояния UI
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val text: String) : UiState()
    data class Error(val message: String) : UiState()
}

// ViewModel для управления состоянием
class FileReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val fileReaderWorker = FileReaderWorker()
    private val appContext = application.applicationContext

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun loadFile(uri: Uri) {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val result = fileReaderWorker.readFileContent(appContext, uri)

            result.onSuccess { content ->
                _uiState.value = UiState.Success(content)
            }.onFailure { exception ->
                _uiState.value = UiState.Error(exception.message ?: "Неизвестная ошибка")
            }
        }
    }
}

// Класс для чтения файлов
class FileReaderWorker {

    suspend fun readFileContent(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    return@withContext Result.failure(
                        Exception("Не удалось открыть поток ввода")
                    )
                }

                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                    stringBuilder.append("\n")
                }

                reader.close()
                inputStream.close()

                val content = stringBuilder.toString()

                if (content.isBlank()) {
                    Result.failure(Exception("Файл пустой"))
                } else {
                    Result.success(content)
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}