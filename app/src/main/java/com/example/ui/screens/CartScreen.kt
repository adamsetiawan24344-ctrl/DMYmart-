package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AvailableVouchers
import com.example.data.model.CartItem
import com.example.data.model.VoucherPromo
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.formatRupiah
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
  cartState: UiState<List<CartItem>>,
  selectedVoucher: VoucherPromo?,
  onToggleSelectItem: (CartItem) -> Unit,
  onToggleSelectAll: (List<CartItem>, Boolean) -> Unit,
  onIncrementItem: (CartItem) -> Unit,
  onDecrementItem: (CartItem) -> Unit,
  onRemoveItem: (CartItem) -> Unit,
  onRemoveSelected: (List<CartItem>) -> Unit,
  onSelectVoucher: (VoucherPromo?) -> Unit,
  onProductClick: (String) -> Unit,
  onGoShopping: () -> Unit,
  onCheckout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showVoucherSheet by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
      val itemCount = (cartState as? UiState.Success)?.data?.size ?: 0
      val selectedItems =
        (cartState as? UiState.Success)?.data?.filter { it.isSelected } ?: emptyList()

      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = "Keranjang Saya ($itemCount)",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Tersimpan otomatis di akun Anda",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f),
          )
        }
        if (selectedItems.isNotEmpty() && cartState is UiState.Success) {
          TextButton(onClick = { onRemoveSelected(cartState.data) }) {
            Text(
              text = "Hapus (${selectedItems.size})",
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }

    when (cartState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = ShopeeOrange)
        }
      }
      is UiState.Error -> {
        Box(
          modifier = Modifier.fillMaxSize().padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(cartState.message, color = MaterialTheme.colorScheme.error)
        }
      }
      is UiState.Success -> {
        val items = cartState.data
        if (items.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Surface(
                shape = RoundedCornerShape(28.dp),
                color = ShopeeOrangeSoft,
                modifier = Modifier.size(92.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.ShoppingCart,
                    contentDescription = null,
                    tint = ShopeeOrange,
                    modifier = Modifier.size(44.dp),
                  )
                }
              }
              Spacer(Modifier.height(16.dp))
              Text(
                text = "Wah, keranjang belanjamu kosong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Spacer(Modifier.height(6.dp))
              Text(
                text = "Yuk isi keranjang dengan kebutuhan minimarket harian & promo hemat!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(Modifier.height(18.dp))
              Button(
                onClick = onGoShopping,
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
              ) {
                Text("Belanja Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          val grouped = remember(items) { items.groupBy { it.sellerName } }
          val selectedItems = items.filter { it.isSelected }
          val allSelected = items.isNotEmpty() && selectedItems.size == items.size
          val subtotal = selectedItems.sumOf { it.price * it.quantity }
          val originalSubtotal =
            selectedItems.sumOf { maxOf(it.originalPrice, it.price) * it.quantity }
          val productSavings = maxOf(0L, originalSubtotal - subtotal)
          val voucherSavings = selectedVoucher?.calculateDiscount(subtotal, 5_000L) ?: 0L
          val totalSavings = productSavings + voucherSavings

          Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
              contentPadding = PaddingValues(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f),
            ) {
              grouped.forEach { (sellerName, sellerItems) ->
                item(key = "store_$sellerName") {
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      val storeAllSelected = sellerItems.all { it.isSelected }
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        Checkbox(
                          checked = storeAllSelected,
                          onCheckedChange = { checked ->
                            onToggleSelectAll(sellerItems, checked)
                          },
                          colors = CheckboxDefaults.colors(checkedColor = ShopeeOrange),
                        )
                        Icon(
                          imageVector = Icons.Rounded.Storefront,
                          contentDescription = null,
                          tint = ShopeeOrange,
                          modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                          text = sellerName,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold,
                        )
                      }
                      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                      sellerItems.forEachIndexed { idx, cartItem ->
                        CartItemRow(
                          item = cartItem,
                          onToggleSelect = { onToggleSelectItem(cartItem) },
                          onIncrement = { onIncrementItem(cartItem) },
                          onDecrement = { onDecrementItem(cartItem) },
                          onRemove = { onRemoveItem(cartItem) },
                          onClick = { onProductClick(cartItem.productId) },
                        )
                        if (idx < sellerItems.lastIndex) {
                          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                      }
                    }
                  }
                }
              }
            }

            // Voucher & Checkout Sticky Footer
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 8.dp,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clickable { showVoucherSheet = true }
                      .padding(horizontal = 14.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Rounded.ConfirmationNumber,
                      contentDescription = null,
                      tint = ShopeeOrange,
                      modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                      text = "Voucher DMYmart",
                      style = MaterialTheme.typography.labelLarge,
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedVoucher != null) EmeraldSoft else ShopeeOrangeSoft,
                  ) {
                    Text(
                      text =
                        if (selectedVoucher != null) {
                          "${selectedVoucher.code} (Hemat ${formatRupiah(voucherSavings)})"
                        } else {
                          "Pilih Voucher >"
                        },
                      style = MaterialTheme.typography.labelSmall,
                      color = if (selectedVoucher != null) EmeraldVoucher else ShopeeOrange,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                  }
                }

                HorizontalDivider()

                Row(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Checkbox(
                    checked = allSelected,
                    onCheckedChange = { checked -> onToggleSelectAll(items, checked) },
                    colors = CheckboxDefaults.colors(checkedColor = ShopeeOrange),
                    modifier = Modifier.testTag("cart_select_all_checkbox"),
                  )
                  Text(
                    text = "Semua",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 8.dp),
                  )

                  Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                  ) {
                    Text(
                      text = formatRupiah(subtotal),
                      style = MaterialTheme.typography.titleMedium,
                      color = ShopeeOrange,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                    )
                    if (totalSavings > 0L) {
                      Text(
                        text = "Hemat ${formatRupiah(totalSavings)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldVoucher,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }
                  }

                  Button(
                    onClick = onCheckout,
                    enabled = selectedItems.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    colors =
                      ButtonDefaults.buttonColors(
                        containerColor = ShopeeOrange,
                        contentColor = Color.White,
                      ),
                    modifier = Modifier.height(46.dp).testTag("cart_checkout_button"),
                  ) {
                    val totalQty = selectedItems.sumOf { it.quantity }
                    Text(
                      text = "Checkout ($totalQty)",
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showVoucherSheet) {
    ModalBottomSheet(onDismissRequest = { showVoucherSheet = false }) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Pilih Voucher DMYmart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          if (selectedVoucher != null) {
            TextButton(
              onClick = {
                onSelectVoucher(null)
                showVoucherSheet = false
              }
            ) {
              Text("Lepas Voucher", color = ShopeeOrange)
            }
          }
        }
        Spacer(Modifier.height(10.dp))
        AvailableVouchers.list.forEach { voucher ->
          val active = selectedVoucher?.code == voucher.code
          Card(
            shape = RoundedCornerShape(12.dp),
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (active) ShopeeOrangeSoft else MaterialTheme.colorScheme.surfaceVariant
              ),
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable {
                  onSelectVoucher(voucher)
                  showVoucherSheet = false
                },
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = voucher.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ShopeeOrange,
                )
                Text(
                  text = voucher.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  text = "Kode: ${voucher.code}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              if (active) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = "Dipilih",
                  tint = ShopeeOrange,
                )
              } else {
                OutlinedButton(
                  onClick = {
                    onSelectVoucher(voucher)
                    showVoucherSheet = false
                  }
                ) {
                  Text("Pakai", color = ShopeeOrange)
                }
              }
            }
          }
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun CartItemRow(
  item: CartItem,
  onToggleSelect: () -> Unit,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onRemove: () -> Unit,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Checkbox(
      checked = item.isSelected,
      onCheckedChange = { onToggleSelect() },
      colors = CheckboxDefaults.colors(checkedColor = ShopeeOrange),
    )
    ProductThumbnail(
      imageKey = item.productImageKey,
      contentDescription = item.productName,
      modifier =
        Modifier
          .size(74.dp)
          .clip(RoundedCornerShape(10.dp))
          .clickable { onClick() },
      iconSize = 30.dp,
      cornerRadius = 10.dp,
    )
    Spacer(Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Text(
          text = item.productName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f).clickable { onClick() },
        )
        IconButton(
          onClick = onRemove,
          modifier = Modifier.size(36.dp).testTag("cart_delete_${item.id}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = "Hapus ${item.productName}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      Text(
        text = "${item.unit} • ${item.categories.joinToString(", ")}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      Spacer(Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = formatRupiah(item.price),
            style = MaterialTheme.typography.titleSmall,
            color = ShopeeOrange,
            fontWeight = FontWeight.Bold,
          )
          if (item.originalPrice > item.price) {
            Text(
              text = formatRupiah(item.originalPrice),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = TextDecoration.LineThrough,
            )
          }
        }

        // Shopee-style Quantity Stepper
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                  .clickable { onDecrement() }
                  .testTag("cart_dec_${item.id}"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Kurangi jumlah",
                modifier = Modifier.size(16.dp),
              )
            }
            Text(
              text = item.quantity.toString(),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp),
            )
            Box(
              modifier =
                Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                  .clickable { onIncrement() }
                  .testTag("cart_inc_${item.id}"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Tambah jumlah",
                tint = ShopeeOrange,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
      }
    }
  }
}
