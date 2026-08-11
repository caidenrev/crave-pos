package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PosNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var storeName by remember { mutableStateOf("Crave Outlet") }
    var storePhone by remember { mutableStateOf("0812-3456-7890") }
    var isAppLockEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Aplikasi POS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White
                )
            )
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
            // Store Profile Settings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = "Store", tint = PosNavyPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Profil Toko UMKM", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Nama Toko") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = storePhone,
                            onValueChange = { storePhone = it },
                            label = { Text("Nomor Telepon Toko") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // General Preferences Card (Matching Reference UI Settings list)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Preferensi & Cetak", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PosNavyPrimary)

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingItemRow(
                            icon = Icons.Default.Palette,
                            title = "Tema Tampilan",
                            value = "Biru - Putih (Light Mode)"
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        SettingItemRow(
                            icon = Icons.Default.Language,
                            title = "Bahasa / Language",
                            value = "Bahasa Indonesia"
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        SettingItemRow(
                            icon = Icons.Default.Print,
                            title = "Koneksi Printer Bluetooth Thermal",
                            value = "Terhubung (58mm)"
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        SettingItemRow(
                            icon = Icons.Default.Receipt,
                            title = "Mata Uang & Format",
                            value = "IDR (Rupiah Rp)"
                        )
                    }
                }
            }

            // Security Settings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock", tint = PosNavyPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Kunci Keamanan Aplikasi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Gunakan PIN Karyawan untuk Akses Kasir", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { isAppLockEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PosNavyPrimary)
                            )
                        }
                    }
                }
            }

            // Version Info
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "KasirKu POS SaaS v2.4.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = "Solusi Pencatatan Digital UMKM Indonesia", fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = PosNavyPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}
