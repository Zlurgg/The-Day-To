package uk.co.zlurgg.thedayto.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.auth.ui.components.DevSignInButton
import uk.co.zlurgg.thedayto.auth.ui.components.SignInButton
import uk.co.zlurgg.thedayto.auth.ui.components.SignInFooter
import uk.co.zlurgg.thedayto.auth.ui.components.WelcomeHeader
import uk.co.zlurgg.thedayto.auth.ui.state.SignInNavigationTarget
import uk.co.zlurgg.thedayto.auth.ui.state.SignInUiEvent
import uk.co.zlurgg.thedayto.core.ui.components.CustomSnackbarHost
import uk.co.zlurgg.thedayto.core.ui.components.WelcomeDialog
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingLarge

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun SignInScreenRoot(
    viewModel: SignInViewModel = koinViewModel(),
    onNavigateToOverview: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle navigation state
    LaunchedEffect(state.navigationTarget) {
        state.navigationTarget?.let { target ->
            when (target) {
                SignInNavigationTarget.ToOverview -> onNavigateToOverview()
            }
            viewModel.onNavigationHandled()
        }
    }

    // Handle one-time UI events (snackbars)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is SignInUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Check sign-in status on launch
    LaunchedEffect(key1 = Unit) {
        viewModel.checkSignInStatus()
    }

    // Delegate to presenter
    SignInScreen(
        showWelcomeDialog = state.showWelcomeDialog,
        onDismissWelcomeDialog = { viewModel.dismissWelcomeDialog() },
        onSignInClick = { viewModel.signIn() },
        onDevSignInClick = { viewModel.devSignIn() },
        isDevSignInAvailable = state.isDevSignInAvailable,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun SignInScreen(
    showWelcomeDialog: Boolean,
    onDismissWelcomeDialog: () -> Unit,
    onSignInClick: () -> Unit,
    onDevSignInClick: () -> Unit,
    isDevSignInAvailable: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    // Show welcome dialog for first-time users
    if (showWelcomeDialog) {
        WelcomeDialog(onDismiss = onDismissWelcomeDialog)
    }

    Scaffold(
        snackbarHost = { CustomSnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        SignInScreenContent(
            onSignInClick = onSignInClick,
            onDevSignInClick = onDevSignInClick,
            isDevSignInAvailable = isDevSignInAvailable,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
private fun SignInScreenContent(
    onSignInClick: () -> Unit,
    onDevSignInClick: () -> Unit,
    isDevSignInAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation states for staggered entrance
    var showWelcome by remember { mutableStateOf(false) }
    var showAppName by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Staggered animation entrance
    LaunchedEffect(Unit) {
        showWelcome = true
        delay(200)
        showAppName = true
        delay(200)
        showSubtitle = true
        delay(300)
        showButton = true
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(paddingLarge),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                WelcomeHeader(
                    showWelcome = showWelcome,
                    showAppName = showAppName,
                    showSubtitle = showSubtitle
                )

                SignInButton(
                    onClick = onSignInClick,
                    showButton = showButton
                )

                if (isDevSignInAvailable) {
                    DevSignInButton(
                        onClick = onDevSignInClick,
                        showButton = showButton
                    )
                }

                SignInFooter(
                    showButton = showButton
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SignInScreenContentPreview() {
    TheDayToTheme {
        SignInScreenContent(
            onSignInClick = { },
            onDevSignInClick = { },
            isDevSignInAvailable = true
        )
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SignInScreenContentDarkPreview() {
    TheDayToTheme(useDarkTheme = true) {
        SignInScreenContent(
            onSignInClick = { },
            onDevSignInClick = { },
            isDevSignInAvailable = true
        )
    }
}