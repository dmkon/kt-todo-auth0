package com.nortohol.auth0

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nortohol.auth0.util.UiText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Router(context = LocalContext.current)
            }
        }
    }
}

@Composable
fun Router(
    context: Context,
    todoAuthViewModel: TodoAuthViewModel = viewModel()
) {
    val logIn = todoAuthViewModel::logIn
    val logOut = todoAuthViewModel::logOut
    val getProfile = todoAuthViewModel::getProfile
    when (val state = todoAuthViewModel.uiState.collectAsState().value) {
        is TodoAuthViewModel.ToDoUiState.Empty -> ToDoLoadedScreen(
            context = context,
            state.data,
            logIn,
            logOut,
            getProfile
        )
        is TodoAuthViewModel.ToDoUiState.Loading ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        is TodoAuthViewModel.ToDoUiState.Error -> {
            ToDoLoadedScreen(
                context = context,
                state.data,
                logIn,
                logOut,
                getProfile
            )
            ErrorDialog(state.message)
        }
        is TodoAuthViewModel.ToDoUiState.Loaded -> ToDoLoadedScreen(
            context = context,
            state.data,
            logIn,
            logOut,
            getProfile
        )
    }
}

@Composable
fun ToDoLoadedScreen(
    context: Context,
    data: TodoAuthUiModel,
    logIn: (Context) -> Unit,
    logOut: (Context) -> Unit,
    getProfile: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.top_app_bar)) },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "fab icon"
                )
            }
        },
        drawerContent = {
            DrawerContent(
                context = context,
                data = data,
                logIn = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                    }
                    logIn(context)
                },
                logOut = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                    }
                    logOut(context)
                },
                getProfile = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                    }
                    getProfile()
                }
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row{
                    Text(stringResource(id = R.string.is_logged))
                    Text("${data.isSignedIn}")
                }
                Row{
                    Text(stringResource(id = R.string.name))
                    Text("${data.name}")
                }
                Row{
                    Text(stringResource(id = R.string.email))
                    Text("${data.email}")
                }
            }
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.primary) {
                Text(stringResource(id = R.string.bottom_app_bar))
            }
        }
    )
}

@Composable
fun ErrorDialog(message: UiText) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = stringResource(R.string.problem_occurred))
            },
            text = {
                Text(message.asString())
            },
            confirmButton = {
                Button(onClick = {
                    openDialog.value = false
                }) {
                    Text(text = stringResource(R.string.yes))
                }
            },

            dismissButton = {
                Button(onClick = {
                    openDialog.value = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DrawerContent(
    context: Context,
    data: TodoAuthUiModel,
    logIn: (Context) -> Unit,
    logOut: (Context) -> Unit,
    getProfile: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .alpha(if (!data.isSignedIn) 1.0f else 0.5f)
                .clickable(
                    enabled = !data.isSignedIn,
                    onClick = { logIn(context) }
                )
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                style = MaterialTheme.typography.h4,
                text = stringResource(R.string.login)
            )
        }
        Row(
            modifier = Modifier
                .alpha(if (data.isSignedIn) 1.0f else 0.5f)
                .clickable(
                    enabled = data.isSignedIn,
                    onClick = getProfile
                )
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                style = MaterialTheme.typography.h4,
                text = stringResource(R.string.profile)
            )
        }
        Row(
            modifier = Modifier
                .alpha(if (data.isSignedIn) 1.0f else 0.5f)
                .clickable(
                    enabled = data.isSignedIn,
                    onClick = { logOut(context) }
                )
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                style = MaterialTheme.typography.h4,
                text = stringResource(R.string.logout)
            )
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme {
        ToDoLoadedScreen(
            context = LocalContext.current,
            data = TodoAuthUiModel(isSignedIn = false),
            logIn = {},
            logOut = {},
            getProfile = {}
        )
    }
}