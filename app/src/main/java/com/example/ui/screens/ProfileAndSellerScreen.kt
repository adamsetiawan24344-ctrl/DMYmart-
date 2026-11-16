package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.FavoriteItem
import com.example.data.model.MarketplaceCategories
import com.example.data.model.Product
import com.example.data.model.ShippingAddress
import com.example.data.model.UserProfile
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.formatRupiah
import com.example.ui.components.sellerPresetKeys
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.UiState

@Composable
fun ProfileAndSellerScreen(
  profile: UserProfile?,
  sellerProductsState: UiState<List<Product>>,
  addresses: List<ShippingAddress>,
  favorites: List<FavoriteItem>,
  isBusy: Boolean,
  onClaimCoins: () -> Unit,
  onSaveSellerProduct: (String?, String, String, List<String>, Long, Long, Long, String, String, Boolean, () -> Unit) -> Unit,
  onDeleteSellerProduct: (String) -> Unit,
  onAddAddress: (String, String, String, String, String, Boolean, () -> Unit) -> Unit,
  onSetDefaultAddress: (String) -> Unit,
  onDeleteAddress: (String) -> Unit,
  onOpenProduct: (String) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var showProductEditor by remember { mutableStateOf(false) }
  var showAddressManager by remember { mutableStateOf(false) }
  var showFavoritesDialog by remember { mutableStateOf(false) }

  val sellerProducts = (sellerProductsState as? UiState.Success)?.data ?: emptyList()

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    // Shopee-style Profile Header
    Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          modifier = Modifier.size(56.dp),
        ) {
          if (!profile?.photoUrl.isNullOrBlank()) {
            AsyncImage(
              model = profile?.photoUrl,
              contentDescription = "Foto Profil",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
          } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Text(
                text = (profile?.displayName?.firstOrNull() ?: 'D').uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = ShopeeOrange,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = profile?.displayName ?: "Pengguna DMYmart",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = profile?.email ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f),
          )
          Spacer(Modifier.height(4.dp))
          Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.22f)) {
            Text(
              text = "Member Classic • Mitra Penjual",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }
        IconButton(onClick = onSignOut, modifier = Modifier.testTag("sign_out_button")) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Logout,
            contentDescription = "Keluar akun",
            tint = Color.White,
          )
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      // Wallet & Coins
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text("Saldo DMYmartPay", style = MaterialTheme.typography.labelMedium)
              Text(
                text = formatRupiah(profile?.walletBalance ?: 250_000L),
                style = MaterialTheme.typography.titleLarge,
                color = ShopeeOrange,
                fontWeight = FontWeight.Bold,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.MonetizationOn, contentDescription = null, tint = AmberFlash, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("${profile?.coins ?: 1500} Koin", fontWeight = FontWeight.Bold)
              }
              TextButton(onClick = onClaimCoins) {
                Text("Klaim +250 Koin Harian", color = ShopeeOrange)
              }
            }
          }
        }
      }

      // Quick Menu (Addresses & Favorites)
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1f).clickable { showAddressManager = true },
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = ShopeeOrange)
              Spacer(Modifier.width(8.dp))
              Column {
                Text("Alamat (${addresses.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Kelola tujuan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1f).clickable { showFavoritesDialog = true },
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Color(0xFFE91E63))
              Spacer(Modifier.width(8.dp))
              Column {
                Text("Favorit (${favorites.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Produk disukai", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      // Seller Center Section ("Toko Saya - Jual Produk Minimarket")
      item {
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
                Icon(Icons.Rounded.Storefront, contentDescription = null, tint = ShopeeOrange)
                Spacer(Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Toko Saya (Mode Penjual)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "${sellerProducts.size} produk di etalase Anda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Button(
                onClick = {
                  editingProduct = null
                  showProductEditor = true
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
                modifier = Modifier.testTag("add_seller_product_button"),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Jual", color = Color.White)
              }
            }
          }
        }
      }

      items(sellerProducts, key = { it.id }) { prod ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth().clickable { onOpenProduct(prod.id) },
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            ProductThumbnail(
              imageKey = prod.imageKey,
              contentDescription = prod.name,
              modifier = Modifier.size(62.dp),
              iconSize = 28.dp,
              cornerRadius = 10.dp,
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = prod.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false),
                )
                if (prod.isOfficialStore) {
                  Spacer(Modifier.width(4.dp))
                  Icon(Icons.Rounded.Verified, contentDescription = null, tint = ShopeeOrange, modifier = Modifier.size(14.dp))
                }
              }
              Text(
                text = "${formatRupiah(prod.price)} • Stok ${prod.stock}",
                style = MaterialTheme.typography.bodySmall,
                color = ShopeeOrange,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = prod.categories.joinToString(" • "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            IconButton(
              onClick = {
                editingProduct = prod
                showProductEditor = true
              }
            ) {
              Icon(Icons.Rounded.Edit, contentDescription = "Edit produk", tint = ShopeeOrange)
            }
            IconButton(onClick = { onDeleteSellerProduct(prod.id) }) {
              Icon(
                Icons.Rounded.DeleteOutline,
                contentDescription = "Hapus produk",
                tint = MaterialTheme.colorScheme.error,
              )
            }
          }
        }
      }
    }
  }

  if (showProductEditor) {
    SellerProductEditorSheet(
      existingProduct = editingProduct,
      isBusy = isBusy,
      onSave = { name, desc, cats, price, orig, stock, unit, imageKey, flash ->
        onSaveSellerProduct(
          editingProduct?.id,
          name,
          desc,
          cats,
          price,
          orig,
          stock,
          unit,
          imageKey,
          flash,
        ) {
          showProductEditor = false
        }
      },
      onDismiss = { showProductEditor = false },
    )
  }

  if (showAddressManager) {
    AddressManagerDialog(
      addresses = addresses,
      defaultName = profile?.displayName ?: "",
      onAdd = onAddAddress,
      onSetDefault = onSetDefaultAddress,
      onDelete = onDeleteAddress,
      onDismiss = { showAddressManager = false },
    )
  }

  if (showFavoritesDialog) {
    AlertDialog(
      onDismissRequest = { showFavoritesDialog = false },
      title = { Text("Produk Favorit Saya") },
      text = {
        if (favorites.isEmpty()) {
          Text("Belum ada produk yang ditandai favorit.")
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            favorites.forEach { fav ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier =
                  Modifier.fillMaxWidth().clickable {
                    showFavoritesDialog = false
                    onOpenProduct(fav.productId)
                  },
              ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                  ProductThumbnail(
                    imageKey = fav.imageKey,
                    contentDescription = fav.productName,
                    modifier = Modifier.size(42.dp),
                    iconSize = 20.dp,
                    cornerRadius = 8.dp,
                  )
                  Spacer(Modifier.width(10.dp))
                  Column {
                    Text(fav.productName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(formatRupiah(fav.price), color = ShopeeOrange, style = MaterialTheme.typography.labelMedium)
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showFavoritesDialog = false }) { Text("Tutup") }
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SellerProductEditorSheet(
  existingProduct: Product?,
  isBusy: Boolean,
  onSave: (String, String, List<String>, Long, Long, Long, String, String, Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  var name by remember { mutableStateOf(existingProduct?.name ?: "") }
  var desc by remember { mutableStateOf(existingProduct?.description ?: "") }
  var priceText by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
  var origPriceText by remember { mutableStateOf(existingProduct?.originalPrice?.toString() ?: "") }
  var stockText by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "25") }
  var unit by remember { mutableStateOf(existingProduct?.unit ?: "1 Pcs") }
  var selectedCategories by remember {
    mutableStateOf(
      existingProduct?.categories?.toSet() ?: setOf(MarketplaceCategories.FOOD_DRINK)
    )
  }
  var imageKey by remember { mutableStateOf(existingProduct?.imageKey ?: "food_noodle") }
  var isFlashSale by remember { mutableStateOf(existingProduct?.isFlashSale ?: false) }

  val priceVal = priceText.toLongOrNull() ?: 0L
  val origVal = origPriceText.toLongOrNull() ?: priceVal
  val stockVal = stockText.toLongOrNull() ?: 0L
  val canSubmit =
    name.trim().length >= 2 && priceVal >= 100L && selectedCategories.isNotEmpty() && !isBusy

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp, vertical = 8.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = if (existingProduct == null) "Jual Produk Baru di DMYmart" else "Edit Produk Toko",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nama Produk") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("seller_product_name_input"),
      )

      OutlinedTextField(
        value = desc,
        onValueChange = { desc = it },
        label = { Text("Deskripsi Produk") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
      )

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = priceText,
          onValueChange = { priceText = it.filter(Char::isDigit) },
          label = { Text("Harga Jual (Rp)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = origPriceText,
          onValueChange = { origPriceText = it.filter(Char::isDigit) },
          label = { Text("Harga Coret (Rp)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = stockText,
          onValueChange = { stockText = it.filter(Char::isDigit) },
          label = { Text("Stok") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = unit,
          onValueChange = { unit = it },
          label = { Text("Satuan / Varian") },
          singleLine = true,
          modifier = Modifier.weight(1.2f),
        )
      }

      Text(
        text = "Kategori Produk (Pilih 1 atau Lebih)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        MarketplaceCategories.allSelectable.forEach { cat ->
          val active = cat in selectedCategories
          FilterChip(
            selected = active,
            onClick = {
              if (active && selectedCategories.size > 1) {
                selectedCategories = selectedCategories - cat
              } else if (!active) {
                selectedCategories = selectedCategories + cat
              }
            },
            label = { Text(cat) },
            leadingIcon =
              if (active) {
                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
              } else null,
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = ShopeeOrangeSoft,
                selectedLabelColor = ShopeeOrange,
              ),
          )
        }
      }

      Text(
        text = "Tampilan Ikon Produk",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sellerPresetKeys) { (key, label) ->
          val selected = imageKey == key
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
              Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) ShopeeOrangeSoft else Color.Transparent)
                .clickable { imageKey = key }
                .padding(6.dp),
          ) {
            ProductThumbnail(
              imageKey = key,
              contentDescription = label,
              modifier = Modifier.size(48.dp),
              iconSize = 22.dp,
              cornerRadius = 10.dp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = if (selected) ShopeeOrange else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Ikutkan ke Promo Flash Sale", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = isFlashSale, onCheckedChange = { isFlashSale = it })
      }

      Button(
        onClick = {
          onSave(
            name,
            desc,
            selectedCategories.toList(),
            priceVal,
            maxOf(origVal, priceVal),
            stockVal,
            unit,
            imageKey,
            isFlashSale,
          )
        },
        enabled = canSubmit,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
        modifier = Modifier.fillMaxWidth().height(50.dp),
      ) {
        Text(
          text = if (existingProduct == null) "Terbitkan Produk" else "Simpan Perubahan",
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun AddressManagerDialog(
  addresses: List<ShippingAddress>,
  defaultName: String,
  onAdd: (String, String, String, String, String, Boolean, () -> Unit) -> Unit,
  onSetDefault: (String) -> Unit,
  onDelete: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var showAddForm by remember(addresses) { mutableStateOf(addresses.isEmpty()) }
  var label by remember { mutableStateOf("Rumah") }
  var recipient by remember { mutableStateOf(defaultName) }
  var phone by remember { mutableStateOf("") }
  var fullAddress by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (showAddForm) "Tambah Alamat" else "Daftar Alamat") },
    text = {
      if (showAddForm) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, singleLine = true)
          OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Nama Penerima") }, singleLine = true)
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No. Telepon") }, singleLine = true)
          OutlinedTextField(value = fullAddress, onValueChange = { fullAddress = it }, label = { Text("Alamat Lengkap") }, minLines = 2)
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          addresses.forEach { addr ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (addr.isDefault) ShopeeOrangeSoft else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth().clickable { onSetDefault(addr.id) },
            ) {
              Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${addr.label}${if (addr.isDefault) " (Utama)" else ""}",
                    fontWeight = FontWeight.Bold,
                    color = if (addr.isDefault) ShopeeOrange else MaterialTheme.colorScheme.onSurface,
                  )
                  Text("${addr.recipientName} • ${addr.phone}", style = MaterialTheme.typography.bodySmall)
                  Text(addr.fullAddress, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { onDelete(addr.id) }) {
                  Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus alamat")
                }
              }
            }
          }
          OutlinedButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
            Text("+ Tambah Alamat Baru", color = ShopeeOrange)
          }
        }
      }
    },
    confirmButton = {
      if (showAddForm) {
        Button(
          onClick = {
            onAdd(label, recipient, phone, fullAddress, "", true) {
              showAddForm = false
            }
          },
          enabled = recipient.isNotBlank() && phone.length >= 5 && fullAddress.length >= 5,
          colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
        ) {
          Text("Simpan", color = Color.White)
        }
      } else {
        TextButton(onClick = onDismiss) { Text("Selesai") }
      }
    },
    dismissButton = {
      if (showAddForm && addresses.isNotEmpty()) {
        TextButton(onClick = { showAddForm = false }) { Text("Batal") }
      }
    },
  )
}
