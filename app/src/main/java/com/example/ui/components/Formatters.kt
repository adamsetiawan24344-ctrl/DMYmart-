package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.RamenDining
import androidx.compose.material.icons.rounded.RiceBowl
import androidx.compose.material.icons.rounded.Sanitizer
import androidx.compose.material.icons.rounded.Soap
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MarketplaceCategories
import java.text.NumberFormat
import java.util.Locale

private val indoLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()

fun formatRupiah(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(indoLocale)
  return "Rp${formatter.format(amount)}"
}

fun formatSoldCount(sold: Long): String =
  when {
    sold >= 1_000L -> {
      val thousands = sold / 1_000.0
      String.format(indoLocale, "%.1frb terjual", thousands).replace(".0", "")
    }
    else -> "$sold terjual"
  }

data class ProductArtSpec(
  val icon: ImageVector,
  val bgStart: Color,
  val bgEnd: Color,
  val tint: Color,
  val backdropRes: Int,
)

fun resolveProductArt(imageKey: String): ProductArtSpec =
  when (imageKey) {
    "food_noodle" ->
      ProductArtSpec(
        icon = Icons.Rounded.RamenDining,
        bgStart = Color(0xFFFFEFE7),
        bgEnd = Color(0xFFFFD8C2),
        tint = Color(0xFFD84315),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "pantry_oil" ->
      ProductArtSpec(
        icon = Icons.Rounded.WaterDrop,
        bgStart = Color(0xFFFFF8E1),
        bgEnd = Color(0xFFFFECB3),
        tint = Color(0xFFEF6C00),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "pantry_rice" ->
      ProductArtSpec(
        icon = Icons.Rounded.RiceBowl,
        bgStart = Color(0xFFF4F7EE),
        bgEnd = Color(0xFFE0EAD0),
        tint = Color(0xFF558B2F),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "food_milk" ->
      ProductArtSpec(
        icon = Icons.Rounded.LocalDrink,
        bgStart = Color(0xFFEBF5FF),
        bgEnd = Color(0xFFD0E7FF),
        tint = Color(0xFF1565C0),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "food_coffee" ->
      ProductArtSpec(
        icon = Icons.Rounded.Coffee,
        bgStart = Color(0xFFF5EFEA),
        bgEnd = Color(0xFFE4D3C3),
        tint = Color(0xFF5D4037),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "food_snack" ->
      ProductArtSpec(
        icon = Icons.Rounded.Fastfood,
        bgStart = Color(0xFFFFF0EB),
        bgEnd = Color(0xFFFFCFC0),
        tint = Color(0xFFE64A19),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "food_drink" ->
      ProductArtSpec(
        icon = Icons.Rounded.LocalDrink,
        bgStart = Color(0xFFE8F8F2),
        bgEnd = Color(0xFFC4EEDD),
        tint = Color(0xFF00897B),
        backdropRes = R.drawable.img_cat_food_drink,
      )
    "care_soap" ->
      ProductArtSpec(
        icon = Icons.Rounded.Soap,
        bgStart = Color(0xFFFFEBEE),
        bgEnd = Color(0xFFFFCDD2),
        tint = Color(0xFFC62828),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "care_dental" ->
      ProductArtSpec(
        icon = Icons.Rounded.Sanitizer,
        bgStart = Color(0xFFE3F2FD),
        bgEnd = Color(0xFFBBDEFB),
        tint = Color(0xFF0277BD),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "care_shampoo" ->
      ProductArtSpec(
        icon = Icons.Rounded.Spa,
        bgStart = Color(0xFFE0F7FA),
        bgEnd = Color(0xFFB2EBF2),
        tint = Color(0xFF00838F),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "household_detergent" ->
      ProductArtSpec(
        icon = Icons.Rounded.CleaningServices,
        bgStart = Color(0xFFF3E5F5),
        bgEnd = Color(0xFFE1BEE7),
        tint = Color(0xFF7B1FA2),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "household_dish" ->
      ProductArtSpec(
        icon = Icons.Rounded.CleaningServices,
        bgStart = Color(0xFFE8F5E9),
        bgEnd = Color(0xFFC8E6C9),
        tint = Color(0xFF2E7D32),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "household_tissue" ->
      ProductArtSpec(
        icon = Icons.Rounded.Inventory2,
        bgStart = Color(0xFFECEFF1),
        bgEnd = Color(0xFFCFD8DC),
        tint = Color(0xFF455A64),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "baby_diaper" ->
      ProductArtSpec(
        icon = Icons.Rounded.BabyChangingStation,
        bgStart = Color(0xFFE8F5FE),
        bgEnd = Color(0xFFC2E3FC),
        tint = Color(0xFF1976D2),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "health_vitamin" ->
      ProductArtSpec(
        icon = Icons.Rounded.Medication,
        bgStart = Color(0xFFFFF3E0),
        bgEnd = Color(0xFFFFE0B2),
        tint = Color(0xFFEF6C00),
        backdropRes = R.drawable.img_cat_household_care,
      )
    "promo_bundle" ->
      ProductArtSpec(
        icon = Icons.Rounded.CardGiftcard,
        bgStart = Color(0xFFFFECE6),
        bgEnd = Color(0xFFFFC9B8),
        tint = Color(0xFFEE4D2D),
        backdropRes = R.drawable.img_hero_promo,
      )
    else ->
      ProductArtSpec(
        icon = Icons.Rounded.LocalPharmacy,
        bgStart = Color(0xFFF3F4F8),
        bgEnd = Color(0xFFE2E4EC),
        tint = Color(0xFF4E5464),
        backdropRes = R.drawable.img_cat_food_drink,
      )
  }

fun categoryVisualKey(category: String): String =
  when (category) {
    MarketplaceCategories.FOOD_DRINK -> "food_snack"
    MarketplaceCategories.HOUSEHOLD -> "household_detergent"
    MarketplaceCategories.PERSONAL_CARE -> "care_soap"
    MarketplaceCategories.GROCERIES -> "pantry_rice"
    MarketplaceCategories.BABY_MOM -> "baby_diaper"
    MarketplaceCategories.HEALTH -> "health_vitamin"
    MarketplaceCategories.PROMO -> "promo_bundle"
    else -> "food_noodle"
  }

@Composable
fun ProductThumbnail(
  imageKey: String,
  contentDescription: String,
  modifier: Modifier = Modifier,
  iconSize: Dp = 44.dp,
  cornerRadius: Dp = 12.dp,
  overlay: @Composable BoxScope.() -> Unit = {},
) {
  val spec = resolveProductArt(imageKey)
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(Brush.linearGradient(listOf(spec.bgStart, spec.bgEnd))),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(id = spec.backdropRes),
      contentDescription = null,
      modifier = Modifier.fillMaxSize().alpha(0.24f),
      contentScale = ContentScale.Crop,
    )
    Surface(
      shape = CircleShape,
      color = Color.White.copy(alpha = 0.88f),
      shadowElevation = 2.dp,
      modifier = Modifier.size(iconSize + 24.dp),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Icon(
          imageVector = spec.icon,
          contentDescription = contentDescription,
          tint = spec.tint,
          modifier = Modifier.size(iconSize),
        )
      }
    }
    overlay()
  }
}

val sellerPresetKeys: List<Pair<String, String>> =
  listOf(
    "food_noodle" to "Makanan Instan",
    "food_snack" to "Snack & Camilan",
    "food_drink" to "Minuman Dingin",
    "food_milk" to "Susu & Olahan",
    "food_coffee" to "Kopi & Teh",
    "pantry_rice" to "Beras & Bahan Pokok",
    "pantry_oil" to "Minyak & Bumbu",
    "household_detergent" to "Deterjen & Kebersihan",
    "household_dish" to "Sabun Dapur",
    "care_soap" to "Sabun & Perawatan",
    "care_shampoo" to "Sampo & Rambut",
    "baby_diaper" to "Perlengkapan Bayi",
    "health_vitamin" to "Vitamin & Kesehatan",
    "promo_bundle" to "Paket Promo Hemat",
  )

@Composable
fun CategoryBadgePill(category: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
  ) {
    androidx.compose.material3.Text(
      text = category,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
      maxLines = 1,
    )
  }
}
