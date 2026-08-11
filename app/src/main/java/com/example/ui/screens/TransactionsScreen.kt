package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.data.model.Transaction
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: PosViewModel,
    onNewSaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Kasbon", "Lunas"
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(allTransactions, selectedFilter, searchQuery) {
        allTransactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                "Kasbon" -> tx.paymentStatus == PaymentStatus.KASBON_PENDING
                "Lunas" -> tx.paymentStatus == PaymentStatus.LUNAS
                else -> true
            }
            val matchesSearch = tx.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    tx.customerName.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Quick Transaction Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNewSaleClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PosNavyPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Sale", tint = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Penjualan Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Payment", tint = PosNavyPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Terima Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PosNavyPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nomor nota atau nama pelanggan...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Kasbon", "Lunas").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        shape = CircleShape
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Belum ada transaksi recorded", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            transaction = tx
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: Transaction
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID")).format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = transaction.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PosNavyPrimary
                    )
                    Text(
                        text = "${transaction.customerName} • ${transaction.paymentMode.name}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (transaction.paymentStatus == PaymentStatus.LUNAS) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (transaction.paymentStatus == PaymentStatus.LUNAS) "LUNAS" else "KASBON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.paymentStatus == PaymentStatus.LUNAS) PosSuccessGreen else PosWarningOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateStr, fontSize = 11.sp, color = Color.LightGray)

                Text(
                    text = PosViewModel.formatRupiah(transaction.totalAmount),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}
