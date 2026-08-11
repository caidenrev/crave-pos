package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel

@Composable
fun NotificationDialog(
    customerName: String,
    amount: Double,
    onDismiss: () -> Unit,
    onSendPushNotification: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PosWarningOrange.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification",
                        tint = PosWarningOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kirim Pengingat Jatuh Tempo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosNavyPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sistem akan mengalirkan Notifikasi Push & Pesan Pengingat Pembayaran Kasbon kepada $customerName sebesar ${PosViewModel.formatRupiah(amount)}.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSendPushNotification()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim Push Notification Sekarang", fontSize = 13.sp)
                }
            }
        }
    }
}
