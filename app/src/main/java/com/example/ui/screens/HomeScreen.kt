package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.RiceBowl
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AvailableVouchers
import com.example.data.model.MarketplaceCategories
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.ui.components.FlashSaleCard
import com.example.ui.components.ShopeeProductCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeDark
import com.example.ui.theme.ShopeeOrangeLight
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
  productsState: UiState<List<Product>>,
  profile: UserProfile?,
  cartItemCount: Int,
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenCart: () -> Unit,
  onOpenAllCategories: () -> Unit,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  onClaimCoins: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var secondsLeft by remember { mutableIntStateOf(6245) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000L)
      if (secondsLeft > 0) secondsLeft-- else secondsLeft = 7200
    }
  }
  val hours = secondsLeft / 3600
  val mins = (secondsLeft % 3600) / 60
  val secs = secondsLeft % 60

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
  ) {
    // Shopee Coral-Orange Search Header
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(Brush.horizontalGradient(listOf(ShopeeOrangeDark, ShopeeOrange, ShopeeOrangeLight)))
          .statusBarsPadding()
          .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          modifier =
            Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onOpenSearch() }
              .testTag("home_search_bar"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = "Cari di DMYmart",
              tint = ShopeeOrange,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
              text = "Cari Indomie, Minyak 2L, Sabun...",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF7B7E8A),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
          onClick = onOpenCart,
          modifier = Modifier.size(48.dp).testTag("home_cart_button"),
        ) {
          BadgedBox(
            badge = {
              if (cartItemCount > 0) {
                Badge(containerColor = AmberGold, contentColor = Color(0xFF1D1E24)) {
                  Text(
                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Rounded.ShoppingCart,
              contentDescription = "Keranjang Belanja",
              tint = Color.White,
              modifier = Modifier.size(26.dp),
            )
          }
        }
      }
    }

    when (productsState) {
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
          Text(
            text = productsState.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
          )
        }
      }
      is UiState.Success -> {
        val allProducts = productsState.data
        val flashSaleProducts =
          remember(allProducts) { allProducts.filter { it.isFlashSale }.take(8) }
        val filteredProducts =
          remember(allProducts, selectedCategory) {
            if (selectedCategory == MarketplaceCategories.ALL) {
              allProducts.sortedByDescending { it.soldCount }
            } else {
              allProducts
                .filter { it.categories.contains(selectedCategory) }
                .sortedByDescending { it.soldCount }
            }
          }

        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 165.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          // 1. DMYmartPay & Koin Strip + Promo Carousel
          item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
              DMYmartWalletStrip(
                profile = profile,
                onClaimCoins = onClaimCoins,
                onOpenPromoCategory = { onSelectCategory(MarketplaceCategories.PROMO) },
              )
              Spacer(Modifier.height(10.dp))
              PromoBannerCarousel(
                onPromoClick = { onSelectCategory(MarketplaceCategories.PROMO) }
              )
            }
          }

          // 2. Category Grid
          item(span = { GridItemSpan(maxLineSpan) }) {
            CategoryShortcutSection(
              selectedCategory = selectedCategory,
              onSelectCategory = { cat ->
                if (cat == MarketplaceCategories.ALL && selectedCategory == MarketplaceCategories.ALL) {
                  onOpenAllCategories()
                } else {
                  onSelectCategory(cat)
                }
              },
              onOpenAllCategories = onOpenAllCategories,
            )
          }

          // 3. Flash Sale Horizontal Carousel
          if (flashSaleProducts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
              Column(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 12.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Rounded.Bolt,
                      contentDescription = null,
                      tint = ShopeeOrange,
                      modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                      text = "FLASH SALE",
                      style = MaterialTheme.typography.titleMedium,
                      color = ShopeeOrange,
                      fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.width(10.dp))
                    CountdownPill(hours = hours, minutes = mins, seconds = secs)
                  }
                  Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShopeeOrange,
                    modifier =
                      Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(MarketplaceCategories.PROMO) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                  )
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(
                  contentPadding = PaddingValues(horizontal = 14.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  items(flashSaleProducts, key = { it.id }) { prod ->
                    FlashSaleCard(
                      product = prod,
                      onProductClick = onProductClick,
                      onAddToCart = onAddToCart,
                    )
                  }
                }
              }
            }
          }

          // 4. Filter Header for Recommendations
          item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text =
                    if (selectedCategory == MarketplaceCategories.ALL) "REKOMENDASI PILIHAN"
                    else selectedCategory.uppercase(),
                  style = MaterialTheme.typography.titleSmall,
                  color = ShopeeOrange,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${filteredProducts.size} produk",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Spacer(Modifier.height(6.dp))
              LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                val filterChoices =
                  listOf(MarketplaceCategories.ALL) + MarketplaceCategories.allSelectable
                items(filterChoices) { cat ->
                  FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat, maxLines = 1) },
                    colors =
                      FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ShopeeOrangeSoft,
                        selectedLabelColor = ShopeeOrange,
                      ),
                  )
                }
              }
            }
          }

          // 5. Product Grid
          items(
            items = filteredProducts,
            key = { it.id },
          ) { product ->
            val index = filteredProducts.indexOf(product)
            val startPad = if (index % 2 == 0) 14.dp else 0.dp
            val endPad = if (index % 2 == 1) 14.dp else 0.dp
            ShopeeProductCard(
              product = product,
              onProductClick = onProductClick,
              onAddToCart = onAddToCart,
              modifier = Modifier.padding(start = startPad, end = endPad),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DMYmartWalletStrip(
  profile: UserProfile?,
  onClaimCoins: () -> Unit,
  onOpenPromoCategory: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
          imageVector = Icons.Rounded.AccountBalanceWallet,
          contentDescription = null,
          tint = ShopeeOrange,
          modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column {
          Text(
            text = formatRupiah(profile?.walletBalance ?: 250_000L),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "DMYmartPay",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClaimCoins() }
            .padding(vertical = 2.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.MonetizationOn,
          contentDescription = null,
          tint = AmberFlash,
          modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column {
          Text(
            text = "${profile?.coins ?: 1500} Koin",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Klaim +250",
            style = MaterialTheme.typography.labelSmall,
            color = ShopeeOrange,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpenPromoCategory() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.ConfirmationNumber,
          contentDescription = null,
          tint = EmeraldVoucher,
          modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column {
          Text(
            text = "${AvailableVouchers.list.size} Voucher",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Gratis Ongkir",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldVoucher,
          )
        }
      }
    }
  }
}

@Composable
private fun PromoBannerCarousel(onPromoClick: () -> Unit) {
  val banners =
    remember {
      listOf(
        Triple(
          R.drawable.img_hero_promo,
          "MEGA PROMO MINIMARKET",
          "Diskon Sembako & Snack hingga 35% + Cashback Koin",
        ),
        Triple(
          R.drawable.img_promo_gratis_ongkir,
          "GRATIS ONGKIR INSTAN",
          "Antar 30 menit sampai rumah pakai kode GRATISONGKIR",
        ),
      )
    }
  val pagerState = rememberPagerState(pageCount = { banners.size })

  Column(modifier = Modifier.fillMaxWidth()) {
    HorizontalPager(
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 14.dp),
      pageSpacing = 10.dp,
      modifier = Modifier.fillMaxWidth().height(142.dp),
    ) { page ->
      val (imgRes, title, subtitle) = banners[page]
      Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize().clickable { onPromoClick() },
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = imgRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .background(
                  Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                  )
                )
          )
          Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(16.dp).fillMaxWidth(0.7f)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AmberGold,
            ) {
              Text(
                text = "PROMO DMYMART",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1D1E24),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
            Spacer(Modifier.height(6.dp))
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.92f),
              maxLines = 2,
            )
          }
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
    ) {
      repeat(banners.size) { idx ->
        val active = pagerState.currentPage == idx
        Box(
          modifier =
            Modifier
              .padding(horizontal = 3.dp)
              .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
              .clip(CircleShape)
              .background(if (active) ShopeeOrange else Color.Gray.copy(alpha = 0.35f))
        )
      }
    }
  }
}

@Composable
private fun CategoryShortcutSection(
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  onOpenAllCategories: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Kategori Minimarket",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Semua Kategori >",
          style = MaterialTheme.typography.labelMedium,
          color = ShopeeOrange,
          modifier =
            Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { onOpenAllCategories() }
              .padding(horizontal = 4.dp, vertical = 2.dp),
        )
      }
      Spacer(Modifier.height(10.dp))

      val items = MarketplaceCategories.homeGrid
      val firstRow = items.take(4)
      val secondRow = items.drop(4).take(4)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        firstRow.forEach { meta ->
          CategoryIconTile(
            label = meta.shortLabel,
            icon = resolveCategoryIcon(meta.iconKey),
            accent = resolveCategoryColor(meta.iconKey),
            selected = selectedCategory == meta.id,
            onClick = { onSelectCategory(meta.id) },
            modifier = Modifier.weight(1f),
          )
        }
      }
      Spacer(Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        secondRow.forEach { meta ->
          CategoryIconTile(
            label = meta.shortLabel,
            icon = resolveCategoryIcon(meta.iconKey),
            accent = resolveCategoryColor(meta.iconKey),
            selected = selectedCategory == meta.id,
            onClick = { onSelectCategory(meta.id) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryIconTile(
  label: String,
  icon: ImageVector,
  accent: Color,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .clickable { onClick() }
        .padding(vertical = 4.dp, horizontal = 2.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = if (selected) accent else accent.copy(alpha = 0.12f),
      modifier = Modifier.size(46.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = if (selected) Color.White else accent,
          modifier = Modifier.size(24.dp),
        )
      }
    }
    Spacer(Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (selected) ShopeeOrange else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun CountdownPill(hours: Int, minutes: Int, seconds: Int) {
  Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
    TimeBox(String.format("%02d", hours))
    Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    TimeBox(String.format("%02d", minutes))
    Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    TimeBox(String.format("%02d", seconds))
  }
}

@Composable
private fun TimeBox(value: String) {
  Surface(
    shape = RoundedCornerShape(5.dp),
    color = Color(0xFF1D1E24),
  ) {
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
    )
  }
}

fun resolveCategoryIcon(key: String): ImageVector =
  when (key) {
    "food" -> Icons.Rounded.Fastfood
    "household" -> Icons.Rounded.CleaningServices
    "care" -> Icons.Rounded.Spa
    "pantry" -> Icons.Rounded.RiceBowl
    "baby" -> Icons.Rounded.BabyChangingStation
    "health" -> Icons.Rounded.Medication
    "promo" -> Icons.Rounded.LocalOffer
    else -> Icons.Rounded.Apps
  }

fun resolveCategoryColor(key: String): Color =
  when (key) {
    "food" -> ShopeeOrange
    "household" -> Color(0xFF7B1FA2)
    "care" -> Color(0xFF00838F)
    "pantry" -> Color(0xFF558B2F)
    "baby" -> InfoBlue
    "health" -> EmeraldVoucher
    "promo" -> AmberFlash
    else -> ShopeeOrangeDark
  }
