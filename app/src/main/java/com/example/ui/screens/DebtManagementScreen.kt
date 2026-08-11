package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerDebt
import com.example.ui.components.NotificationDialog
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtManagementScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.customerDebts.collectAsState()

    var selectedDebtForPayoff by remember { mutableStateOf<CustomerDebt?>(null) }
    var selectedDebtForNotification by remember { mutableStateOf<CustomerDebt?>(null) }

    val totalUnsettledDebt = debts.filter { !it.isSettled }.sumOf { it.remainingDebt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencatatan Utang / Kasbon", fontWeight = FontWeight.Bold) },
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
            // Total Outstanding Debt Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PosWarningOrange)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Kasbon Pelanggan Belum Lunas",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = PosViewModel.formatRupiah(totalUnsettledDebt),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Kasbon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daftar Piutang Pelanggan (${debts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PosNavyPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (debts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Tidak ada catatan kasbon pelanggan", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(debts, key = { it.id }) { debt ->
                        val dueDateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(debt.dueDateTimestamp))
                        val isOverdue = System.currentTimeMillis() > debt.dueDateTimestamp && !debt.isSettled

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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(PosNavyContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = "User", tint = PosNavyPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = debt.customerName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = debt.phone, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (debt.isSettled) Color(0xFFD1FAE5) else if (isOverdue) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = if (debt.isSettled) "LUNAS" else if (isOverdue) "JATUH TEMPO" else "AKTIF",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (debt.isSettled) PosSuccessGreen else if (isOverdue) Color(0xFFDC2626) else PosWarningOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Jatuh Tempo: $dueDateStr", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = "Sisa: ${PosViewModel.formatRupiah(debt.remainingDebt)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (debt.isSettled) PosSuccessGreen else PosWarningOrange
                                    )
                                }

                                if (debt.notes.isNotBlank()) {
                                    Text(
                                        text = "Catatan: ${debt.notes}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                if (!debt.isSettled) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedDebtForNotification = debt },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "Remind", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ingatkan", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { selectedDebtForPayoff = debt },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary)
                                        ) {
                                            Text("Bayar Cicilan", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedDebtForPayoff?.let { debt ->
        PayoffDialog(
            debt = debt,
            onDismiss = { selectedDebtForPayoff = null },
            onPay = { amount ->
                viewModel.recordDebtPayment(debt, amount)
                selectedDebtForPayoff = null
            }
        )
    }

    selectedDebtForNotification?.let { debt ->
        NotificationDialog(
            customerName = debt.customerName,
            amount = debt.remainingDebt,
            onDismiss = { selectedDebtForNotification = null },
            onSendPushNotification = {
                viewModel.sendDebtNotificationReminder(debt)
                selectedDebtForNotification = null
            }
        )
    }
}

@Composable
fun PayoffDialog(
    debt: CustomerDebt,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf(debt.remainingDebt.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bayar Kasbon ${debt.customerName}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Sisa piutang: ${PosViewModel.formatRupiah(debt.remainingDebt)}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Jumlah Pembayaran (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onPay(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary)
            ) {
                Text("Simpan Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
