package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String,
    val category: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val minStockAlert: Int = 5,
    val imageUrl: String = "",
    val unit: String = "Pcs"
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() {
            val basePrice = product.sellPrice * quantity
            return basePrice * (1.0 - (discountPercent / 100.0))
        }
}
