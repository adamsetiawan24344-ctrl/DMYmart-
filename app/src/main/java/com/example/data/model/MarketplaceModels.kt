package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

object MarketplaceCategories {
  const val ALL = "Semua"
  const val FOOD_DRINK = "Makanan & Minuman"
  const val HOUSEHOLD = "Kebutuhan Rumah Tangga"
  const val PERSONAL_CARE = "Perawatan Pribadi"
  const val GROCERIES = "Bahan Pokok & Dapur"
  const val BABY_MOM = "Bayi & Ibu"
  const val HEALTH = "Kesehatan & Vitamin"
  const val PROMO = "Promo & Paket Hemat"

  val allSelectable: List<String> =
    listOf(
      FOOD_DRINK,
      HOUSEHOLD,
      PERSONAL_CARE,
      GROCERIES,
      BABY_MOM,
      HEALTH,
      PROMO,
    )

  val homeGrid: List<CategoryMeta> =
    listOf(
      CategoryMeta(FOOD_DRINK, "Makanan & Minum", "food"),
      CategoryMeta(HOUSEHOLD, "Rumah Tangga", "household"),
      CategoryMeta(PERSONAL_CARE, "Perawatan Diri", "care"),
      CategoryMeta(GROCERIES, "Bahan Pokok", "pantry"),
      CategoryMeta(BABY_MOM, "Bayi & Ibu", "baby"),
      CategoryMeta(HEALTH, "Kesehatan", "health"),
      CategoryMeta(PROMO, "Paket Hemat", "promo"),
      CategoryMeta(ALL, "Semua Produk", "all"),
    )
}

data class CategoryMeta(
  val id: String,
  val shortLabel: String,
  val iconKey: String,
)

data class VoucherPromo(
  val code: String,
  val title: String,
  val subtitle: String,
  val minSpend: Long,
  val discountPercent: Int = 0,
  val maxDiscount: Long = 0L,
  val flatDiscount: Long = 0L,
  val shippingDiscount: Long = 0L,
) {
  fun calculateDiscount(subtotal: Long, shippingCost: Long): Long {
    if (subtotal < minSpend) return 0L
    if (shippingDiscount > 0L) {
      return minOf(shippingCost, shippingDiscount)
    }
    if (flatDiscount > 0L) {
      return minOf(subtotal, flatDiscount)
    }
    if (discountPercent > 0) {
      val pct = (subtotal * discountPercent) / 100L
      return if (maxDiscount > 0L) minOf(pct, maxDiscount) else pct
    }
    return 0L
  }
}

object AvailableVouchers {
  val list: List<VoucherPromo> =
    listOf(
      VoucherPromo(
        code = "GRATISONGKIR",
        title = "Gratis Ongkir s/d Rp12.000",
        subtitle = "Min. belanja Rp20.000 • Semua kurir",
        minSpend = 20_000L,
        shippingDiscount = 12_000L,
      ),
      VoucherPromo(
        code = "DMYHEMAT10",
        title = "Diskon 10% s/d Rp15.000",
        subtitle = "Min. belanja Rp30.000 • Khusus DMYmart",
        minSpend = 30_000L,
        discountPercent = 10,
        maxDiscount = 15_000L,
      ),
      VoucherPromo(
        code = "CASHBACK5RB",
        title = "Potongan Langsung Rp5.000",
        subtitle = "Min. belanja Rp25.000 • Hemat harian",
        minSpend = 25_000L,
        flatDiscount = 5_000L,
      ),
      VoucherPromo(
        code = "BELANJA15",
        title = "Diskon Belanja Bulanan Rp15.000",
        subtitle = "Min. belanja Rp100.000 • Semua kategori",
        minSpend = 100_000L,
        flatDiscount = 15_000L,
      ),
    )
}

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val coins: Long = 1000L,
  val walletBalance: Long = 250_000L,
  val lastCheckInDate: String? = null,
  val photoUrl: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Product(
  @DocumentId val id: String = "",
  val name: String = "",
  val description: String = "",
  val categories: List<String> = listOf(MarketplaceCategories.FOOD_DRINK),
  val price: Long = 0L,
  val originalPrice: Long = 0L,
  val stock: Long = 0L,
  val soldCount: Long = 0L,
  val rating: Double = 4.8,
  val unit: String = "",
  val imageKey: String = "food_noodle",
  val sellerId: String = "",
  val sellerName: String = "DMYmart Official",
  @get:PropertyName("isFlashSale") @set:PropertyName("isFlashSale") var isFlashSale: Boolean = false,
  @get:PropertyName("isOfficialStore")
  @set:PropertyName("isOfficialStore")
  var isOfficialStore: Boolean = true,
  @get:PropertyName("isPublished") @set:PropertyName("isPublished") var isPublished: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val discountPercent: Int
    get() =
      if (originalPrice > price && originalPrice > 0L) {
        (((originalPrice - price).toDouble() / originalPrice.toDouble()) * 100).toInt()
      } else {
        0
      }
}

@IgnoreExtraProperties
data class CartItem(
  @DocumentId val id: String = "",
  val productId: String = "",
  val productName: String = "",
  val productImageKey: String = "food_noodle",
  val categories: List<String> = listOf(MarketplaceCategories.FOOD_DRINK),
  val unit: String = "",
  val sellerId: String = "",
  val sellerName: String = "DMYmart Official",
  val price: Long = 0L,
  val originalPrice: Long = 0L,
  val quantity: Long = 1L,
  val stock: Long = 0L,
  @get:PropertyName("isSelected") @set:PropertyName("isSelected") var isSelected: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Order(
  @DocumentId val id: String = "",
  val orderNumber: String = "",
  val status: String = OrderStatus.DIKEMAS,
  val items: List<Map<String, Any>> = emptyList(),
  val itemSummary: String = "",
  val shippingAddress: String = "",
  val recipientName: String = "",
  val recipientPhone: String = "",
  val shippingMethod: String = "",
  val shippingCost: Long = 0L,
  val voucherCode: String = "",
  val discountAmount: Long = 0L,
  val coinsUsed: Long = 0L,
  val subtotal: Long = 0L,
  val totalAmount: Long = 0L,
  val paymentMethod: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

object OrderStatus {
  const val ALL = "SEMUA"
  const val BELUM_BAYAR = "BELUM_BAYAR"
  const val DIKEMAS = "DIKEMAS"
  const val DIKIRIM = "DIKIRIM"
  const val SELESAI = "SELESAI"
  const val DIBATALKAN = "DIBATALKAN"

  fun label(status: String): String =
    when (status) {
      ALL -> "Semua"
      BELUM_BAYAR -> "Belum Bayar"
      DIKEMAS -> "Dikemas"
      DIKIRIM -> "Dikirim"
      SELESAI -> "Selesai"
      DIBATALKAN -> "Dibatalkan"
      else -> status
    }
}

@IgnoreExtraProperties
data class ShippingAddress(
  @DocumentId val id: String = "",
  val label: String = "Rumah",
  val recipientName: String = "",
  val phone: String = "",
  val fullAddress: String = "",
  val notes: String = "",
  @get:PropertyName("isDefault") @set:PropertyName("isDefault") var isDefault: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FavoriteItem(
  @DocumentId val id: String = "",
  val productId: String = "",
  val productName: String = "",
  val price: Long = 0L,
  val imageKey: String = "food_noodle",
  @ServerTimestamp val createdAt: Timestamp? = null,
)
