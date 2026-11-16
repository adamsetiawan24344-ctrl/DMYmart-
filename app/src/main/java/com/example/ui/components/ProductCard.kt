package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft

@Composable
fun ShopeeProductCard(
  product: Product,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .clickable { onProductClick(product) }
        .testTag("product_card_${product.id}"),
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().height(138.dp)) {
        ProductThumbnail(
          imageKey = product.imageKey,
          contentDescription = product.name,
          modifier = Modifier.fillMaxWidth().height(138.dp),
          iconSize = 46.dp,
          cornerRadius = 0.dp,
        ) {
          if (product.discountPercent > 0) {
            Surface(
              shape = RoundedCornerShape(bottomStart = 10.dp),
              color = AmberFlash,
              modifier = Modifier.align(Alignment.TopEnd),
            ) {
              Text(
                text = "-${product.discountPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
              )
            }
          }
          if (product.isOfficialStore) {
            Surface(
              shape = RoundedCornerShape(bottomEnd = 10.dp),
              color = ShopeeOrange,
              modifier = Modifier.align(Alignment.TopStart),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Verified,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                  text = "DMY Mall",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
          if (product.isFlashSale) {
            Surface(
              shape = RoundedCornerShape(topEnd = 8.dp),
              color = Color(0xFF212121).copy(alpha = 0.78f),
              modifier = Modifier.align(Alignment.BottomStart),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Bolt,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text = "FLASH",
                  style = MaterialTheme.typography.labelSmall,
                  color = AmberGold,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }

      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          minLines = 2,
        )

        Spacer(Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          val firstCategory = product.categories.firstOrNull().orEmpty()
          if (firstCategory.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(5.dp),
              color = ShopeeOrangeSoft,
            ) {
              Text(
                text = firstCategory,
                style = MaterialTheme.typography.labelSmall,
                color = ShopeeOrange,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
              )
            }
          }
          if (product.categories.size > 1) {
            Surface(
              shape = RoundedCornerShape(5.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = "+${product.categories.size - 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
              )
            }
          }
        }

        Spacer(Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = formatRupiah(product.price),
              style = MaterialTheme.typography.titleMedium,
              color = ShopeeOrange,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
            )
            if (product.originalPrice > product.price) {
              Text(
                text = formatRupiah(product.originalPrice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 1,
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = if (product.stock > 0L) ShopeeOrange else MaterialTheme.colorScheme.surfaceVariant,
            modifier =
              Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(enabled = product.stock > 0L) { onAddToCart(product) }
                .testTag("quick_add_cart_${product.id}"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.AddShoppingCart,
                contentDescription = "Tambah ${product.name} ke keranjang",
                tint =
                  if (product.stock > 0L) Color.White
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        Spacer(Modifier.height(6.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = null,
              tint = AmberGold,
              modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.width(2.dp))
            Text(
              text = String.format("%.1f", product.rating),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
              text = formatSoldCount(product.soldCount),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
fun FlashSaleCard(
  product: Product,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier =
      modifier
        .width(142.dp)
        .clickable { onProductClick(product) },
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        ProductThumbnail(
          imageKey = product.imageKey,
          contentDescription = product.name,
          modifier = Modifier.fillMaxWidth().height(110.dp),
          iconSize = 38.dp,
          cornerRadius = 0.dp,
        ) {
          if (product.discountPercent > 0) {
            Surface(
              shape = RoundedCornerShape(bottomStart = 8.dp),
              color = ShopeeOrange,
              modifier = Modifier.align(Alignment.TopEnd),
            ) {
              Text(
                text = "-${product.discountPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
      }
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = formatRupiah(product.price),
            style = MaterialTheme.typography.titleSmall,
            color = ShopeeOrange,
            fontWeight = FontWeight.Bold,
          )
          Box(
            modifier =
              Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ShopeeOrangeSoft)
                .clickable(enabled = product.stock > 0L) { onAddToCart(product) },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.AddShoppingCart,
              contentDescription = "Tambah ke keranjang",
              tint = ShopeeOrange,
              modifier = Modifier.size(15.dp),
            )
          }
        }
        Spacer(Modifier.height(6.dp))
        val progress =
          ((product.soldCount % 100L).toFloat() / 100f).coerceIn(0.35f, 0.92f)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
          LinearProgressIndicator(
            progress = { progress },
            color = ShopeeOrange,
            trackColor = ShopeeOrangeSoft,
            modifier = Modifier.fillMaxWidth().height(13.dp).clip(RoundedCornerShape(8.dp)),
          )
          Text(
            text = if (product.stock < 35) "SEGERA HABIS" else "LARIS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
