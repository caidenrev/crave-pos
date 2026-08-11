package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.ui.components.ExportDialog
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val debts by viewModel.customerDebts.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    val totalLunas = transactions.filter { it.paymentStatus == PaymentStatus.LUNAS }.sumOf { it.totalAmount }
    val totalKasbon = transactions.filter { it.paymentStatus == PaymentStatus.KASBON_PENDING }.sumOf { it.totalAmount } + debts.filter { !it.isSettled }.sumOf { it.remainingDebt }
    val totalCount = transactions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Performance Sales", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Action export bar matching the exact design in the uploaded image reference!
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExportActionPill(
                        title = "Ekspor Excel",
                        icon = Icons.Default.GridOn,
                        onClick = { showExportDialog = true }
                    )
                    ExportActionPill(
                        title = "Cetak PDF",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = { showExportDialog = true }
                    )
                    ExportActionPill(
                        title = "Download",
                        icon = Icons.Default.Download,
                        onClick = { showExportDialog = true }
                    )
                    ExportActionPill(
                        title = "Bagikan",
                        icon = Icons.Default.Share,
                        onClick = { showExportDialog = true }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Analytics Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PosNavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Omset Penjualan",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = "Trend", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+18.5% vs bulan lalu", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = PosViewModel.formatRupiah(totalLunas + totalKasbon),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Uang Lunas Masuk", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = PosViewModel.formatRupiah(totalLunas),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Kasbon Pelanggan", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = PosViewModel.formatRupiah(totalKasbon),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE047)
                                )
                            }
                        }
                    }
                }
            }

            // Sales Chart Breakdown Simulation Visualizer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Grafik Performa Penjualan Harian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PosNavyPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            listOf(
                                "Sen" to 0.4f,
                                "Sel" to 0.7f,
                                "Rab" to 0.5f,
                                "Kam" to 0.85f,
                                "Jum" to 0.6f,
                                "Sab" to 0.95f,
                                "Min" to 0.9f
                            ).forEach { (day, fraction) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(fraction)
                                            .background(
                                                color = if (fraction > 0.8f) PosNavyPrimary else PosNavyContainer,
                                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = day, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction Summary Statistics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Total Transaksi", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "$totalCount Transaksi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PosNavyPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Pelanggan Kasbon", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "${debts.size} Orang", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PosWarningOrange)
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportExcel = { },
            onExportPdf = { },
            onShareReport = { }
        )
    }
}

@Composable
fun ExportActionPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(PosNavyContainer, CircleShape)
                .size(42.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PosNavyPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
    }
}
