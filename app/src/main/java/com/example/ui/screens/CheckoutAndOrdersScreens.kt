package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.ShippingAddress
import com.example.data.model.UserProfile
import com.example.data.model.VoucherPromo
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueSoft
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.UiState

@Composable
fun CheckoutScreen(
  items: List<CartItem>,
  isFromCart: Boolean,
  addresses: List<ShippingAddress>,
  profile: UserProfile?,
  selectedVoucher: VoucherPromo?,
  isBusy: Boolean,
  onAddAddress: (String, String, String, String, String, Boolean, () -> Unit) -> Unit,
  onPlaceOrder: (List<CartItem>, Boolean, ShippingAddress, String, Long, VoucherPromo?, Boolean, String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val defaultAddress =
    remember(addresses) { addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull() }
  var chosenAddress by remember(defaultAddress) { mutableStateOf(defaultAddress) }
  var showAddressDialog by remember { mutableStateOf(false) }

  val shippingOptions =
    remember {
      listOf(
        "Reguler 1-2 Jam" to 5_000L,
        "Kurir Instan 30 Menit" to 12_000L,
        "Ambil di Toko DMYmart" to 0L,
      )
    }
  var selectedShipping by remember { mutableStateOf(shippingOptions.first()) }

  val paymentMethods =
    remember {
      listOf(
        "QRIS",
        "DMYmartPay",
        "COD (Bayar di Tempat)",
        "Transfer Bank BCA/BRI",
      )
    }
  var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }
  var useCoins by remember { mutableStateOf(false) }

  val subtotal = items.sumOf { it.price * it.quantity }
  val shippingCost = selectedShipping.second
  val voucherDiscount = selectedVoucher?.calculateDiscount(subtotal, shippingCost) ?: 0L
  val availableCoins = profile?.coins ?: 0L
  val coinsUsed =
    if (useCoins) minOf(availableCoins, maxOf(0L, subtotal + shippingCost - voucherDiscount), 10_000L)
    else 0L
  val finalTotal = maxOf(0L, subtotal + shippingCost - voucherDiscount - coinsUsed)

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Kembali",
            tint = Color.White,
          )
        }
        Text(
          text = "Checkout Pesanan",
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Column(
      modifier =
        Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Address Section
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = ShopeeOrange,
              )
              Spacer(Modifier.width(6.dp))
              Text(
                text = "Alamat Pengiriman",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            TextButton(onClick = { showAddressDialog = true }) {
              Text(
                text = if (chosenAddress == null) "+ Tambah Alamat" else "Ubah / Tambah",
                color = ShopeeOrange,
              )
            }
          }
          if (chosenAddress != null) {
            val addr = chosenAddress!!
            Text(
              text = "${addr.label} • ${addr.recipientName} (${addr.phone})",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = addr.fullAddress,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          } else {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = ShopeeOrangeSoft,
              modifier = Modifier.fillMaxWidth().clickable { showAddressDialog = true },
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Rounded.AddLocationAlt, contentDescription = null, tint = ShopeeOrange)
                Spacer(Modifier.width(8.dp))
                Text(
                  text = "Belum ada alamat tersimpan. Ketuk untuk menambah alamat.",
                  style = MaterialTheme.typography.bodySmall,
                  color = ShopeeOrange,
                )
              }
            }
          }
        }
      }

      // Items Section
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Produk Dipesan (${items.sumOf { it.quantity }} item)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Spacer(Modifier.height(10.dp))
          items.forEachIndexed { idx, item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
              ProductThumbnail(
                imageKey = item.productImageKey,
                contentDescription = item.productName,
                modifier = Modifier.size(52.dp),
                iconSize = 24.dp,
                cornerRadius = 8.dp,
              )
              Spacer(Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.productName,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = "${item.unit} • ${item.quantity}x ${formatRupiah(item.price)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = formatRupiah(item.price * item.quantity),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            if (idx < items.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          }
        }
      }

      // Shipping Options
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = EmeraldVoucher)
            Spacer(Modifier.width(6.dp))
            Text(
              text = "Opsi Pengiriman",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(Modifier.height(8.dp))
          shippingOptions.forEach { option ->
            val selected = selectedShipping == option
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedShipping = option }
                  .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(
                selected = selected,
                onClick = { selectedShipping = option },
                colors = RadioButtonDefaults.colors(selectedColor = ShopeeOrange),
              )
              Text(option.first, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
              Text(
                text = if (option.second == 0L) "Gratis" else formatRupiah(option.second),
                style = MaterialTheme.typography.labelLarge,
                color = if (option.second == 0L) EmeraldVoucher else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }

      // Voucher & Coins
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, tint = ShopeeOrange)
              Spacer(Modifier.width(8.dp))
              Text(
                text = selectedVoucher?.code ?: "Tanpa Voucher",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }
            Text(
              text =
                if (voucherDiscount > 0L) "-${formatRupiah(voucherDiscount)}"
                else "Tidak memenuhi min.",
              style = MaterialTheme.typography.labelLarge,
              color = if (voucherDiscount > 0L) EmeraldVoucher else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.MonetizationOn, contentDescription = null, tint = AmberFlash)
              Spacer(Modifier.width(8.dp))
              Column {
                Text("Tukar Koin DMYmart", style = MaterialTheme.typography.bodyMedium)
                Text(
                  text = "Tersedia $availableCoins Koin",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Switch(
              checked = useCoins,
              onCheckedChange = { useCoins = it },
              enabled = availableCoins > 0L,
            )
          }
        }
      }

      // Payment Method
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Payments, contentDescription = null, tint = ShopeeOrange)
            Spacer(Modifier.width(6.dp))
            Text(
              text = "Metode Pembayaran",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(Modifier.height(6.dp))
          paymentMethods.forEach { method ->
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedPayment = method }
                  .padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(
                selected = selectedPayment == method,
                onClick = { selectedPayment = method },
                colors = RadioButtonDefaults.colors(selectedColor = ShopeeOrange),
              )
              Column {
                Text(method, style = MaterialTheme.typography.bodyMedium)
                if (method == "DMYmartPay") {
                  Text(
                    text = "Saldo: ${formatRupiah(profile?.walletBalance ?: 250_000L)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShopeeOrange,
                  )
                }
              }
            }
          }
        }
      }

      // Summary
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Rincian Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          SummaryLine("Subtotal Produk", formatRupiah(subtotal))
          SummaryLine("Ongkos Kirim", formatRupiah(shippingCost))
          if (voucherDiscount > 0L) {
            SummaryLine("Diskon Voucher", "-${formatRupiah(voucherDiscount)}", EmeraldVoucher)
          }
          if (coinsUsed > 0L) {
            SummaryLine("Potongan Koin", "-${formatRupiah(coinsUsed)}", AmberFlash)
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              text = formatRupiah(finalTotal),
              style = MaterialTheme.typography.titleLarge,
              color = ShopeeOrange,
              fontWeight = FontWeight.ExtraBold,
            )
          }
        }
      }
    }

    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 10.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text("Total Pembayaran", style = MaterialTheme.typography.labelSmall)
          Text(
            text = formatRupiah(finalTotal),
            style = MaterialTheme.typography.titleLarge,
            color = ShopeeOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        Button(
          onClick = {
            val addr =
              chosenAddress
                ?: ShippingAddress(
                  label = "Alamat Utama",
                  recipientName = profile?.displayName ?: "Pembeli DMYmart",
                  phone = "081234567890",
                  fullAddress = "Minimarket Pickup / Alamat Belum Diatur",
                )
            onPlaceOrder(
              items,
              isFromCart,
              addr,
              selectedShipping.first,
              shippingCost,
              selectedVoucher,
              useCoins,
              selectedPayment,
            )
          },
          enabled = !isBusy && chosenAddress != null,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
          modifier = Modifier.height(50.dp).testTag("place_order_button"),
        ) {
          if (isBusy) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
          } else {
            Text("Buat Pesanan", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  if (showAddressDialog) {
    AddressPickerAndCreatorDialog(
      addresses = addresses,
      defaultRecipient = profile?.displayName ?: "",
      onSelectAddress = {
        chosenAddress = it
        showAddressDialog = false
      },
      onAddNewAddress = { label, name, phone, full ->
        onAddAddress(label, name, phone, full, "", true) {
          chosenAddress =
            ShippingAddress(
              label = label,
              recipientName = name,
              phone = phone,
              fullAddress = full,
              isDefault = true,
            )
          showAddressDialog = false
        }
      },
      onDismiss = { showAddressDialog = false },
    )
  }
}

@Composable
private fun SummaryLine(
  label: String,
  value: String,
  valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun AddressPickerAndCreatorDialog(
  addresses: List<ShippingAddress>,
  defaultRecipient: String,
  onSelectAddress: (ShippingAddress) -> Unit,
  onAddNewAddress: (String, String, String, String) -> Unit,
  onDismiss: () -> Unit,
) {
  var creating by remember(addresses) { mutableStateOf(addresses.isEmpty()) }
  var label by remember { mutableStateOf("Rumah") }
  var name by remember { mutableStateOf(defaultRecipient) }
  var phone by remember { mutableStateOf("") }
  var fullAddress by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (creating) "Tambah Alamat Pengiriman" else "Pilih Alamat Pengiriman")
    },
    text = {
      if (creating) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label (Rumah/Kantor)") },
            singleLine = true,
          )
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Penerima") },
            singleLine = true,
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("No. HP (min 5 digit)") },
            singleLine = true,
          )
          OutlinedTextField(
            value = fullAddress,
            onValueChange = { fullAddress = it },
            label = { Text("Alamat Lengkap") },
            minLines = 2,
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          addresses.forEach { addr ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth().clickable { onSelectAddress(addr) },
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("${addr.label} • ${addr.recipientName}", fontWeight = FontWeight.Bold)
                Text(addr.phone, style = MaterialTheme.typography.bodySmall)
                Text(addr.fullAddress, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
          TextButton(onClick = { creating = true }) {
            Text("+ Tambah Alamat Baru", color = ShopeeOrange)
          }
        }
      }
    },
    confirmButton = {
      if (creating) {
        Button(
          onClick = { onAddNewAddress(label, name, phone, fullAddress) },
          enabled = name.isNotBlank() && phone.length >= 5 && fullAddress.length >= 5,
          colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
        ) {
          Text("Simpan", color = Color.White)
        }
      } else {
        TextButton(onClick = onDismiss) { Text("Tutup") }
      }
    },
    dismissButton = {
      if (creating && addresses.isNotEmpty()) {
        TextButton(onClick = { creating = false }) { Text("Kembali") }
      }
    },
  )
}

@Composable
fun OrdersScreen(
  ordersState: UiState<List<Order>>,
  onAdvanceStatus: (Order, String) -> Unit,
  onReorder: (Order) -> Unit,
  onGoShopping: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tabs =
    remember {
      listOf(
        OrderStatus.ALL,
        OrderStatus.BELUM_BAYAR,
        OrderStatus.DIKEMAS,
        OrderStatus.DIKIRIM,
        OrderStatus.SELESAI,
        OrderStatus.DIBATALKAN,
      )
    }
  var selectedTab by remember { mutableStateOf(OrderStatus.ALL) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = "Pesanan Saya",
          style = MaterialTheme.typography.headlineSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Pantau status pengemasan & pengiriman minimarket Anda",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.88f),
        )
      }
    }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(tabs) { status ->
          val active = selectedTab == status
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (active) ShopeeOrangeSoft else MaterialTheme.colorScheme.surfaceVariant,
            modifier =
              Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { selectedTab = status },
          ) {
            Text(
              text = OrderStatus.label(status),
              style = MaterialTheme.typography.labelLarge,
              color = if (active) ShopeeOrange else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            )
          }
        }
      }
    }

    when (ordersState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = ShopeeOrange)
        }
      }
      is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(ordersState.message, color = MaterialTheme.colorScheme.error)
        }
      }
      is UiState.Success -> {
        val filtered =
          remember(ordersState.data, selectedTab) {
            if (selectedTab == OrderStatus.ALL) ordersState.data
            else ordersState.data.filter { it.status == selectedTab }
          }

        if (filtered.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Rounded.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
              )
              Spacer(Modifier.height(10.dp))
              Text(
                text = "Belum ada pesanan di tab ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Spacer(Modifier.height(12.dp))
              Button(
                onClick = onGoShopping,
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
              ) {
                Text("Belanja Sekarang", color = Color.White)
              }
            }
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            items(filtered, key = { it.id }) { order ->
              OrderCard(
                order = order,
                onAdvanceStatus = onAdvanceStatus,
                onReorder = onReorder,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OrderCard(
  order: Order,
  onAdvanceStatus: (Order, String) -> Unit,
  onReorder: (Order) -> Unit,
) {
  val (badgeBg, badgeText) =
    when (order.status) {
      OrderStatus.BELUM_BAYAR -> AmberSoft to AmberFlash
      OrderStatus.DIKEMAS -> ShopeeOrangeSoft to ShopeeOrange
      OrderStatus.DIKIRIM -> InfoBlueSoft to InfoBlue
      OrderStatus.SELESAI -> EmeraldSoft to EmeraldVoucher
      else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = order.orderNumber,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
          Text(
            text = OrderStatus.label(order.status),
            style = MaterialTheme.typography.labelSmall,
            color = badgeText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Spacer(Modifier.height(10.dp))
      val firstItemMap = order.items.firstOrNull()
      val imageKey = firstItemMap?.get("productImageKey") as? String ?: "food_noodle"

      Row(verticalAlignment = Alignment.CenterVertically) {
        ProductThumbnail(
          imageKey = imageKey,
          contentDescription = order.itemSummary,
          modifier = Modifier.size(56.dp),
          iconSize = 26.dp,
          cornerRadius = 10.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = order.itemSummary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "${order.shippingMethod} • ${order.paymentMethod}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "Tujuan: ${order.shippingAddress}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text("Total Belanja", style = MaterialTheme.typography.labelSmall)
          Text(
            text = formatRupiah(order.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            color = ShopeeOrange,
            fontWeight = FontWeight.Bold,
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          when (order.status) {
            OrderStatus.BELUM_BAYAR -> {
              OutlinedButton(
                onClick = { onAdvanceStatus(order, OrderStatus.DIBATALKAN) }
              ) {
                Text("Batalkan")
              }
              Button(
                onClick = { onAdvanceStatus(order, OrderStatus.DIKEMAS) },
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
              ) {
                Text("Bayar", color = Color.White)
              }
            }
            OrderStatus.DIKEMAS -> {
              Button(
                onClick = { onAdvanceStatus(order, OrderStatus.DIKIRIM) },
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue),
              ) {
                Text("Kirim Sekarang", color = Color.White)
              }
            }
            OrderStatus.DIKIRIM -> {
              Button(
                onClick = { onAdvanceStatus(order, OrderStatus.SELESAI) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldVoucher),
              ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pesanan Diterima", color = Color.White)
              }
            }
            else -> {
              OutlinedButton(onClick = { onReorder(order) }) {
                Text("Beli Lagi", color = ShopeeOrange)
              }
            }
          }
        }
      }
    }
  }
}
