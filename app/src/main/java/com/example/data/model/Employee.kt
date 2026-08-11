package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EmployeeRole {
    OWNER, MANAGER, CASHIER
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: EmployeeRole,
    val pin: String = "1234",
    val phone: String = "",
    val isActive: Boolean = true,
    val avatarUrl: String = ""
)
