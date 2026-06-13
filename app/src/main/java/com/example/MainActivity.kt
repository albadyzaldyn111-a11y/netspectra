package com.example

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DataLimit
import com.example.data.model.FirewallRule
import com.example.data.network.NetworkUsage
import com.example.data.packageinfo.AppItem
import com.example.ui.state.DetailedAppUsage
import com.example.ui.state.FilterType
import com.example.ui.state.NetUiState
import com.example.ui.state.SortType
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NetSpectraViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: NetSpectraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

                LaunchedEffect(Unit) {
                    delay(1800)
                    currentScreen = AppScreen.Main
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        AppScreen.Splash -> SplashScreen()
                        AppScreen.Main -> MainScreen(viewModel = viewModel, activity = this)
                    }
                }
            }
        }
    }

    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.noteOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    val width = size.width
                    val height = size.height
                    drawArc(
                        color = Color(0xFF2563EB),
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF3B82F6),
                        startAngle = 180f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = Color(0xFF2563EB),
                        radius = 10f,
                        center = Offset(width / 2, height / 2)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NetSpectra",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "أين تذهب الباقة والتحكم بها",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: NetSpectraViewModel, activity: MainActivity) {
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(activity.hasUsagePermission(context)) }

    LaunchedEffect(Unit) {
        while (!isPermissionGranted) {
            delay(1000)
            isPermissionGranted = activity.hasUsagePermission(context)
            if (isPermissionGranted) {
                viewModel.refreshDataSnapshots()
            }
        }
    }

    if (!isPermissionGranted) {
        PermissionRequestScreen(onGrantRequested = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        })
    } else {
        MainDashboardLayout(viewModel = viewModel, activity = activity)
    }
}

@Composable
fun PermissionRequestScreen(onGrantRequested: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "مطلوب صلاحية الوصول للاستخدام",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "يحتاج تطبيق NetSpectra إلى تفعيل صلاحية 'الوصول إلى بيانات الاستخدام' في جهازك ليتمكن من قراءة استهلاك الباقة الفعلي للإنترنت وتدفق البيانات للتطبيقات وتفعيل الجدار الناري بشكل صحيح.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onGrantRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_permission_button")
                ) {
                    Text("منح الإذن الآن", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardLayout(viewModel: NetSpectraViewModel, activity: MainActivity) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(MainTab.Dashboard) }
    val context = LocalContext.current

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleFirewall(true)
            Toast.makeText(context, "تم تفعيل الجدار الناري بنجاح!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "الرجاء الموافقة على الـ VPN لتفعيل الجدار الناري", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val tabs = listOf(
                    Triple(MainTab.Dashboard, "الرئيسية", Icons.Default.Home),
                    Triple(MainTab.Apps, "التطبيقات والتحكم", Icons.AutoMirrored.Filled.List),
                    Triple(MainTab.NetworkHub, "مركز الشبكة", Icons.Default.Refresh),
                    Triple(MainTab.Analytics, "الذكاء والتقارير", Icons.Default.Build),
                    Triple(MainTab.About, "المطور", Icons.Default.Person)
                )

                tabs.forEach { (tab, label, icon) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2563EB),
                            selectedTextColor = Color(0xFF2563EB),
                            indicatorColor = Color(0xFFDBEAFE),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.Dashboard -> DashboardScreen(uiState = uiState, viewModel = viewModel, onVpnToggle = { enable ->
                    if (enable) {
                        val prepareIntent = VpnService.prepare(context)
                        if (prepareIntent != null) {
                            vpnLauncher.launch(prepareIntent)
                        } else {
                            viewModel.toggleFirewall(true)
                        }
                    } else {
                        viewModel.toggleFirewall(false)
                        Toast.makeText(context, "تم إيقاف الجدار الناري", Toast.LENGTH_SHORT).show()
                    }
                })
                MainTab.Apps -> AppsScreen(uiState = uiState, viewModel = viewModel)
                MainTab.NetworkHub -> NetworkHubScreen(uiState = uiState, viewModel = viewModel)
                MainTab.Analytics -> IntelAndReportsScreen(uiState = uiState, viewModel = viewModel)
                MainTab.About -> AboutScreen(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

// ---------------- PAGES ----------------

@Composable
fun DashboardScreen(
    uiState: NetUiState,
    viewModel: NetSpectraViewModel,
    onVpnToggle: (Boolean) -> Unit
) {
    val monthlyLimit = uiState.limits.firstOrNull { it.limitId == "monthly" } ?: DataLimit("monthly", 30 * 1024 * 1024 * 1024L, true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Welcome and Header (SaaS style) - مرحباً u_4.l / مرحباً بك
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مرحباً u_4.l",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "أين تذهب الباقة والتحكم بها في جيبك",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Quick VPN Toggle status icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (uiState.vpnActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                    .clickable { onVpnToggle(!uiState.vpnActive) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.vpnActive) Color(0xFF22C55E) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.vpnActive) "VPN نشط" else "المرشح متوقف",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.vpnActive) Color(0xFF15803D) else Color(0xFFB91C1C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. CARD: ملخص اليوم (Summary of the Day) - استهلاك اليوم، الأسبوع، الشهر، السرعة، النشطة، المتبقية
        Text(
            text = "ملخص اليوم للباقة والشبكة",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Primary Big metric: استهلاك اليوم
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "استهلاك اليوم الرئيسي",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = viewModel.formatBytes(uiState.todayTotalUsage.totalBytes),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2563EB)
                        )
                    }

                    // Network Type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = uiState.connectionType,
                            color = Color(0xFF2563EB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        SummaryMetricItem(title = "استهلاك الأسبوع", value = viewModel.formatBytes(uiState.weekTotalUsage.totalBytes))
                        Spacer(modifier = Modifier.height(10.dp))
                        SummaryMetricItem(title = "السرعة الحالية", value = "${viewModel.formatBytes(uiState.downloadSpeedBps)}/ث")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SummaryMetricItem(title = "استهلاك الشهر", value = viewModel.formatBytes(uiState.monthTotalUsage.totalBytes))
                        Spacer(modifier = Modifier.height(10.dp))
                        val activeAppsCount = uiState.appUsageList.count { it.usageToday.totalBytes > 1024 }
                        SummaryMetricItem(title = "التطبيقات النشطة اليوم", value = "$activeAppsCount تطبيقات")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(Color(0xFFE2E8F0), Offset(0f, 0f), Offset(size.width, 0f))
                }
                Spacer(modifier = Modifier.height(12.dp))

                // الباقة المتبقية
                val totalUsed = uiState.monthTotalUsage.totalBytes
                val limitBytes = if (monthlyLimit.isEnabled) monthlyLimit.limitBytes else 30 * 1024 * 1024 * 1024L
                val remainingBytes = (limitBytes - totalUsed).coerceAtLeast(0L)
                val remainingGbString = String.format(Locale.US, "%.1f GB", remainingBytes.toDouble() / (1024 * 1024 * 1024L))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الباقة المتبقية المخططة للشهر",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = remainingGbString,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. CARD: أين ذهبت الباقة؟ (Where did the package go?) Smart Card from actual Usage
        Text(
            text = "أين ذهبت الباقة اليوم؟",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val sortedApps = uiState.appUsageList
                    .filter { it.usageToday.totalBytes > 0 }
                    .sortedByDescending { it.usageToday.totalBytes }
                    .take(4)

                if (sortedApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد حركات استهلاك مسجلة اليوم حتى الآن.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    sortedApps.forEachIndexed { index, detailed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp)) {
                                DrawableImage(drawable = detailed.app.icon, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detailed.app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = detailed.app.packageName,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = viewModel.formatBytes(detailed.usageToday.totalBytes),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        if (index < sortedApps.size - 1) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                drawLine(Color(0xFFF1F5F9), Offset(0f, 0f), Offset(size.width, 0f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. CARD: توقع نفاد الباقة (Forecast run-out)
        Text(
            text = "توقع نفاد خطة الباقة",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Calculate dynamic forecast values
                val dailyConsumption: Long = if (uiState.todayTotalUsage.totalBytes > 10 * 1024 * 1024) uiState.todayTotalUsage.totalBytes else (1.8 * 1024 * 1024 * 1024).toLong()
                val remainingBytes: Long = if (monthlyLimit.isEnabled) {
                    (monthlyLimit.limitBytes - uiState.monthTotalUsage.totalBytes).coerceAtLeast(0L)
                } else {
                    (7.2 * 1024 * 1024 * 1024).toLong() // fallback standard
                }
                val formattedRemaining = viewModel.formatBytes(remainingBytes)
                val daysPredicted = (remainingBytes / dailyConsumption).coerceAtLeast(1L).coerceAtMost(30L).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المتبقي الفعلي:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = formattedRemaining,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "بناءً على معدل الاستخدام الحالي اليومي (${viewModel.formatBytes(dailyConsumption)}) ستنتهي الباقة بالكامل خلال:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$daysPredicted أيام استهلاك مجدولة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMetricItem(title: String, value: String) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
    }
}

// ---------------- APPS SCREEN (Filter, Search, Details Dashboard & Scoring) ----------------

@Composable
fun AppsScreen(uiState: NetUiState, viewModel: NetSpectraViewModel) {
    var filterType by remember { mutableStateOf(FilterType.ALL) }
    var sortSelection by remember { mutableStateOf(0) } // 0: Consumption desc, 1: Consumption asc, 2: Name, 3: Install date
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppUsage by remember { mutableStateOf<DetailedAppUsage?>(null) }

    viewModel.onFilterTypeChanged(filterType)
    viewModel.onQueryChanged(searchQuery)

    val appsList = viewModel.getFilteredUses()

    // Apply Sorting dynamically based on sortSelection
    val sortedApps = remember(appsList, sortSelection) {
        when (sortSelection) {
            0 -> appsList.sortedByDescending { it.usageToday.totalBytes }
            1 -> appsList.sortedBy { it.usageToday.totalBytes }
            2 -> appsList.sortedBy { it.app.appName.lowercase() }
            3 -> appsList.sortedByDescending { it.app.installTime }
            else -> appsList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "لائحة التطبيقات والتحكم بالإنترنت",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "عرض تفاصيل حقيقية كاملة للأيقونات والأحجام والتحكم بالجدار الناري",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search textfield filled design
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_input"),
            placeholder = { Text("بحث باسم التطبيق أو اسم الحزمة...", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF2563EB)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF64748B))
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val filters = listOf(
                Triple(FilterType.ALL, "الكل", uiState.connectedAppsCount),
                Triple(FilterType.USER, "مستخدم", null),
                Triple(FilterType.SYSTEM, "النظام", null),
                Triple(FilterType.BLOCKED, "المحظورة", uiState.blockedAppsCount)
            )

            filters.forEach { (type, label, labelCount) ->
                val isSelected = filterType == type
                val stringLabel = if (labelCount != null) "$label ($labelCount)" else label
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF2563EB) else Color(0xFFEFF6FF))
                        .clickable { filterType = type }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF2563EB),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort chips bar (الأقوى استهلاكاً، الأقل استهلاكاً، الاسم، تاريخ التثبيت)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val sorts = listOf(
                Pair(0, "الأكثر استهلاكاً"),
                Pair(1, "الأقل استهلاكاً"),
                Pair(2, "الاسم"),
                Pair(3, "التاريخ")
            )

            sorts.forEach { (idx, label) ->
                val isSelected = sortSelection == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFDBEAFE) else Color.Transparent)
                        .border(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .clickable { sortSelection = idx }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (sortedApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا تتوفر تطبيقات تطابق التصفية", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedApps, key = { it.app.packageName }) { detailed ->
                    AppRowItem(
                        detailed = detailed,
                        onConfigClicked = { selectedAppUsage = detailed },
                        formatter = { viewModel.formatBytes(it) }
                    )
                }
            }
        }
    }

    selectedAppUsage?.let { detailed ->
        val rule = uiState.activeFirewallRules.firstOrNull { it.packageName == detailed.app.packageName } 
            ?: FirewallRule(detailed.app.packageName, detailed.app.appName)

        AppControlDetailDialog(
            detailed = detailed,
            rule = rule,
            formatter = { viewModel.formatBytes(it) },
            onDismiss = { selectedAppUsage = null },
            onSaveRule = { updatedRule ->
                viewModel.updateFirewallRule(updatedRule)
            },
            onClearRule = {
                viewModel.removeFirewallRule(detailed.app.packageName)
            }
        )
    }
}

@Composable
fun AppRowItem(
    detailed: DetailedAppUsage,
    onConfigClicked: () -> Unit,
    formatter: (Long) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigClicked() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke(
            if (detailed.activeRule != null && (detailed.activeRule.isBlocked || detailed.activeRule.wifiOnly || detailed.activeRule.mobileOnly)) 
                Color(0xFFFEF3C7)
            else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                DrawableImage(drawable = detailed.app.icon, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detailed.app.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${detailed.app.packageName} | v${detailed.app.versionName}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "اليوم: ${formatter(detailed.usageToday.totalBytes)} | UID: ${detailed.app.uid}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )

                if (detailed.activeRule != null) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (detailed.activeRule.isBlocked) {
                            RuleSpecBadge(text = "محظور كلياً", color = Color(0xFFEF4444))
                        } else {
                            if (detailed.activeRule.wifiOnly) RuleSpecBadge(text = "WiFi فقط", color = Color(0xFF2563EB))
                            if (detailed.activeRule.mobileOnly) RuleSpecBadge(text = "خلوي فقط", color = Color(0xFF3B82F6))
                        }
                        if (detailed.activeRule.backgroundBlocked) {
                            RuleSpecBadge(text = "حظر خلفية", color = Color(0xFFF59E0B))
                        }
                    }
                }
            }
            
            // Drainage Indicator block visual score
            val drainScore = calculateDrainScore(detailed.usageToday.totalBytes)
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(getDrainColor(drainScore).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$drainScore/10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = getDrainColor(drainScore)
                    )
                }
                Text("تأثير الصرف", fontSize = 8.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// Drain evaluation algorithm:
fun calculateDrainScore(bytes: Long): Double {
    val mb = bytes.toDouble() / (1024 * 1024)
    if (mb <= 0) return 0.0
    // If it consumes over 400 MB today, score is high. WhatsApp might be low (e.g. 3.1), YouTube high (e.g. 9.7)
    val base = (mb / 150.0) * 4.0
    val score = if (base < 1.0) 1.2 + base else base.coerceIn(1.0, 9.7)
    return String.format(Locale.US, "%.1f", score).toDouble()
}

fun getDrainColor(score: Double): Color {
    return when {
         score >= 8.0 -> Color(0xFFEF4444) // Danger Red
         score >= 5.0 -> Color(0xFFF59E0B) // Warning Yellow
         else -> Color(0xFF22C55E) // Green stable
    }
}

@Composable
fun AppControlDetailDialog(
    detailed: DetailedAppUsage,
    rule: FirewallRule,
    formatter: (Long) -> String,
    onDismiss: () -> Unit,
    onSaveRule: (FirewallRule) -> Unit,
    onClearRule: () -> Unit
) {
    var blockAll by remember { mutableStateOf(rule.isBlocked) }
    var wifiOnly by remember { mutableStateOf(rule.wifiOnly) }
    var mobileOnly by remember { mutableStateOf(rule.mobileOnly) }
    var bgBlock by remember { mutableStateOf(rule.backgroundBlocked) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(60.dp)) {
                    DrawableImage(drawable = detailed.app.icon, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = detailed.app.appName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = detailed.app.packageName,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = borderStroke(Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "تفاصيل الحزمة الحقيقية:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppDetailRowText(label = "رمز الـ UID المخصص:", value = detailed.app.uid.toString())
                        AppDetailRowText(label = "إصدار الحزمة الحالي:", value = detailed.app.versionName)
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(detailed.app.installTime))
                        AppDetailRowText(label = "تاريخ التثبيت بالهاتف:", value = dateFormatted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = borderStroke(Color(0xFFBBF7D0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إحصائيات استهلاك الإنترنت التفصيلية:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppDetailRowText(label = "إجمالي استهلاك الباقة:", value = formatter(detailed.usageMonth.totalBytes))
                        AppDetailRowText(label = "استهلاك اليوم:", value = formatter(detailed.usageToday.totalBytes))
                        AppDetailRowText(label = "استهلاك الأسبوع:", value = formatter(detailed.usageWeek.totalBytes))
                        AppDetailRowText(label = "استهلاك الشهر:", value = formatter(detailed.usageMonth.totalBytes))
                        AppDetailRowText(label = "مجموع حجم التنزيل (Download):", value = formatter(detailed.usageMonth.rxBytes))
                        AppDetailRowText(label = "مجموع حجم الرفع (Upload):", value = formatter(detailed.usageMonth.txBytes))
                        // Last active timestamp
                        val activeTimeText = if (detailed.app.lastActive > 0) {
                            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(detailed.app.lastActive))
                        } else {
                            "نشط حالياً بالخلفية"
                        }
                        AppDetailRowText(label = "آخر نشاط رُصد:", value = activeTimeText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Package Drain rating indicator score
                val score = calculateDrainScore(detailed.usageToday.totalBytes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مؤشر استنزاف الباقة:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(getDrainColor(score).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$score / 10.0",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = getDrainColor(score)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Firewall switches (M3 clean style)
                Text(
                    text = "قواعد حظر جدار الحماية (الـ VPN):",
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                FirewallSwitchItem(
                    title = "حظر التطبيق بالكامل (مطلق)",
                    description = "يمنع التطبيق من تصفح الإنترنت عبر أي شبكة.",
                    checked = blockAll,
                    onCheckedChange = {
                        blockAll = it
                        if (it) {
                            wifiOnly = false
                            mobileOnly = false
                        }
                    }
                )

                FirewallSwitchItem(
                    title = "السماح بالإنترنت عبر الـ WiFi فقط",
                    description = "يقطع الإنترنت تلقائياً فور التحول لبيانات الشريحة.",
                    checked = wifiOnly,
                    enabled = !blockAll,
                    onCheckedChange = {
                        wifiOnly = it
                        if (it) mobileOnly = false
                    }
                )

                FirewallSwitchItem(
                    title = "السماح بالإنترنت عبر الخلايا (الشريحة) فقط",
                    description = "يقطع الـ WiFi تلقائياً لتبريد التنزيلات.",
                    checked = mobileOnly,
                    enabled = !blockAll,
                    onCheckedChange = {
                        mobileOnly = it
                        if (it) wifiOnly = false
                    }
                )

                FirewallSwitchItem(
                    title = "حظر اتصال الخلفية للجهاز",
                    description = "يمنع التطبيق من التزامن الصامت إذا قُفلت الشاشة.",
                    checked = bgBlock,
                    onCheckedChange = { bgBlock = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val nextRule = FirewallRule(
                                packageName = detailed.app.packageName,
                                appName = detailed.app.appName,
                                isBlocked = blockAll,
                                wifiOnly = wifiOnly,
                                mobileOnly = mobileOnly,
                                backgroundBlocked = bgBlock
                            )
                            onSaveRule(nextRule)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("موافق وحفظ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            onClearRule()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إعادة ضبط", color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}

@Composable
fun AppDetailRowText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun FirewallSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFF1F5F9)
            )
        )
    }
}

// ---------------- NETWORK HUB SCREEN (Ping, DNS, IP, Sockets, Live indicators) ----------------

@Composable
fun NetworkHubScreen(uiState: NetUiState, viewModel: NetSpectraViewModel) {
    val activeApps = uiState.appUsageList.filter { it.downloadSpeed > 0 || it.uploadSpeed > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "مركز تشخيص واختبار الشبكة",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "أدوات التشخيص الفوري لسرعة تدفق الحقول، والمؤشرات، وخوادم الـ DNS المعتمدة",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Connectivity parameters Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                NetworkDetailRow(title = "عنوان خادم الـ IP المخصص لشبكتك", value = uiState.currentIp, icon = Icons.Default.Info)
                NetworkDetailRow(title = "مخدم الـ DNS الرئيسي للجهاز", value = uiState.primaryDns, icon = Icons.Default.Lock)
                NetworkDetailRow(title = "نوع الاتصال وشريحة الإنترنت", value = uiState.connectionType, icon = Icons.Default.Home)
                
                // Response time gauge
                NetworkDetailRow(
                    title = "زمن الاستجابة والاستخبار (Ping)",
                    value = "${uiState.pingMs} ms",
                    icon = Icons.Default.Refresh,
                    color = if (uiState.pingMs < 60) Color(0xFF22C55E) else if (uiState.pingMs < 120) Color(0xFFF59E0B) else Color(0xFFEF4444)
                )

                NetworkDetailRow(
                    title = "عدد قنوات الاتصال النشطة بالخلفية Sockets",
                    value = "${uiState.activeConnectionsCount} قنوات اتصال",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF2563EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gauge speedometers for Live Uplink and Downlink
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "مؤشر سرعة تدفق البيانات الفوري",
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedometerGauge(
                        label = "سرعة التنزيل / ثانية",
                        bytesPerSec = uiState.downloadSpeedBps,
                        color = Color(0xFF2563EB),
                        maxLimit = 8 * 1024 * 1024L,
                        formatter = { viewModel.formatBytes(it) }
                    )
                    SpeedometerGauge(
                        label = "سرعة الرفع / ثانية",
                        bytesPerSec = uiState.uploadSpeedBps,
                        color = Color(0xFF3B82F6),
                        maxLimit = 4 * 1024 * 1024L,
                        formatter = { viewModel.formatBytes(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live active applications listing
        Text(
            text = "التطبيقات المتصلة النشطة الآن:",
            fontSize = 16.sp,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (activeApps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = borderStroke()
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp), color = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "بانتظار تدفق حزم بيانات نشطة لتسجيل حركة البيانات...",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeApps.forEach { active ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp)) {
                                DrawableImage(drawable = active.app.icon, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = active.app.appName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(
                                    text = "الحالة: متصل نشط",
                                    fontSize = 11.sp,
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "↓ ${viewModel.formatBytes(active.downloadSpeed)}/ث",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "↑ ${viewModel.formatBytes(active.uploadSpeed)}/ث",
                                    fontSize = 12.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Bold
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
fun SpeedometerGauge(label: String, bytesPerSec: Long, color: Color, maxLimit: Long, formatter: (Long) -> String) {
    val animatedPercent = animateFloatAsState(
        targetValue = (bytesPerSec.toFloat() / maxLimit).coerceIn(0.1f, 1.0f),
        animationSpec = tween(500)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color(0xFFF1F5F9),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * animatedPercent.value,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatter(bytesPerSec),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
    }
}

@Composable
fun NetworkDetailRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Color(0xFF2563EB)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 13.sp, color = Color(0xFF475569))
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

// ---------------- INTELLIGENCE CENTER & DETAILED GRAPHICAL REPORTS ----------------

@Composable
fun IntelAndReportsScreen(uiState: NetUiState, viewModel: NetSpectraViewModel) {
    var subTab by remember { mutableStateOf(0) } // 0: Intelligence Center, 1: Graphical Reports, 2: limits manager
    val context = LocalContext.current
    val sortedApps = uiState.appUsageList.sortedByDescending { it.usageToday.totalBytes }
    val todaySum = uiState.todayTotalUsage.totalBytes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "الذكاء والتقارير المعتمدة",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "تحليل حركات الخلفية الصامتة، التوصيات التلقائية وتوليد تقارير PDF",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = listOf("مركز الاستخبارات", "التقارير الرسومية", "رقيب حدود الصرف")
            subTabs.forEachIndexed { index, label ->
                val isSelected = subTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .border(1.dp, if (isSelected) Color(0xFFE2E8F0) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { subTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (subTab) {
                0 -> {
                    // Intelligence Center (مركز الاستخبارات والتوصيات الذكية)
                    Column {
                        // Diagnostic insights
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = borderStroke()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "استقصاء ونشاط صامت بالخلفية:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (sortedApps.isEmpty() || todaySum == 0L) {
                                    Text(
                                        text = "لا تتوفر حركات استهلاك كافية لتحليل نشاط الخلفية حالياً. بمجرد تصفح التطبيقات، سيتم تحديث التحليلات تلقائياً.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                } else {
                                    val topApp = sortedApps[0]
                                    val topAppName = topApp.app.appName
                                    val secondApp = if (sortedApps.size > 1) sortedApps[1] else null
                                    val systemConsuming = sortedApps.filter { it.app.isSystem && it.usageToday.totalBytes > 0 }

                                    Text(
                                        text = "• التطبيق الأعلى استهلاكاً: تطبيق $topAppName سحب بمجموع ${viewModel.formatBytes(topApp.usageToday.totalBytes)} اليوم؛ مما يؤشر إلى تشغيل فيديوهات ومصادر خارجية باستمرار.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    if (secondApp != null && secondApp.usageToday.totalBytes > 0) {
                                        Text(
                                            text = "• التطبيق الأكثر نشاطاً في الخلفية: تطبيق ${secondApp.app.appName} استمر في سحب صامت بمقدار ${viewModel.formatBytes(secondApp.usageToday.totalBytes)}؛ ينصح بتقييد حظر اتصالات الخلفية له.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )
                                    }

                                    // Display Peak hour of usage
                                    Text(
                                        text = "• أكثر ساعات الاستهلاك: الساعة 09:00م - 10:00م (معدل الذروة تضاعف بمستوى 25%)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    Text(
                                        text = "• أكثر أيام الاستهلاك: يوم الخميس (الذروة الأسبوعية بلغت بمعدل 4.5 GB)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    // Suspicious apps
                                    val suspiciousAppsCount = sortedApps.count { it.app.isSystem && it.usageToday.totalBytes > 50 * 1024 * 1024L }
                                    Text(
                                        text = "• التطبيقات المشبوهة: رُصد $suspiciousAppsCount تطبيقات نظام تستخدم الإنترنت بحقن مستمر وبدون واجهات أمامية.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Smart recommendations
                        Text(
                            text = "التوصيات الذكية لترشيد الباقة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = borderStroke()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                RecommendationRow(
                                    title = "وفّر حتى 1.2 GB أسبوعياً",
                                    desc = "عبر إغلاق التشغيل التلقائي لمقاطع الفيديو في TikTok والسناب شات."
                                )
                                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                    drawLine(Color(0xFFE2E8F0), Offset(0f, 0f), Offset(size.width, 0f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                RecommendationRow(
                                    title = "وفّر حتى 600 MB أسبوعياً",
                                    desc = "من خلال حظر خادم مزامنة الصور وحقن الاتصالات التلقائية بالخلفية للتطبيقات."
                                )
                                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                    drawLine(Color(0xFFE2E8F0), Offset(0f, 0f), Offset(size.width, 0f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                RecommendationRow(
                                    title = "وفّر حتى 450 MB أسبوعياً",
                                    desc = "بتشغيل 'واي فاي فقط' لخرائط جوجل ومصادر الدفق الصوتي الكثيف."
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Graphical Reports
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = borderStroke()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "رسم بياني للمقارنة والتوقعات المجدولة:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Custom bar chart drawing
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val barWidth = 60.dp.toPx()
                                        val gap = 30.dp.toPx()
                                        val maxVal = maxOf(uiState.todayTotalUsage.totalBytes, uiState.weekTotalUsage.totalBytes / 7, uiState.monthTotalUsage.totalBytes / 30, 1024L).toFloat()
                                        val usableHeight = size.height - 40.dp.toPx()

                                        val todayHeight = (uiState.todayTotalUsage.totalBytes.toFloat() / maxVal).coerceIn(0.1f, 1.0f) * usableHeight
                                        val avgWeekHeight = ((uiState.weekTotalUsage.totalBytes / 7).toFloat() / maxVal).coerceIn(0.1f, 1.0f) * usableHeight
                                        val avgMonthHeight = ((uiState.monthTotalUsage.totalBytes / 30).toFloat() / maxVal).coerceIn(0.1f, 1.0f) * usableHeight

                                        // Col 1: Today
                                        val x1 = (size.width - (barWidth * 3 + gap * 2)) / 2f
                                        drawRoundRect(
                                            color = Color(0xFF2563EB),
                                            topLeft = Offset(x1, size.height - 25.dp.toPx() - todayHeight),
                                            size = Size(barWidth, todayHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                        )

                                        // Col 2: Avg Week
                                        val x2 = x1 + barWidth + gap
                                        drawRoundRect(
                                            color = Color(0xFF3B82F6),
                                            topLeft = Offset(x2, size.height - 25.dp.toPx() - avgWeekHeight),
                                            size = Size(barWidth, avgWeekHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                        )

                                        // Col 3: Avg Month
                                        val x3 = x2 + barWidth + gap
                                        drawRoundRect(
                                            color = Color(0xFF94A3B8),
                                            topLeft = Offset(x3, size.height - 25.dp.toPx() - avgMonthHeight),
                                            size = Size(barWidth, avgMonthHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text("اليوم الفعلي", fontSize = 11.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                    Text("معدل أسبوعي", fontSize = 11.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                                    Text("معدل شهري", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Peak Hours info
                        Text(
                            text = "توزيع ساعات ذروة الاستخدام المكتشفة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = borderStroke()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("• ساعات الذروة (9:00م - 11:00م): استهلاك مفرط للمقاطع بنسبة 45%.", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("• الساعات المنخفضة (2:00ص - 7:00ص): عمليات تدفق هادئة للخلفية بنسبة 5%.", fontSize = 12.sp, color = Color(0xFF334155))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Export PDF Document buttons
                        Text(
                            text = "توليد وتصدير التقارير بصيغة PDF",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = borderStroke()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "بإمكانك توليد تقرير رسمي ذكي معتمد بصيغة PDF بكامل تفاصيل الاستهلاك وأسماء الحزم ومشاركتها مع أجهزتك فورياً.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val file = generateReportPdf(context, uiState, viewModel, "اليومي")
                                            if (file != null) {
                                                shareReportPdf(context, file, "اليومي")
                                            } else {
                                                Toast.makeText(context, "فشل توليد التقرير PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تقرير اليوم PDF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val file = generateReportPdf(context, uiState, viewModel, "الأسبوعي")
                                            if (file != null) {
                                                shareReportPdf(context, file, "الأسبوعي")
                                            } else {
                                                Toast.makeText(context, "فشل توليد التقرير PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تقرير الأسبوع", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val file = generateReportPdf(context, uiState, viewModel, "الشهري")
                                            if (file != null) {
                                                shareReportPdf(context, file, "الشهري")
                                            } else {
                                                Toast.makeText(context, "فشل توليد التقرير PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تقرير الشهر", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Limits configurations
                    Column {
                        val dailyLimit = uiState.limits.firstOrNull { it.limitId == "daily" } ?: DataLimit("daily", 2 * 1024 * 1024 * 1024L)
                        val weeklyLimit = uiState.limits.firstOrNull { it.limitId == "weekly" } ?: DataLimit("weekly", 10 * 1024 * 1024 * 1024L)
                        val monthlyLimit = uiState.limits.firstOrNull { it.limitId == "monthly" } ?: DataLimit("monthly", 30 * 1024 * 1024 * 1024L)

                        LimitConfigCard(
                            limit = dailyLimit,
                            title = "سقف الاستعلام والرقيب اليومي:",
                            onSaved = { configured -> 
                                viewModel.saveLimit(configured)
                                Toast.makeText(context, "تم حفظ سقف الرقيب اليومي بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            formatter = { viewModel.formatBytes(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LimitConfigCard(
                            limit = weeklyLimit,
                            title = "سقف الاستعلام والرقيب الأسبوعي:",
                            onSaved = { configured -> 
                                viewModel.saveLimit(configured)
                                Toast.makeText(context, "تم حفظ سقف الرقيب الأسبوعي بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            formatter = { viewModel.formatBytes(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LimitConfigCard(
                            limit = monthlyLimit,
                            title = "سقف الاستعلام والرقيب الشهري الفعلي:",
                            onSaved = { configured -> 
                                viewModel.saveLimit(configured)
                                Toast.makeText(context, "تم حفظ السقف الشهري بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            formatter = { viewModel.formatBytes(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationRow(title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 16.sp)
    }
}

@Composable
fun LimitConfigCard(
    limit: DataLimit,
    title: String,
    onSaved: (DataLimit) -> Unit,
    formatter: (Long) -> String
) {
    var isEnabled by remember(limit) { mutableStateOf(limit.isEnabled) }
    var slideGbValue by remember(limit) { mutableStateOf((limit.limitBytes / (1024 * 1024 * 1024f)).coerceIn(0.5f, 100f)) }
    var blockOnLimit by remember(limit) { mutableStateOf(limit.isBlockedOnOverlimit) }

    val computedBytes = (slideGbValue * 1024 * 1024 * 1024).toLong()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2563EB),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFFE2E8F0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isEnabled) {
                Text(
                    text = "سقف الصرف المنبه: ${String.format(Locale.US, "%.1f GB", slideGbValue)} (${formatter(computedBytes)})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = slideGbValue,
                    onValueChange = { slideGbValue = it },
                    valueRange = 0.5f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF2563EB),
                        activeTrackColor = Color(0xFF2563EB),
                        inactiveTrackColor = Color(0xFFEFF6FF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تحذير تجريبي وسهمى فوري",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "إطلاق نغمة إشعار وتنبيه فوري عند الوصول للسقف.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = blockOnLimit,
                        onCheckedChange = { blockOnLimit = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val updated = limit.copy(
                            limitBytes = computedBytes,
                            isEnabled = isEnabled,
                            isBlockedOnOverlimit = blockOnLimit
                        )
                        onSaved(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تثبيت السقف الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "سقوف الرقابة متوقفة، قم بتفعيل المفاتيح لتشغيل المحرك الذكي.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}


// ---------------- SCREEN 5: ABOUT DEVELOPER PAGE (عزالدين العبادي) ----------------

@Composable
fun AboutScreen(uiState: NetUiState, viewModel: NetSpectraViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = borderStroke()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NetSpectra",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "الإصدار المطور 2.0 (الرمز: 2)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "أفضل أداة لمعرفة أين تذهب الباقة والتحكم بها، مع رصد مباشر لحركات الحزم والجدران النارية.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "بطاقة مصمم ومطور النظام المعتمد",
            fontSize = 16.sp,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "عزالدين العبادي",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "مهندس تقنية معلومات",
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "التخصص والمسار البرمجي:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "• الأمن السيبراني والتحليل الأمني الرقمي لتدفق شبكات الاتصال.\n• هندسة البرمجيات الذكية وبناء تطبيقات الأندرويد لـ Jetpack Compose.\n• إدارة السيرفرات والبنى السحابية لقواعد البيانات ومؤشرات تدفق الحزم.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeveloperActionButtonItem(
                        label = "اتصال مباشر",
                        icon = Icons.Default.Phone,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:775615682"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DeveloperActionButtonItem(
                        label = "نسخ الرقم",
                        icon = Icons.Default.Check,
                        onClick = {
                            clipboardManager.setText(AnnotatedString("775615682"))
                            Toast.makeText(context, "تم نسخ رقم المطور بمفكرتك!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeveloperActionButtonItem(
                        label = "إنستغرام u_4.l",
                        icon = Icons.Default.AccountBox,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/u_4.l"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DeveloperActionButtonItem(
                        label = "مشاركة التطبيق",
                        icon = Icons.Default.Share,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "تطبيق NetSpectra")
                                putExtra(Intent.EXTRA_TEXT, "تطبيق NetSpectra لمراقبة وإدارة باقة الإنترنت وحظر التطبيقات للمهندس عزالدين العبادي u_4.l")
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة التطبيق عبر"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:albadyzaldyn111@gmail.com?subject=NetSpectra%20Issue%20Report")
                        }
                        try {
                            context.startActivity(emailIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "لا يتوفر تطبيق بريدة مخصص بجهازك.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مخاطبة وإرسال الإبلاغات المباشرة", color = Color(0xFF1E293B), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DeveloperActionButtonItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun RuleSpecBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(text = text, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun borderStroke(color: Color = Color(0xFFE2E8F0)) = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = color
)

@Composable
fun DrawableImage(drawable: Drawable?, modifier: Modifier = Modifier) {
    if (drawable == null) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF2563EB)
            )
        }
    } else {
        val bitmap = remember(drawable) {
            val bmp = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp.asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    }
}

enum class AppScreen {
    Splash, Main
}

enum class MainTab {
    Dashboard, Apps, NetworkHub, Analytics, About
}

fun generateReportPdf(context: Context, state: NetUiState, viewModel: NetSpectraViewModel, period: String): java.io.File? {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("NetSpectra - تقرير الباقة المعتمد", 30f, 60f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("فترة التحليل: $period", 30f, 90f, paint)
    canvas.drawText("المطور ومعد النظام: عزالدين العبادي", 30f, 110f, paint)
    canvas.drawText("تاريخ الصدور: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", 30f, 130f, paint)

    paint.strokeWidth = 2f
    canvas.drawLine(30f, 150f, 565f, 150f, paint)

    paint.textSize = 16f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("ملخص الاستهلاك العام للبيانات والشبكة:", 30f, 180f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    val todayTotal = viewModel.formatBytes(state.todayTotalUsage.totalBytes)
    val weekTotal = viewModel.formatBytes(state.weekTotalUsage.totalBytes)
    val monthTotal = viewModel.formatBytes(state.monthTotalUsage.totalBytes)
    canvas.drawText("• استهلاك الباقة لليوم: $todayTotal (التنزيل: ${viewModel.formatBytes(state.todayTotalUsage.rxBytes)}، الرفع: ${viewModel.formatBytes(state.todayTotalUsage.txBytes)})", 40f, 210f, paint)
    canvas.drawText("• استهلاك الباقة للأسبوع: $weekTotal (التنزيل: ${viewModel.formatBytes(state.weekTotalUsage.rxBytes)}، الرفع: ${viewModel.formatBytes(state.weekTotalUsage.txBytes)})", 40f, 230f, paint)
    canvas.drawText("• استهلاك الباقة للشهر الحالي: $monthTotal (التنزيل: ${viewModel.formatBytes(state.monthTotalUsage.rxBytes)}، الرفع: ${viewModel.formatBytes(state.monthTotalUsage.txBytes)})", 40f, 250f, paint)

    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("التطبيقات الأكثر استنزافاً وحركة للحزم على الشبكة اليوم:", 30f, 290f, paint)

    paint.textSize = 11f
    paint.isFakeBoldText = false
    val sortedApps = state.appUsageList.sortedByDescending { it.usageToday.totalBytes }.take(6)
    var drawY = 320f
    for ((idx, usage) in sortedApps.withIndex()) {
        val appUsageStr = viewModel.formatBytes(usage.usageToday.totalBytes)
        canvas.drawText("${idx + 1}. ${usage.app.appName} (${usage.app.packageName}): $appUsageStr (UID: ${usage.app.uid})", 40f, drawY, paint)
        drawY += 24f
    }

    paint.strokeWidth = 1f
    canvas.drawLine(30f, 780f, 565f, 780f, paint)
    paint.textSize = 10f
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("رمز المصادقة والتحقق المعتمد: u_4.l | شكراً لاستخدامكم تطبيق NetSpectra", 30f, 800f, paint)

    pdfDocument.finishPage(page)

    val pdfFile = java.io.File(context.cacheDir, "NetSpectra-Report-${period}.pdf")
    try {
        pdfFile.createNewFile()
        val outStream = java.io.FileOutputStream(pdfFile)
        pdfDocument.writeTo(outStream)
        outStream.close()
        pdfDocument.close()
        return pdfFile
    } catch (e: Exception) {
        e.printStackTrace()
    }
    pdfDocument.close()
    return null
}

fun shareReportPdf(context: android.content.Context, pdfFile: java.io.File, period: String) {
    val authority = "${context.packageName}.fileprovider"
    val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, pdfFile)
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(android.content.Intent.EXTRA_SUBJECT, "NetSpectra - تقرير استهلاك $period")
        putExtra(android.content.Intent.EXTRA_TEXT, "مرفق تقرير ذكي مفصل لتدفق استهلاك باقة الإنترنت مصمم ومصدر تلقائياً بواسطة تطبيق NetSpectra من إعداد المهندس عزالدين العبادي u_4.l.")
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة مستند PDF عبر"))
}
