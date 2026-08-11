package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.data.model.EmployeeRole
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployeeName by viewModel.currentEmployeeName.collectAsState()

    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akses Karyawan & Peran", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEmployeeDialog = true },
                containerColor = PosNavyPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Employee")
            }
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
            // Role Switcher Simulator Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
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
                            Icon(Icons.Default.Shield, contentDescription = "Role", tint = PosNavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sesi Aktif Saat Ini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (currentRole == EmployeeRole.OWNER) Color(0xFFFEF3C7) else Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = currentRole.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentRole == EmployeeRole.OWNER) PosWarningOrange else PosNavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pengguna: $currentEmployeeName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PosNavyPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Beralih Peran (Demo Akses Keamanan):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.switchRole(EmployeeRole.OWNER, "M. A. Rouf (Owner)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currentRole == EmployeeRole.OWNER) PosNavyContainer else Color.Transparent
                            )
                        ) {
                            Text("Owner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.switchRole(EmployeeRole.CASHIER, "Budi Santoso (Kasir)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currentRole == EmployeeRole.CASHIER) PosNavyContainer else Color.Transparent
                            )
                        ) {
                            Text("Kasir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daftar Akses Karyawan Toko (${employees.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PosNavyPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(employees, key = { it.id }) { emp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(PosNavyContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "Staff", tint = PosNavyPrimary)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = emp.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "PIN Keamanan: **** • ${emp.phone.ifEmpty { "0812-XXXX-XXXX" }}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = when (emp.role) {
                                    EmployeeRole.OWNER -> Color(0xFFFEF3C7)
                                    EmployeeRole.MANAGER -> Color(0xFFE0F2FE)
                                    EmployeeRole.CASHIER -> Color(0xFFD1FAE5)
                                }
                            ) {
                                Text(
                                    text = emp.role.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (emp.role) {
                                        EmployeeRole.OWNER -> PosWarningOrange
                                        EmployeeRole.MANAGER -> PosNavyPrimary
                                        EmployeeRole.CASHIER -> PosSuccessGreen
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEmployeeDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddEmployeeDialog = false },
            onSave = { employee ->
                viewModel.addEmployee(employee)
                showAddEmployeeDialog = false
            }
        )
    }
}

@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var selectedRole by remember { mutableStateOf(EmployeeRole.CASHIER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Karyawan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Karyawan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WA") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN Keamanan (4 Digit)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Pilih Peran / Jabatan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EmployeeRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.name, fontSize = 11.sp) },
                            shape = CircleShape
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(Employee(name = name, phone = phone, pin = pin, role = selectedRole))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
