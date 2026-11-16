package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.formatRupiah
import com.example.ui.components.formatSoldCount
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
  product: Product,
  isFavorite: Boolean,
  cartCount: Int,
  onToggleFavorite: () -> Unit,
  onAddToCart: (Int) -> Unit,
  onBuyNow: (Int) -> Unit,
  onCategoryClick: (String) -> Unit,
  onOpenCart: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  var selectedQty by remember(product.id) { mutableIntStateOf(1) }
  val maxQty = product.stock.toInt().coerceAtLeast(1)

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      // Hero Product Artwork
      Box(modifier = Modifier.fillMaxWidth().height(290.dp)) {
        ProductThumbnail(
          imageKey = product.imageKey,
          contentDescription = product.name,
          modifier = Modifier.fillMaxSize(),
          iconSize = 86.dp,
          cornerRadius = 0.dp,
        )

        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.42f),
            modifier = Modifier.size(44.dp),
          ) {
            IconButton(onClick = onBack) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.42f),
              modifier = Modifier.size(44.dp),
            ) {
              IconButton(onClick = onToggleFavorite) {
                Icon(
                  imageVector =
                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                  contentDescription = "Favorit",
                  tint = if (isFavorite) Color(0xFFFF4D6D) else Color.White,
                )
              }
            }
            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.42f),
              modifier = Modifier.size(44.dp),
            ) {
              IconButton(onClick = onOpenCart) {
                BadgedBox(
                  badge = {
                    if (cartCount > 0) {
                      Badge(containerColor = AmberGold, contentColor = Color(0xFF1D1E24)) {
                        Text(cartCount.toString())
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Rounded.ShoppingCart,
                    contentDescription = "Keranjang",
                    tint = Color.White,
                  )
                }
              }
            }
          }
        }
      }

      if (product.isFlashSale) {
        Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(20.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = "FLASH SALE MINIMARKET",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
              )
            }
            Text(
              text = "Stok ${product.stock}",
              style = MaterialTheme.typography.labelMedium,
              color = AmberGold,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = formatRupiah(product.price),
              style = MaterialTheme.typography.headlineMedium,
              color = ShopeeOrange,
              fontWeight = FontWeight.ExtraBold,
            )
            if (product.originalPrice > product.price) {
              Spacer(Modifier.width(10.dp))
              Text(
                text = formatRupiah(product.originalPrice),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
              )
              Spacer(Modifier.width(8.dp))
              Surface(shape = RoundedCornerShape(6.dp), color = ShopeeOrangeSoft) {
                Text(
                  text = "-${product.discountPercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = ShopeeOrange,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
          }

          Spacer(Modifier.height(8.dp))
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )

          Spacer(Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = String.format("%.1f", product.rating),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(
              text = "• ${formatSoldCount(product.soldCount)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "• Kemasan: ${product.unit}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Spacer(Modifier.height(12.dp))
          Text(
            text = "Kategori Produk",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            product.categories.forEach { cat ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ShopeeOrangeSoft,
                modifier =
                  Modifier.clip(RoundedCornerShape(8.dp)).clickable { onCategoryClick(cat) },
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelMedium,
                  color = ShopeeOrange,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      // Delivery & Guarantee Strip
      Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(shape = CircleShape, color = EmeraldSoft, modifier = Modifier.size(38.dp)) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.LocalShipping,
                contentDescription = null,
                tint = EmeraldVoucher,
                modifier = Modifier.size(20.dp),
              )
            }
          }
          Spacer(Modifier.width(12.dp))
          Column {
            Text(
              text = "Gratis Ongkir Instan & Garansi Segar",
              style = MaterialTheme.typography.titleSmall,
              color = EmeraldVoucher,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Dikirim cepat dari minimarket terdekat dalam 30-60 menit.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      // Store Info
      Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(shape = CircleShape, color = ShopeeOrangeSoft, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.Storefront,
                contentDescription = null,
                tint = ShopeeOrange,
              )
            }
          }
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = product.sellerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              if (product.isOfficialStore) {
                Spacer(Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Rounded.Verified,
                  contentDescription = "Official Store",
                  tint = ShopeeOrange,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
            Text(
              text = "Aktif sekarang • Pengiriman Cepat 100%",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      // Description + Quantity Picker
      Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Deskripsi Produk",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Spacer(Modifier.height(6.dp))
          Text(
            text =
              product.description.ifBlank {
                "Produk berkualitas tersedia di minimarket DMYmart dengan pengiriman cepat dan harga terjangkau."
              },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Jumlah Beli (Stok ${product.stock})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { if (selectedQty > 1) selectedQty-- },
                  modifier = Modifier.size(40.dp),
                ) {
                  Icon(Icons.Rounded.Remove, contentDescription = "Kurangi")
                }
                Text(
                  text = selectedQty.toString(),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp),
                )
                IconButton(
                  onClick = { if (selectedQty < maxQty) selectedQty++ },
                  modifier = Modifier.size(40.dp),
                ) {
                  Icon(Icons.Rounded.Add, contentDescription = "Tambah", tint = ShopeeOrange)
                }
              }
            }
          }
        }
      }
      Spacer(Modifier.height(20.dp))
    }

    // Sticky Bottom Action Bar
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedButton(
          onClick = { onAddToCart(selectedQty) },
          enabled = product.stock > 0L,
          shape = RoundedCornerShape(12.dp),
          modifier =
            Modifier.weight(1f).height(50.dp).testTag("detail_add_to_cart_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.AddShoppingCart,
            contentDescription = null,
            tint = ShopeeOrange,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = "+ Keranjang",
            color = ShopeeOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        Button(
          onClick = { onBuyNow(selectedQty) },
          enabled = product.stock > 0L,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
          modifier = Modifier.weight(1f).height(50.dp).testTag("detail_buy_now_button"),
        ) {
          Text(
            text = if (product.stock > 0L) "Beli Sekarang" else "Stok Habis",
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
