package com.dharmabit.notes

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.dharmabit.notes.data.AudioHandler
import com.dharmabit.notes.data.ChecklistItem
import com.dharmabit.notes.data.Note
import com.dharmabit.notes.data.NoteType
import com.dharmabit.notes.ui.NoteViewModel
import com.dharmabit.notes.ui.NoteViewModelFactory
import com.dharmabit.notes.ui.auth.SecuritySetupScreen
import com.dharmabit.notes.ui.auth.UnlockScreen
import com.dharmabit.notes.ui.auth.PrivateNotesToggle
import com.dharmabit.notes.ui.theme.NotesTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NotesApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(LocalContext.current.applicationContext as Application))

    val isUnlocked by noteViewModel.isUnlocked.collectAsState()
    val isPrivateMode by noteViewModel.isPrivateMode.collectAsState()
    val securityManager = noteViewModel.getSecurityManager()

    // Check if security is enabled and show unlock screen
    if (securityManager.isSecurityEnabled() && !isUnlocked) {
        UnlockScreen(
            securityManager = securityManager,
            onUnlocked = { noteViewModel.unlock() }
        )
        return
    }

    val navItems = buildList {
        add("com.dharmabit.notes" to ("Notes" to Icons.Outlined.Lightbulb))
        add("archive" to ("Archive" to Icons.Outlined.Archive))
        if (securityManager.isSecurityEnabled()) {
            add("security_settings" to ("Security" to Icons.Outlined.Security))
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Notes", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                // Private notes toggle (if enabled)
                if (securityManager.isPrivateNotesEnabled()) {
                    PrivateNotesToggle(
                        isPrivateMode = isPrivateMode,
                        onToggle = { noteViewModel.togglePrivateMode() }
                    )
                }

                navItems.forEach { (route, details) ->
                    val (label, icon) = details
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                // Security setup option (if not enabled)
                if (!securityManager.isSecurityEnabled()) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Lock, contentDescription = "Enable Security") },
                        label = { Text("Enable Security") },
                        selected = currentRoute == "security_setup",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("security_setup")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        AppNavigation(
            navController = navController,
            viewModel = noteViewModel,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: NoteViewModel, onMenuClick: () -> Unit) {
    NavHost(navController = navController, startDestination = "com.dharmabit.notes") {
        composable("com.dharmabit.notes") {
            NoteListScreen(
                navController = navController,
                viewModel = viewModel,
                notes = viewModel.activeNotes.collectAsState().value,
                onMenuClick = onMenuClick,
                isArchiveScreen = false
            )
        }
        composable("archive") {
            NoteListScreen(
                navController = navController,
                viewModel = viewModel,
                notes = viewModel.archivedNotes.collectAsState().value,
                onMenuClick = onMenuClick,
                isArchiveScreen = true
            )
        }
        composable("security_setup") {
            SecuritySetupScreen(
                securityManager = viewModel.getSecurityManager(),
                onSecurityEnabled = {
                    viewModel.unlock()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("security_settings") {
            SecuritySettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "add_edit_note?noteId={noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType; defaultValue = -1 }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
        ) {
            AddEditNoteScreen(
                navController = navController,
                viewModel = viewModel,
                noteId = it.arguments?.getInt("noteId") ?: -1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val securityManager = viewModel.getSecurityManager()
    var showDisableConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Security Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Security: Enabled")
                    Text("Biometric: ${if (securityManager.isBiometricEnabled()) "Enabled" else "Disabled"}")
                    Text("Private Notes: ${if (securityManager.isPrivateNotesEnabled()) "Enabled" else "Disabled"}")
                }
            }

            TextButton(
                onClick = { showDisableConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disable Security", color = MaterialTheme.colorScheme.error)
            }

            if (showDisableConfirm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Disable Security?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "All encrypted notes will be decrypted and security features will be disabled.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDisableConfirm = false }) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    viewModel.disableSecurity()
                                    onBack()
                                }
                            ) {
                                Text("Disable", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    navController: NavController, viewModel: NoteViewModel, notes: List<Note>,
    onMenuClick: () -> Unit, isArchiveScreen: Boolean
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isPrivateMode by viewModel.isPrivateMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            isArchiveScreen && isPrivateMode -> "Private Archive"
                            isArchiveScreen -> "Archive"
                            isPrivateMode -> "Private Notes"
                            else -> "Notes"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open menu")
                    }
                },
                actions = {
                    if (isPrivateMode) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Private Mode",
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (!isArchiveScreen) {
                FloatingActionButton(onClick = { navController.navigate("add_edit_note") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty())
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, "Clear search")
                        }
                },
                shape = CircleShape
            )

            AnimatedContent(targetState = notes.isEmpty(), label = "EmptyStateAnimation") { isEmpty ->
                if (isEmpty) {
                    EmptyStateView(isArchiveScreen, searchQuery.isNotBlank(), isPrivateMode)
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(notes, key = { it.id }) { note ->
                            Box(modifier = Modifier.animateItemPlacement(tween(300))) {
                                NoteItemCard(
                                    note = note,
                                    onClick = { navController.navigate("add_edit_note?noteId=${note.id}") },
                                    isPrivateMode = isPrivateMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(isArchiveScreen: Boolean, isSearching: Boolean, isPrivateMode: Boolean) {
    val icon = when {
        isSearching -> Icons.Outlined.SearchOff
        isArchiveScreen -> Icons.Outlined.Archive
        isPrivateMode -> Icons.Outlined.Lock
        else -> Icons.Outlined.Lightbulb
    }

    val text = when {
        isSearching -> "No results found"
        isArchiveScreen && isPrivateMode -> "Your private archive is empty"
        isArchiveScreen -> "Your archive is empty"
        isPrivateMode -> "No private notes yet"
        else -> "The notes you add will appear here"
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun NoteItemCard(note: Note, onClick: () -> Unit, isPrivateMode: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            0.5.dp,
            if (isPrivateMode) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrivateMode)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            note.imageUri?.let {
                AsyncImage(
                    model = Uri.parse(it),
                    contentDescription = "Note Image",
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (note.title.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (note.isEncrypted) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = note.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 3,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (note.audioPath != null) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Has voice recording",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    when (note.noteType) {
                        NoteType.TEXT -> {
                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                maxLines = 10,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NoteType.CHECKLIST -> {
                            note.checklistItems.take(5).forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Checkbox",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        fontSize = 14.sp,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (item.isChecked) 0.6f else 1f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// Rest of the existing composables (AddEditNoteScreen, ChecklistItemRow) remain the same
// but need to be updated to handle private mode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(navController: NavController, viewModel: NoteViewModel, noteId: Int) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var noteType by remember { mutableStateOf(NoteType.TEXT) }
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioHandler = remember { AudioHandler(context) }
    val isPrivateMode by viewModel.isPrivateMode.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasAudioPermission = isGranted }
    )

    val focusManager = LocalFocusManager.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { imageUri = it.toString() } }
    )

    // Load existing note data
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            viewModel.getNoteById(noteId)?.let { note ->
                title = note.title
                content = note.content
                isPinned = note.isPinned
                isArchived = note.isArchived
                noteType = note.noteType
                checklistItems = note.checklistItems
                imageUri = note.imageUri
                audioPath = note.audioPath
            }
        }
    }

    fun saveNote() {
        audioHandler.stopRecording()
        audioHandler.stopPlayback()

        val finalChecklist = if (noteType == NoteType.CHECKLIST) {
            checklistItems.filter { it.text.isNotBlank() }
        } else emptyList()

        val finalContent = if (noteType == NoteType.TEXT) content else ""

        val noteToSave = Note(
            id = if (noteId != -1) noteId else 0,
            title = title,
            content = finalContent,
            isPinned = isPinned,
            isArchived = isArchived,
            noteType = noteType,
            checklistItems = finalChecklist,
            imageUri = imageUri,
            audioPath = audioPath,
            timestamp = System.currentTimeMillis(),
            isPrivate = isPrivateMode // Set based on current mode
        )

        val isContentEmpty = if (noteType == NoteType.TEXT) {
            finalContent.isBlank()
        } else {
            finalChecklist.isEmpty()
        }

        if (title.isNotBlank() || !isContentEmpty || imageUri != null || audioPath != null) {
            viewModel.insertOrUpdate(noteToSave)
        }
    }

    fun deleteNote() {
        if (noteId != -1) {
            audioHandler.stopRecording()
            audioHandler.stopPlayback()
            isDeleted = true
            viewModel.deleteById(noteId)
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isDeleted) {
                saveNote()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isPrivateMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Private",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Private Note")
                        }
                    } else {
                        Text("")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (noteId != -1) {
                        val archiveIcon = if (isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive
                        IconButton(onClick = {
                            isArchived = !isArchived
                            if (isArchived) isPinned = false
                            saveNote()
                            navController.popBackStack()
                        }) {
                            Icon(archiveIcon, "Archive/Unarchive")
                        }
                    }
                    if (!isArchived) {
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                "Pin"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                actions = {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Outlined.Image, "Add image")
                    }

                    IconButton(onClick = {
                        noteType = if (noteType == NoteType.TEXT) NoteType.CHECKLIST else NoteType.TEXT
                    }) {
                        Icon(
                            if (noteType == NoteType.TEXT) Icons.Outlined.CheckBox else Icons.Outlined.TextFields,
                            "Change type"
                        )
                    }

                    IconButton(onClick = {
                        if (hasAudioPermission) {
                            if (isRecording) {
                                audioHandler.stopRecording()
                                isRecording = false
                            } else {
                                audioPath = audioHandler.startRecording()
                                isRecording = true
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            "Record",
                            tint = if (isRecording) Color.Red else LocalContentColor.current
                        )
                    }

                    if (noteId != -1) {
                        IconButton(onClick = { deleteNote() }) {
                            Icon(Icons.Outlined.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                imageUri?.let {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        AsyncImage(
                            model = Uri.parse(it),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "Remove image", tint = Color.White)
                        }
                    }
                }
            }

            item {
                audioPath?.let { path ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (isPlaying) {
                                audioHandler.stopPlayback()
                                isPlaying = false
                            } else {
                                isPlaying = true
                                audioHandler.play(path) { isPlaying = false }
                            }
                        }) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                "Play/Pause"
                            )
                        }
                        Text(
                            if (isPlaying) "Playing..." else "Recording",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            audioHandler.stopPlayback()
                            isPlaying = false
                            audioPath = null
                        }) {
                            Icon(Icons.Default.Close, "Remove recording")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            when (noteType) {
                NoteType.TEXT -> {
                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = { Text("Write a note...") },
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
                NoteType.CHECKLIST -> {
                    items(checklistItems.size, key = { checklistItems[it].id }) { index ->
                        ChecklistItemRow(
                            item = checklistItems[index],
                            onCheckedChange = { isChecked ->
                                val newList = checklistItems.toMutableList()
                                newList[index] = newList[index].copy(isChecked = isChecked)
                                checklistItems = newList
                            },
                            onTextChange = { newText ->
                                val newList = checklistItems.toMutableList()
                                newList[index] = newList[index].copy(text = newText)
                                checklistItems = newList
                            },
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDelete = {
                                val newList = checklistItems.toMutableList()
                                newList.removeAt(index)
                                checklistItems = newList
                            }
                        )
                    }
                    item {
                        TextButton(
                            onClick = {
                                checklistItems = checklistItems + ChecklistItem(text = "", isChecked = false)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Add, "Add item")
                            Text("Add item")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onNext: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = onCheckedChange)
        OutlinedTextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            textStyle = TextStyle(
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "Delete item")
        }
    }
}