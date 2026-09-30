package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CreateReceiptScreen
import com.example.ui.screens.FirstTimeSetupScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrinterScreen
import com.example.ui.screens.ReceiptDetailScreen
import com.example.ui.screens.ReceiptHistoryScreen
import com.example.ui.screens.ShopProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AdManager
import com.example.viewmodel.ReceiptViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: ReceiptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.init(this)

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                Screen.SETUP -> FirstTimeSetupScreen(viewModel = viewModel)
                                Screen.HOME -> HomeScreen(viewModel = viewModel)
                                Screen.CREATE_RECEIPT -> CreateReceiptScreen(viewModel = viewModel)
                                Screen.RECEIPT_PREVIEW,
                                Screen.RECEIPT_DETAIL -> ReceiptDetailScreen(viewModel = viewModel)
                                Screen.RECEIPT_HISTORY -> ReceiptHistoryScreen(viewModel = viewModel)
                                Screen.SHOP_PROFILE -> ShopProfileScreen(viewModel = viewModel)
                                Screen.PRINTER -> PrinterScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
