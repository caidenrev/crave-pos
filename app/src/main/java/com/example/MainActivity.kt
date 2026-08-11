package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.CartItem
import com.example.data.model.EmployeeRole
import com.example.data.model.Transaction
import com.example.ui.components.NotificationDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.KasirKuTheme
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.UiEvent

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object PosHome : Screen("pos_home", "Kasir", Icons.Default.Storefront)
    object Transactions : Screen("transactions", "Transaksi", Icons.Default.ReceiptLong)
    object Inventory : Screen("inventory", "Stok", Icons.Default.Inventory2)
    object DebtManagement : Screen("debt_management", "Kasbon", Icons.Default.AccountBalanceWallet)
    object Analytics : Screen("analytics", "Laporan", Icons.Default.BarChart)
    object Employee : Screen("employee", "Akses", Icons.Default.ManageAccounts)
    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
    object Checkout : Screen("checkout", "Checkout", Icons.Default.ReceiptLong)
}

class MainActivity : ComponentActivity() {
    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KasirKuTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: PosViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var activeReceiptTransaction by remember { mutableStateOf<Pair<Transaction, List<CartItem>>?>(null) }
    var activeNotificationData by remember { mutableStateOf<Pair<String, Double>?>(null) }

    // Listen to ViewModel UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowReceiptDialog -> {
                    activeReceiptTransaction = Pair(event.transaction, event.items)
                }
                is UiEvent.ShowNotificationSent -> {
                    activeNotificationData = Pair(event.customerName, event.amount)
                }
            }
        }
    }

    val bottomNavItems = remember(currentRole) {
        if (currentRole == EmployeeRole.CASHIER) {
            // Cashier role has restricted menu (no sensitive financial reports)
            listOf(
                Screen.PosHome,
                Screen.Transactions,
                Screen.DebtManagement,
                Screen.Employee
            )
        } else {
            // Full Owner / Manager Menu
            listOf(
                Screen.PosHome,
                Screen.Transactions,
                Screen.Inventory,
                Screen.DebtManagement,
                Screen.Analytics,
                Screen.Employee
            )
        }
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PosNavyPrimary,
                                    selectedTextColor = PosNavyPrimary,
                                    indicatorColor = PosNavyContainer,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PosHome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.PosHome.route) {
                PosHomeScreen(
                    viewModel = viewModel,
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    viewModel = viewModel,
                    onNewSaleClick = { navController.navigate(Screen.PosHome.route) }
                )
            }

            composable(Screen.Inventory.route) {
                InventoryScreen(viewModel = viewModel)
            }

            composable(Screen.DebtManagement.route) {
                DebtManagementScreen(viewModel = viewModel)
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }

            composable(Screen.Employee.route) {
                EmployeeScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }

    // Receipt Dialog Popup
    activeReceiptTransaction?.let { (tx, items) ->
        ReceiptDialog(
            transaction = tx,
            items = items,
            onDismiss = { activeReceiptTransaction = null },
            onPrintClick = {
                Toast.makeText(context, "Mencetak Struk ke Printer Thermal...", Toast.LENGTH_SHORT).show()
            },
            onExportPdfClick = {
                Toast.makeText(context, "Nota PDF disimpan ke /Download/KasirKu_${tx.invoiceNumber}.pdf", Toast.LENGTH_LONG).show()
                activeReceiptTransaction = null
            }
        )
    }

    // Debt Reminder Notification Simulation
    activeNotificationData?.let { (customer, amount) ->
        NotificationDialog(
            customerName = customer,
            amount = amount,
            onDismiss = { activeNotificationData = null },
            onSendPushNotification = {
                Toast.makeText(
                    context,
                    "Notifikasi Push terkirim ke HP $customer! Pengingat Kasbon ${PosViewModel.formatRupiah(amount)}",
                    Toast.LENGTH_LONG
                ).show()
                activeNotificationData = null
            }
        )
    }
}
