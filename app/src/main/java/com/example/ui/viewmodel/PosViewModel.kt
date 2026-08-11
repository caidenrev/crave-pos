package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PosDatabase
import com.example.data.model.*
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowReceiptDialog(val transaction: Transaction, val items: List<CartItem>) : UiEvent()
    data class ShowNotificationSent(val customerName: String, val amount: Double) : UiEvent()
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PosRepository

    init {
        val database = PosDatabase.getDatabase(application)
        repository = PosRepository(database)
    }

    // Role state (Kasir vs Owner/Manager)
    private val _currentRole = MutableStateFlow(EmployeeRole.OWNER)
    val currentRole: StateFlow<EmployeeRole> = _currentRole.asStateFlow()

    private val _currentEmployeeName = MutableStateFlow("M. A. Rouf (Owner)")
    val currentEmployeeName: StateFlow<String> = _currentEmployeeName.asStateFlow()

    fun switchRole(newRole: EmployeeRole, name: String) {
        _currentRole.value = newRole
        _currentEmployeeName.value = name
    }

    // Products & Filtering
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val products: StateFlow<List<Product>> = combine(
        _selectedCategory.flatMapLatest { cat -> repository.getProductsByCategory(cat) },
        _searchQuery
    ) { productList, query ->
        if (query.isBlank()) {
            productList
        } else {
            productList.filter {
                it.name.contains(query, ignoreCase = true) || it.barcode.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerDebts: StateFlow<List<CustomerDebt>> = repository.allCustomerDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Events
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("Stok barang habis! Silakan lakukan restok."))
            }
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            if (existing.quantity < product.stock) {
                currentList[index] = existing.copy(quantity = existing.quantity + 1)
            } else {
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowToast("Batas stok tercapai (${product.stock} ${product.unit})"))
                }
            }
        } else {
            currentList.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty <= item.product.stock) {
                currentList[index] = item.copy(quantity = newQty)
            } else {
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowToast("Batas stok tercapai (${item.product.stock})"))
                }
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode)
            if (product != null) {
                addToCart(product)
                _uiEvents.emit(UiEvent.ShowToast("Barcode terdeteksi: ${product.name}"))
            } else {
                _uiEvents.emit(UiEvent.ShowToast("Barang dengan barcode $barcode tidak ditemukan"))
            }
        }
    }

    // Checkout execution
    fun processCheckout(
        paymentMode: PaymentMode,
        customerName: String,
        customerPhone: String,
        discountAmount: Double = 0.0,
        taxAmount: Double = 0.0,
        extraCharges: Double = 0.0,
        notes: String = ""
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val subtotal = items.sumOf { it.subtotal }
        val finalTotal = (subtotal - discountAmount + taxAmount + extraCharges).coerceAtLeast(0.0)

        val timestamp = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date(timestamp))
        val invNo = "INV-$dateStr-${(100..999).random()}"

        val paymentStatus = if (paymentMode == PaymentMode.KASBON) {
            PaymentStatus.KASBON_PENDING
        } else {
            PaymentStatus.LUNAS
        }

        val transaction = Transaction(
            invoiceNumber = invNo,
            timestamp = timestamp,
            totalAmount = finalTotal,
            paymentMode = paymentMode,
            paymentStatus = paymentStatus,
            customerName = customerName.ifBlank { "Pelanggan Umum" },
            customerPhone = customerPhone,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            extraCharges = extraCharges,
            cashierName = _currentEmployeeName.value,
            notes = notes
        )

        viewModelScope.launch {
            repository.createTransaction(transaction, items)
            _uiEvents.emit(UiEvent.ShowReceiptDialog(transaction, items))
            _uiEvents.emit(UiEvent.ShowToast("Transaksi berhasil disimpan!"))
            clearCart()
        }
    }

    // Debt Payment
    fun recordDebtPayment(debt: CustomerDebt, amount: Double) {
        viewModelScope.launch {
            repository.recordDebtPayment(debt.id, amount)
            _uiEvents.emit(UiEvent.ShowToast("Pembayaran Rp ${formatRupiah(amount)} berhasil dicatat!"))
        }
    }

    fun sendDebtNotificationReminder(debt: CustomerDebt) {
        viewModelScope.launch {
            _uiEvents.emit(
                UiEvent.ShowNotificationSent(
                    customerName = debt.customerName,
                    amount = debt.remainingDebt
                )
            )
        }
    }

    // Product & Inventory actions
    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
            _uiEvents.emit(UiEvent.ShowToast("Barang ${product.name} berhasil ditambahkan"))
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _uiEvents.emit(UiEvent.ShowToast("Data barang ${product.name} diperbarui"))
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _uiEvents.emit(UiEvent.ShowToast("Barang ${product.name} berhasil dihapus"))
        }
    }

    fun restockProduct(productId: Long, qty: Int) {
        viewModelScope.launch {
            repository.addStock(productId, qty)
            _uiEvents.emit(UiEvent.ShowToast("Stok berhasil ditambah +$qty"))
        }
    }

    // Staff / Employee Management
    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployee(employee)
            _uiEvents.emit(UiEvent.ShowToast("Karyawan ${employee.name} berhasil ditambahkan"))
        }
    }

    companion object {
        fun formatRupiah(number: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(number).replace("Rp", "Rp ").replace(",00", "")
        }
    }
}
