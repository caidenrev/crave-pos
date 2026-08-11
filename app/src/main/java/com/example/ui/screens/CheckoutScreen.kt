package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.PaymentMode
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val discountVal = discountInput.toDoubleOrNull() ?: 0.0
    val finalTotal = (subtotal - discountVal).coerceAtLeast(0.0)

    val todayDateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Penjualan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Pembayaran", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = PosViewModel.formatRupiah(finalTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PosNavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.processCheckout(
                                paymentMode = selectedPaymentMode,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                discountAmount = discountVal,
                                notes = notesInput
                            )
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary),
                        enabled = cartItems.isNotEmpty()
                    ) {
                        Text(
                            text = if (selectedPaymentMode == PaymentMode.KASBON) "Simpan Kasbon Pelanggan" else "Simpan & Cetak Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
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
            // Invoice Metadata Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Status Transaksi", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = if (selectedPaymentMode == PaymentMode.KASBON) "KASBON / BELUM LUNAS" else "LUNAS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedPaymentMode == PaymentMode.KASBON) PosWarningOrange else PosSuccessGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Tanggal Nota", fontSize = 11.sp, color = Color.Gray)
                                Text(text = todayDateStr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Payment Mode Selector
            item {
                Text(
                    text = "Metode Pembayaran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PosNavyPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentModeChip(
                        title = "Cash (Tunai)",
                        icon = Icons.Default.Money,
                        isSelected = selectedPaymentMode == PaymentMode.CASH,
                        onClick = { selectedPaymentMode = PaymentMode.CASH },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentModeChip(
                        title = "QRIS",
                        icon = Icons.Default.QrCode,
                        isSelected = selectedPaymentMode == PaymentMode.QRIS,
                        onClick = { selectedPaymentMode = PaymentMode.QRIS },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentModeChip(
                        title = "E-Wallet",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedPaymentMode == PaymentMode.EWALLET,
                        onClick = { selectedPaymentMode = PaymentMode.EWALLET },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentModeChip(
                        title = "Kasbon / Utang",
                        icon = Icons.Default.Receipt,
                        isSelected = selectedPaymentMode == PaymentMode.KASBON,
                        onClick = { selectedPaymentMode = PaymentMode.KASBON },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Customer Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informasi Pelanggan ${if (selectedPaymentMode == PaymentMode.KASBON) "(Wajib)" else "(Opsional)"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Nama Pelanggan") },
                            placeholder = { Text("e.g. Ibu Halimah / Pak Rouf") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Customer") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("No. HP / WA (Untuk Pengingat Kasbon)") },
                            placeholder = { Text("e.g. 081234567890") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Cart Items Summary
            item {
                Text(
                    text = "Daftar Barang Belanja (${cartItems.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PosNavyPrimary
                )
            }

            items(cartItems, key = { it.product.id }) { cartItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cartItem.product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${PosViewModel.formatRupiah(cartItem.product.sellPrice)} / ${cartItem.product.unit}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateCartQuantity(cartItem.product.id, -1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PosNavyContainer, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", tint = PosNavyPrimary, modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "${cartItem.quantity}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { viewModel.updateCartQuantity(cartItem.product.id, 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PosNavyPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Additional Charges & Discount
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal Barang", color = Color.Gray, fontSize = 13.sp)
                            Text(text = PosViewModel.formatRupiah(subtotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Potongan Potongan / Diskon (Rp)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Catatan / Remarks") },
                            placeholder = { Text("e.g. Jangan terlalu pedas") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentModeChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PosNavyPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) PosNavyPrimary else Color(0xFFE2E8F0)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else PosNavyPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF0F172A)
            )
        }
    }
}
