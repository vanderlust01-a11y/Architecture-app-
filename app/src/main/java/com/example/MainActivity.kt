package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectEntity
import com.example.network.ValayDesignOutput
import com.example.ui.CustomBlueprintCanvas
import com.example.ui.ThreeDViewCanvas
import com.example.ui.ElevationViewCanvas
import com.example.ui.SectionViewCanvas
import com.example.ui.ValayUiState
import com.example.ui.ValayViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValayAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ValayViewModel = viewModel()
                    ValayMainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ValayAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4), // Deep Purple
            secondary = Color(0xFFD0BCFE), // Soft Lavender
            tertiary = Color(0xFFE8DEF8), // Very Light Lavender
            background = Color(0xFFFDF8F6), // Warm Chalk / Cream Background
            surface = Color(0xFFFFFFFF), // White card surface
            onPrimary = Color.White,
            onSecondary = Color(0xFF1D1B1E),
            onBackground = Color(0xFF1D1B1E),
            onSurface = Color(0xFF1D1B1E)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValayMainScreen(viewModel: ValayViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState(initial = emptyList())
    val activeState by viewModel.uiState.collectAsState()
    val activeDesign by viewModel.activeDesign.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    LaunchedEffect(currentScreen) {
        focusManager.clearFocus()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF7F2FA),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "designer",
                    onClick = { viewModel.setScreen("designer") },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Draft Studio") },
                    label = { Text("Draft Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = Color(0xFF21005D),
                        unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        indicatorColor = Color(0xFFE8DEF8)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "viewer",
                    onClick = { viewModel.setScreen("viewer") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Viewport") },
                    label = { Text("Viewport", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = Color(0xFF21005D),
                        unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        indicatorColor = Color(0xFFE8DEF8)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "history",
                    onClick = { viewModel.setScreen("history") },
                    icon = { Icon(Icons.Default.List, contentDescription = "History Registry") },
                    label = { Text("Registry", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = Color(0xFF21005D),
                        unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f),
                        indicatorColor = Color(0xFFE8DEF8)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "designer" -> DesignerFormScreen(viewModel)
                    "viewer" -> ViewportScreen(viewModel, activeState, activeDesign)
                    "history" -> HistoryScreen(viewModel, savedProjects)
                }
            }
        }
    }
}

@Composable
fun DesignerFormScreen(viewModel: ValayViewModel) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    // Form inputs
    val projName by viewModel.inputProjectName.collectAsState()
    val landArea by viewModel.inputLandArea.collectAsState()
    val style by viewModel.inputStyle.collectAsState()
    val budget by viewModel.inputBudget.collectAsState()
    val timeline by viewModel.inputTimeline.collectAsState()
    val airflow by viewModel.inputAirflow.collectAsState()
    val sunDir by viewModel.inputSunDir.collectAsState()
    val roomsNum by viewModel.inputRooms.collectAsState()
    val floors by viewModel.inputFloors.collectAsState()
    val demands by viewModel.inputDemands.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VALAY",
                    fontSize = 44.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1D1B1E),
                    lineHeight = 40.sp,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = "LITE DRAFTING SUITE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Sync/Mode status bar indicator - styled as a beautiful round pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFE8DEF8))
                    .clickable { viewModel.isOfflineMode.value = !isOfflineMode }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isOfflineMode) Color(0xFF6750A4) else Color(0xFF21005D))
                )
                Text(
                    text = if (isOfflineMode) "OFFLINE PROSTHESIS" else "ONLINE BLUEPRINT SYNC",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF21005D),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Project Identity details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0x66CAC4D0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "PROJECT IDENTITY",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = projName,
                    onValueChange = { viewModel.inputProjectName.value = it },
                    label = { Text("Draft Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0x99CAC4D0),
                        unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = landArea,
                        onValueChange = { viewModel.inputLandArea.value = it },
                        label = { Text("Area Size") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0x99CAC4D0),
                            unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = timeline,
                        onValueChange = { viewModel.inputTimeline.value = it },
                        label = { Text("Project Timeline") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0x99CAC4D0),
                            unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }

        // Structural and physical specifications
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0x66CAC4D0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    "PHYSICAL SPECIFICATIONS",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = roomsNum,
                    onValueChange = { viewModel.inputRooms.value = it },
                    label = { Text("Client Rooms Requirement (e.g. 3 BHK, Studio)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0x99CAC4D0),
                        unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                )

                // Floors Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Requested Levels / Floors", fontSize = 12.sp, color = Color(0xFF1D1B1E), fontWeight = FontWeight.Bold)
                        Text("$floors Floors", fontSize = 12.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.ExtraBold)
                    }
                    Slider(
                        value = floors.toFloat(),
                        onValueChange = { viewModel.inputFloors.value = it.toInt() },
                        valueRange = 1f..4f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6750A4),
                            activeTrackColor = Color(0xFF6750A4),
                            inactiveTrackColor = Color(0xFFE8DEF8)
                        )
                    )
                }

                // Dropdowns row: Style & Budget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StyleDropdown(
                        selectedStyle = style,
                        onStyleSelected = { viewModel.inputStyle.value = it },
                        modifier = Modifier.weight(1f)
                    )

                    BudgetDropdown(
                        selectedBudget = budget,
                        onBudgetSelected = { viewModel.inputBudget.value = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // INSULATION AND ECO ENVIRONMENT
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0x66CAC4D0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    "ENVIRONMENTAL VENTILATION ALIGNMENT",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AirDropdown(
                        selectedAir = airflow,
                        onAirSelected = { viewModel.inputAirflow.value = it },
                        modifier = Modifier.weight(1f),
                        title = "Dominant Wind"
                    )

                    AirDropdown(
                        selectedAir = sunDir,
                        onAirSelected = { viewModel.inputSunDir.value = it },
                        modifier = Modifier.weight(1f),
                        title = "Dominant Sunrise"
                    )
                }

                OutlinedTextField(
                    value = demands,
                    onValueChange = { viewModel.inputDemands.value = it },
                    label = { Text("Special Demands (Eco-friendly, ventilation, etc.)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0x99CAC4D0),
                        unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Generate Design Call action button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.generateDesign()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
                tint = Color.White
            )
            Text(
                text = "COMPILE PRELIMINARY CAD",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        // Disclaimer statement
        Text(
            text = "* Analysis generates drafting geometry compatible with professional tools including AutoCAD and Revit. Completely persistent offline database synced locally.",
            fontSize = 9.sp,
            color = Color(0xFF1D1B1E).copy(alpha = 0.6f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }
}

@Composable
fun ViewportScreen(
    viewModel: ValayViewModel,
    state: ValayUiState,
    design: ValayDesignOutput?
) {
    if (state is ValayUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF6750A4))
                Text(
                    text = "COMPILING VECTOR MATRICES...",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }
        }
        return
    }

    if (design == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(56.dp))
                Text(
                    "NO PORTFOLIO PREVIEW ACTIVE",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF1D1B1E).copy(alpha = 0.6f)
                )
                Button(
                    onClick = { viewModel.setScreen("designer") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("START DRAFTING STUDIO", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
        return
    }

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: 2D Blueprint, 1: 3D Concept Model
    val floors by viewModel.inputFloors.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Heading design title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = design.projectName.uppercase(),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1D1B1E)
                )
                Text(
                    text = "EST. COST: ${design.estimatedCost} | RATING: ${design.sustainabilityRating}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Quick back button
            OutlinedButton(
                onClick = { viewModel.setScreen("designer") },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(16.dp))
                Text(
                    text = "EDIT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Toggle blueprint view, elevation, section vs 3D model
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color(0xFFF7F2FA),
            contentColor = Color(0xFF6750A4),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = Color(0xFF6750A4)
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Text(
                        text = "2D PLAN",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (activeTab == 0) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (activeTab == 0) Color(0xFF21005D) else Color(0xFF49454F).copy(alpha = 0.8f)
                    )
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Text(
                        text = "ELEVATION",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (activeTab == 1) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (activeTab == 1) Color(0xFF21005D) else Color(0xFF49454F).copy(alpha = 0.8f)
                    )
                }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = {
                    Text(
                        text = "SECTION",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (activeTab == 2) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (activeTab == 2) Color(0xFF21005D) else Color(0xFF49454F).copy(alpha = 0.8f)
                    )
                }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = {
                    Text(
                        text = "3D SKETCH",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (activeTab == 3) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (activeTab == 3) Color(0xFF21005D) else Color(0xFF49454F).copy(alpha = 0.8f)
                    )
                }
            )
        }

        // Active Canvas pane
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            when (activeTab) {
                0 -> {
                    CustomBlueprintCanvas(
                        rooms = design.rooms,
                        airflowDir = viewModel.inputAirflow.value,
                        sunDir = viewModel.inputSunDir.value,
                        onRoomsUpdated = { updatedList ->
                            viewModel.updateActiveRooms(updatedList)
                        }
                    )

                    // Drag indicator overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xE605111A))
                            .border(0.5.dp, Color(0xFF2EA4DF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "DRAG CELLS TO TWEAK",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF2EA4DF)
                        )
                    }
                }
                1 -> {
                    ElevationViewCanvas(
                        style = viewModel.inputStyle.value,
                        floors = floors,
                        airflowDir = viewModel.inputAirflow.value,
                        sunDir = viewModel.inputSunDir.value
                    )
                }
                2 -> {
                    SectionViewCanvas(
                        rooms = design.rooms,
                        floors = floors,
                        style = viewModel.inputStyle.value
                    )
                }
                3 -> {
                    ThreeDViewCanvas(
                        rooms = design.rooms,
                        totalFloors = floors,
                        airflowDir = viewModel.inputAirflow.value,
                        sunDir = viewModel.inputSunDir.value
                    )
                }
            }
        }

        // CAD parameters / details card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B1E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            border = BorderStroke(1.dp, Color(0x26FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ARCHITECTURAL STUDY ANALYSIS",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD0BCFE),
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = design.explanation,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFE2F1F8)
                )

                Divider(color = Color(0x26FFFFFF))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Air Corridors", fontSize = 10.sp, color = Color(0xFFCCC2DC))
                        Text(design.ventilationAnalysis, fontSize = 12.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sunlight Efficiency", fontSize = 10.sp, color = Color(0xFFCCC2DC))
                        Text(design.sunlightOptimalTime, fontSize = 12.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color(0x26FFFFFF))

                Column {
                    Text("Exterior Facade Elevation Description", fontSize = 10.sp, color = Color(0xFFCCC2DC))
                    Text(design.elevationDescription, fontSize = 12.sp, color = Color(0xFFE2F1F8))
                }

                Divider(color = Color(0x26FFFFFF))

                Column {
                    Text("Transverse Vertical Section Specifications", fontSize = 10.sp, color = Color(0xFFCCC2DC))
                    Text(design.sectionDescription, fontSize = 12.sp, color = Color(0xFFE2F1F8))
                }

                Divider(color = Color(0x26FFFFFF))

                Column {
                    Text("3D Construction Scope", fontSize = 10.sp, color = Color(0xFFCCC2DC))
                    Text(design.concept3DDescription, fontSize = 12.sp, color = Color(0xFFE2F1F8))
                }
            }
        }

        // PRO EXPORTS block
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0x66CAC4D0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "EXPORT TO PROFESSIONAL CAD SUITES",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6750A4),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // JPG Snapshot export
                    ExportButton(
                        label = "JPG SCREEN",
                        onClick = {
                            val uri = viewModel.exportToJPG(context, design)
                            if (uri != null) {
                                triggerFileShare(context, uri, "image/jpeg")
                            } else {
                                Toast.makeText(context, "JPG Export failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // PDF document export
                    ExportButton(
                        label = "PDF SCHEM",
                        onClick = {
                            val uri = viewModel.exportToPDF(context, design)
                            if (uri != null) {
                                triggerFileShare(context, uri, "application/pdf")
                            } else {
                                Toast.makeText(context, "PDF Export failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // DXF AutoCAD export
                    ExportButton(
                        label = "DXF VECTOR",
                        onClick = {
                            val uri = viewModel.exportToDXF(context, design)
                            if (uri != null) {
                                triggerFileShare(context, uri, "application/dxf")
                            } else {
                                Toast.makeText(context, "DXF Export failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // DWG CAD export
                    ExportButton(
                        label = "DWG CAD",
                        onClick = {
                            val uri = viewModel.exportToDWG(context, design)
                            if (uri != null) {
                                triggerFileShare(context, uri, "application/dwg")
                            } else {
                                Toast.makeText(context, "DWG Export failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: ValayViewModel, list: List<ProjectEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "VALAY PORTFOLIO",
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1D1B1E),
                lineHeight = 30.sp,
                letterSpacing = (-1).sp
            )
            Text(
                text = "OFFLINE SYNC REGISTRY",
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6750A4),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6750A4).copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                    Text("No local blueprints in drafts table.", fontSize = 12.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF1D1B1E).copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectHistoryProject(item) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, Color(0x66CAC4D0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.projectName.uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF1D1B1E)
                                )
                                Text(
                                    text = "${item.selectedStyle} | Level: ${item.floors} flr | Size: ${item.landArea}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF1D1B1E).copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteProject(item) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Purge Draft", tint = Color(0xFFB3261E))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExportButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4)),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun StyleDropdown(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val styles = listOf("Modern", "Minimalist Scandinavian", "Brutalist", "Eco-Solar Cabin", "Scandinavian", "Industrial Draft")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedStyle,
            onValueChange = {},
            readOnly = true,
            label = { Text("Theme Style") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6750A4))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6750A4),
                focusedLabelColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0x99CAC4D0),
                unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFFFFFFF))
        ) {
            styles.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s, color = Color(0xFF1D1B1E), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onStyleSelected(s)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetDropdown(
    selectedBudget: String,
    onBudgetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val budgets = listOf("Low ($30k - $70k)", "Medium ($70k - $150k)", "Premium ($150k - $400k)")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedBudget,
            onValueChange = {},
            readOnly = true,
            label = { Text("Budget Bracket") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6750A4))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6750A4),
                focusedLabelColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0x99CAC4D0),
                unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFFFFFFF))
        ) {
            budgets.forEach { b ->
                DropdownMenuItem(
                    text = { Text(b, color = Color(0xFF1D1B1E), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onBudgetSelected(b)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AirDropdown(
    selectedAir: String,
    onAirSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String
) {
    var expanded by remember { mutableStateOf(false) }
    val orientations = listOf("North", "South", "East", "West", "Northeast", "Northwest", "Southeast", "Southwest")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedAir,
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6750A4))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6750A4),
                focusedLabelColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0x99CAC4D0),
                unfocusedLabelColor = Color(0xFF49454F).copy(alpha = 0.7f)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFFFFFFF))
        ) {
            orientations.forEach { o ->
                DropdownMenuItem(
                    text = { Text(o, color = Color(0xFF1D1B1E), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onAirSelected(o)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun triggerFileShare(context: Context, fileUri: Uri, mimeType: String) {
    try {
        val path = fileUri.path ?: return
        val rawFile = File(path)
        val contentUri = FileProvider.getUriForFile(context, "com.example.fileprovider", rawFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Valay Drafting CAD Export"))
    } catch (e: Exception) {
        Toast.makeText(context, "Sharing failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
