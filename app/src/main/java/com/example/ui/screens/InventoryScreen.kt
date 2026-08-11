package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.theme.PosErrorRed
import com.example.ui.theme.PosNavyContainer
import com.example.ui.theme.PosNavyPrimary
import com.example.ui.theme.PosWarningOrange
import com.example.ui.viewmodel.PosViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    var showFormDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var selectedProductForRestock by remember { mutableStateOf<Product?>(null) }
    var selectedProductForDelete by remember { mutableStateOf<Product?>(null) }

    val displayedProducts = remember(products, lowStockProducts, searchQuery, filterLowStockOnly) {
        val list = if (filterLowStockOnly) lowStockProducts else products
        if (searchQuery.isBlank()) list else list.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery) || it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stok & Inventaris Barang", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PosNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedProductForEdit = null
                    showFormDialog = true
                },
                containerColor = PosNavyPrimary,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Tambah Barang", fontWeight = FontWeight.Bold) },
                shape = CircleShape
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            // Low Stock Warning Banner if any
            if (lowStockProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = PosWarningOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Peringatan Stok Menipis!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PosWarningOrange
                            )
                            Text(
                                text = "${lowStockProducts.size} produk berada di bawah batas stok minimum.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                        FilterChip(
                            selected = filterLowStockOnly,
                            onClick = { filterLowStockOnly = !filterLowStockOnly },
                            label = { Text(if (filterLowStockOnly) "Semua" else "Filter", fontSize = 11.sp) },
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari barang, barcode, atau kategori...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada data produk", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayedProducts, key = { it.id }) { product ->
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
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Image Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PosNavyContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (product.imageUrl.isNotBlank()) {
                                        val imageModel: Any = if (product.imageUrl.startsWith("/")) File(product.imageUrl) else product.imageUrl
                                        AsyncImage(
                                            model = imageModel,
                                            contentDescription = product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "No Image",
                                            tint = PosNavyPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Product Info
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PosNavyContainer
                                        ) {
                                            Text(
                                                text = product.category,
                                                fontSize = 9.sp,
                                                color = PosNavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "Barcode: ${product.barcode}",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "Beli: ${PosViewModel.formatRupiah(product.buyPrice)} • Jual: ${PosViewModel.formatRupiah(product.sellPrice)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PosNavyPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Stock & Action Buttons
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (product.stock <= product.minStockAlert) Color(0xFFFEE2E2) else Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = "Stok: ${product.stock} ${product.unit}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (product.stock <= product.minStockAlert) Color(0xFFDC2626) else PosNavyPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Edit Button
                                        IconButton(
                                            onClick = {
                                                selectedProductForEdit = product
                                                showFormDialog = true
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(PosNavyContainer, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Produk",
                                                tint = PosNavyPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Restock Button
                                        Button(
                                            onClick = { selectedProductForRestock = product },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("+ Stok", fontSize = 11.sp)
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

    // Add or Edit Product Full-Screen White Page
    if (showFormDialog) {
        ProductFormFullScreen(
            productToEdit = selectedProductForEdit,
            onDismiss = {
                showFormDialog = false
                selectedProductForEdit = null
            },
            onSave = { product ->
                if (selectedProductForEdit != null) {
                    viewModel.updateProduct(product)
                } else {
                    viewModel.addProduct(product)
                }
                showFormDialog = false
                selectedProductForEdit = null
            },
            onDelete = { product ->
                selectedProductForDelete = product
                showFormDialog = false
                selectedProductForEdit = null
            }
        )
    }

    // Restock Dialog
    selectedProductForRestock?.let { product ->
        RestockDialog(
            product = product,
            onDismiss = { selectedProductForRestock = null },
            onRestock = { qty ->
                viewModel.restockProduct(product.id, qty)
                selectedProductForRestock = null
            }
        )
    }

    // Delete Confirmation Dialog
    selectedProductForDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedProductForDelete = null },
            title = { Text("Hapus Produk", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = { Text("Apakah Anda yakin ingin menghapus '${product.name}' dari daftar stok?", color = Color(0xFF475569)) },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        selectedProductForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosErrorRed)
                ) {
                    Text("Ya, Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForDelete = null }) { Text("Batal", color = Color.Gray) }
            }
        )
    }
}

/**
 * Full Page White Screen for Add / Edit Product with Local Storage Image Upload Support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormFullScreen(
    productToEdit: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "${(1000000000..9999999999).random()}") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "Makanan") }
    var buyPriceInput by remember { mutableStateOf(productToEdit?.buyPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var sellPriceInput by remember { mutableStateOf(productToEdit?.sellPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var stockInput by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "10") }
    var unitInput by remember { mutableStateOf(productToEdit?.unit ?: "Pcs") }
    var minStockInput by remember { mutableStateOf(productToEdit?.minStockAlert?.toString() ?: "5") }
    
    // Store local file path or image path
    var imageUrlInput by remember { mutableStateOf(productToEdit?.imageUrl ?: "") }

    // Launcher for selecting local image from device storage
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = saveImageToLocalStorage(context, it)
            if (localPath.isNotBlank()) {
                imageUrlInput = localPath
            }
        }
    }

    val categories = listOf("Makanan", "Minuman", "Snacks", "Burger", "Shakes", "Bakery", "Sembako")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Makes it true Full Screen!
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (productToEdit != null) "Edit Produk" else "Tambah Produk Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        if (productToEdit != null) {
                            IconButton(onClick = { onDelete(productToEdit) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Produk",
                                    tint = PosErrorRed
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val productToSave = Product(
                                        id = productToEdit?.id ?: 0,
                                        name = name,
                                        barcode = barcode.ifBlank { "${(1000000000..9999999999).random()}" },
                                        category = category.ifBlank { "Umum" },
                                        buyPrice = buyPriceInput.toDoubleOrNull() ?: 0.0,
                                        sellPrice = sellPriceInput.toDoubleOrNull() ?: 0.0,
                                        stock = stockInput.toIntOrNull() ?: 0,
                                        unit = unitInput.ifBlank { "Pcs" },
                                        minStockAlert = minStockInput.toIntOrNull() ?: 5,
                                        imageUrl = imageUrlInput
                                    )
                                    onSave(productToSave)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Simpan", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF0F172A),
                        navigationIconContentColor = Color(0xFF0F172A)
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Local Image Upload Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Foto Produk (Penyimpanan Lokal Device)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Image Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PosNavyContainer)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrlInput.isNotBlank()) {
                                val imageModel: Any = if (imageUrlInput.startsWith("/")) File(imageUrlInput) else imageUrlInput
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Preview Produk",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Upload Foto",
                                        tint = PosNavyPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ketuk untuk Memilih Foto dari Galeri",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PosNavyPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeri", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih Foto", fontSize = 12.sp)
                            }

                            if (imageUrlInput.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { imageUrlInput = "" },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PosErrorRed)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus Foto", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hapus Foto", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Foto disimpan secara 100% offline di penyimpanan lokal perangkat.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Section 2: Product Info Fields
                Text("Informasi Barang", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PosNavyPrimary)

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang") },
                    placeholder = { Text("Contoh: Nasi Goreng Spesial") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Barcode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Kode Barcode / SKU") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { barcode = "${(1000000000..9999999999).random()}" },
                        modifier = Modifier
                            .background(PosNavyContainer, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generate Barcode", tint = PosNavyPrimary)
                    }
                }

                // Category Selection Chips
                Text("Kategori Barang:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF334155))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = CircleShape
                        )
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori Custom") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(color = Color(0xFFE2E8F0))

                // Section 3: Pricing & Inventory
                Text("Harga & Stok", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PosNavyPrimary)

                // Prices
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = buyPriceInput,
                        onValueChange = { buyPriceInput = it },
                        label = { Text("Harga Beli Modal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellPriceInput,
                        onValueChange = { sellPriceInput = it },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Stock & Unit
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Stok Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Satuan (Pcs/Kg/..)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Min Stock Alert
                OutlinedTextField(
                    value = minStockInput,
                    onValueChange = { minStockInput = it },
                    label = { Text("Batas Peringatan Stok Menipis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Helper to save selectedUri into app's internal filesDir/product_images/ folder
 */
fun saveImageToLocalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val imagesDir = File(context.filesDir, "product_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, "prod_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

@Composable
fun RestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onRestock: (Int) -> Unit
) {
    var qtyInput by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restok ${product.name}", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
        containerColor = Color.White,
        text = {
            Column {
                Text("Masukkan jumlah stok tambahan:", color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { qtyInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyInput.toIntOrNull() ?: 0
                    if (qty > 0) onRestock(qty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PosNavyPrimary)
            ) {
                Text("Tambah Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}
