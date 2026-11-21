package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.theme.BackgroundColor
import com.example.myapplication.ui.theme.ButtonColor
import com.example.myapplication.ui.theme.ButtonContentColor
import com.example.myapplication.ui.theme.ErrorColor
import com.example.myapplication.ui.theme.TextColor
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import com.example.myapplication.ui.theme.ContentTextStyle
import com.example.myapplication.ui.theme.ErrorTextStyle

class TextViewerActivity : ComponentActivity() {

    private lateinit var viewModel: FileReaderViewModel

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.loadFile(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[FileReaderViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundColor
                ) {
                    TextViewerScreen(
                        viewModel = viewModel,
                        onOpenFileClick = { openFilePicker() }
                    )
                }
            }
        }

        viewModel.uiState.observe(this) { state ->
            if (state is UiState.Error) {
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openFilePicker() {
        openDocumentLauncher.launch(arrayOf("text/plain"))
    }
}

@Composable
fun TextViewerScreen(
    viewModel: FileReaderViewModel,
    onOpenFileClick: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState(UiState.Idle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onOpenFileClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonColor,
                contentColor = ButtonContentColor
            )
        ) {
            Text("Открыть файл .txt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is UiState.Idle -> { /* Пустой экран */ }

            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Success -> {
                SelectionContainer {
                    Text(
                        text = state.text,
                        style = ContentTextStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }

            is UiState.Error -> {
                Text(
                    text = "Ошибка: ${state.message}",
                    style = ErrorTextStyle,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}