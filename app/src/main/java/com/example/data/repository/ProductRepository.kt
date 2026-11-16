package com.example.data.repository

import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.MarketplaceCategories
import com.example.data.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProductRepository(private val db: FirebaseFirestore) {

  fun observePublishedProducts(): Flow<List<Product>> {
    val query = db.collection(COLLECTION_PRODUCTS).whereEqualTo("isPublished", true)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          Product::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, COLLECTION_PRODUCTS)
        throw error
      }
  }

  fun observeSellerProducts(sellerId: String): Flow<List<Product>> {
    val query = db.collection(COLLECTION_PRODUCTS).whereEqualTo("sellerId", sellerId)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          Product::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, COLLECTION_PRODUCTS)
        throw error
      }
  }

  suspend fun getProductById(productId: String): Result<Product?> {
    val docRef = db.collection(COLLECTION_PRODUCTS).document(productId)
    return try {
      val snapshot = docRef.get().await()
      Result.success(
        snapshot.toObject(
          Product::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun createProduct(
    sellerId: String,
    sellerName: String,
    name: String,
    description: String,
    categories: List<String>,
    price: Long,
    originalPrice: Long,
    stock: Long,
    unit: String,
    imageKey: String,
    isFlashSale: Boolean,
    isOfficialStore: Boolean = false,
  ): Result<String> {
    val docRef = db.collection(COLLECTION_PRODUCTS).document()
    val cleanCategories =
      categories.distinct().filter { it.isNotBlank() }.ifEmpty {
        listOf(MarketplaceCategories.FOOD_DRINK)
      }.take(7)
    val safeOriginalPrice = maxOf(originalPrice, price)
    val payload =
      mapOf(
        "name" to name.trim(),
        "description" to description.trim(),
        "categories" to cleanCategories,
        "price" to price.coerceIn(100L, 100_000_000L),
        "originalPrice" to safeOriginalPrice.coerceIn(100L, 100_000_000L),
        "stock" to stock.coerceIn(0L, 100_000L),
        "soldCount" to 0L,
        "rating" to 5.0,
        "unit" to unit.trim().ifBlank { "1 Pcs" },
        "imageKey" to imageKey.ifBlank { "food_noodle" },
        "sellerId" to sellerId,
        "sellerName" to sellerName.trim().ifBlank { "Toko DMYmart" },
        "isFlashSale" to isFlashSale,
        "isOfficialStore" to isOfficialStore,
        "isPublished" to true,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateProduct(
    productId: String,
    name: String,
    description: String,
    categories: List<String>,
    price: Long,
    originalPrice: Long,
    stock: Long,
    unit: String,
    imageKey: String,
    isFlashSale: Boolean,
    isPublished: Boolean = true,
  ): Result<Unit> {
    val docRef = db.collection(COLLECTION_PRODUCTS).document(productId)
    val cleanCategories =
      categories.distinct().filter { it.isNotBlank() }.ifEmpty {
        listOf(MarketplaceCategories.FOOD_DRINK)
      }.take(7)
    val safeOriginalPrice = maxOf(originalPrice, price)
    val updates =
      mapOf(
        "name" to name.trim(),
        "description" to description.trim(),
        "categories" to cleanCategories,
        "price" to price.coerceIn(100L, 100_000_000L),
        "originalPrice" to safeOriginalPrice.coerceIn(100L, 100_000_000L),
        "stock" to stock.coerceIn(0L, 100_000L),
        "unit" to unit.trim().ifBlank { "1 Pcs" },
        "imageKey" to imageKey.ifBlank { "food_noodle" },
        "isFlashSale" to isFlashSale,
        "isPublished" to isPublished,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    return try {
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteProduct(productId: String): Result<Unit> {
    val docRef = db.collection(COLLECTION_PRODUCTS).document(productId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun seedDefaultCatalogIfEmpty(sellerUid: String): Result<Unit> {
    return try {
      val existing =
        db.collection(COLLECTION_PRODUCTS).whereEqualTo("isPublished", true).limit(1).get().await()
      if (!existing.isEmpty) {
        return Result.success(Unit)
      }

      val batch = db.batch()
      defaultMinimarketSeed(sellerUid).forEach { (docId, data) ->
        batch.set(db.collection(COLLECTION_PRODUCTS).document(docId), data)
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, COLLECTION_PRODUCTS)
      Result.failure(e)
    }
  }

  private fun defaultMinimarketSeed(sellerUid: String): List<Pair<String, Map<String, Any>>> {
    data class SeedSpec(
      val id: String,
      val name: String,
      val desc: String,
      val categories: List<String>,
      val price: Long,
      val orig: Long,
      val stock: Long,
      val sold: Long,
      val rating: Double,
      val unit: String,
      val imageKey: String,
      val flash: Boolean,
    )

    val specs =
      listOf(
        SeedSpec(
          "dmy_indomie_goreng_5",
          "Indomie Mi Goreng Spesial (Isi 5 Bungkus)",
          "Paket hemat 5 bungkus Indomie Mi Goreng Spesial lengkap dengan bumbu gurih dan bawang goreng renyah. Cocok untuk stok dapur & anak kos.",
          listOf(
            MarketplaceCategories.FOOD_DRINK,
            MarketplaceCategories.GROCERIES,
            MarketplaceCategories.PROMO,
          ),
          14_900L,
          17_500L,
          85L,
          2450L,
          4.9,
          "Pack 5 x 85g",
          "food_noodle",
          true,
        ),
        SeedSpec(
          "dmy_minyak_bimoli_2l",
          "Minyak Goreng Bimoli Klasik Pouch 2 Liter",
          "Minyak goreng kelapa sawit murni dengan pemurnian multi-proses, jernih dan renyah untuk setiap masakan keluarga.",
          listOf(
            MarketplaceCategories.GROCERIES,
            MarketplaceCategories.HOUSEHOLD,
            MarketplaceCategories.PROMO,
          ),
          35_500L,
          41_000L,
          42L,
          1820L,
          4.9,
          "Pouch 2 Liter",
          "pantry_oil",
          true,
        ),
        SeedSpec(
          "dmy_beras_ramos_5kg",
          "Beras Premium Setra Ramos Pulen 5 Kg",
          "Beras putih pilihan bebas pemutih dan pewangi buatan, nasi pulen dan wangi alami untuk makan sehari-hari.",
          listOf(MarketplaceCategories.GROCERIES, MarketplaceCategories.FOOD_DRINK),
          72_500L,
          78_000L,
          30L,
          960L,
          4.8,
          "Karung 5 Kg",
          "pantry_rice",
          false,
        ),
        SeedSpec(
          "dmy_ultra_milk_1l",
          "Susu UHT Ultra Milk Full Cream 1000ml",
          "Susu segar UHT alami kaya kalsium, protein, dan vitamin. Baik untuk sarapan bergizi anak hingga dewasa.",
          listOf(
            MarketplaceCategories.FOOD_DRINK,
            MarketplaceCategories.BABY_MOM,
            MarketplaceCategories.HEALTH,
          ),
          18_800L,
          22_000L,
          64L,
          1540L,
          4.9,
          "Kotak 1 Liter",
          "food_milk",
          true,
        ),
        SeedSpec(
          "dmy_kopi_kapal_api",
          "Kopi Kapal Api Special Mix Isi 10 Sachet",
          "Paduan biji kopi pilihan dan gula murni menghasilkan aroma khas yang nikmat untuk mengawali pagi.",
          listOf(MarketplaceCategories.FOOD_DRINK, MarketplaceCategories.GROCERIES),
          15_200L,
          18_000L,
          110L,
          1120L,
          4.8,
          "Renceng 10 x 24g",
          "food_coffee",
          false,
        ),
        SeedSpec(
          "dmy_chitato_sapi_68g",
          "Chitato Keripik Kentang Rasa Sapi Panggang 68g",
          "Keripik kentang asli dengan potongan bergelombang dan bumbu sapi panggang melimpah yang renyah.",
          listOf(MarketplaceCategories.FOOD_DRINK, MarketplaceCategories.PROMO),
          10_500L,
          13_000L,
          95L,
          3100L,
          4.9,
          "Pouch 68g",
          "food_snack",
          true,
        ),
        SeedSpec(
          "dmy_teh_pucuk_6",
          "Teh Pucuk Harum Melati Paket 6 Botol x 350ml",
          "Minuman teh melati segar dari pucuk daun teh pilihan, pas manisnya dan menyegarkan.",
          listOf(MarketplaceCategories.FOOD_DRINK, MarketplaceCategories.PROMO),
          19_900L,
          24_000L,
          55L,
          1380L,
          4.9,
          "6 Botol x 350ml",
          "food_drink",
          true,
        ),
        SeedSpec(
          "dmy_lifebuoy_450ml",
          "Lifebuoy Sabun Mandi Cair Total 10 Refill 450ml",
          "Sabun antibakteri dengan formula Active Silver+ melindungi keluarga dari kuman hingga 10x lebih efektif.",
          listOf(
            MarketplaceCategories.PERSONAL_CARE,
            MarketplaceCategories.HEALTH,
            MarketplaceCategories.HOUSEHOLD,
          ),
          22_900L,
          29_500L,
          50L,
          1760L,
          4.9,
          "Refill 450ml",
          "care_soap",
          true,
        ),
        SeedSpec(
          "dmy_pepsodent_190g",
          "Pasta Gigi Pepsodent Pencegah Gigi Berlubang 190g",
          "Perlindungan maksimal untuk gigi kuat dan gusi sehat dengan mikro kalsium aktif.",
          listOf(MarketplaceCategories.PERSONAL_CARE, MarketplaceCategories.HEALTH),
          13_800L,
          16_500L,
          75L,
          1420L,
          4.8,
          "Tube 190g",
          "care_dental",
          false,
        ),
        SeedSpec(
          "dmy_clear_shampoo_300ml",
          "Sampo Clear Ice Cool Menthol Anti Ketombe 300ml",
          "Sensasi dingin mentol menyegarkan kulit kepala serta membersihkan ketombe membandel.",
          listOf(MarketplaceCategories.PERSONAL_CARE),
          34_900L,
          42_000L,
          38L,
          890L,
          4.8,
          "Botol 300ml",
          "care_shampoo",
          false,
        ),
        SeedSpec(
          "dmy_rinso_molto_770g",
          "Deterjen Bubuk Rinso Molto Perfume Essence 770g",
          "Hilangkan noda membandel dalam 1x kucek dan berikan keharuman segar tahan lama hingga 21 hari.",
          listOf(MarketplaceCategories.HOUSEHOLD, MarketplaceCategories.PROMO),
          21_500L,
          27_000L,
          60L,
          1630L,
          4.9,
          "Bag 770g",
          "household_detergent",
          true,
        ),
        SeedSpec(
          "dmy_sunlight_jeruk_650ml",
          "Sunlight Sabun Cuci Piring Jeruk Nipis Refill 650ml",
          "Ekstrak jeruk nipis asli 5x lebih cepat bersihkan lemak membandel pada piring dan peralatan dapur.",
          listOf(MarketplaceCategories.HOUSEHOLD, MarketplaceCategories.GROCERIES),
          12_900L,
          16_000L,
          90L,
          2240L,
          4.9,
          "Pouch 650ml",
          "household_dish",
          false,
        ),
        SeedSpec(
          "dmy_tisu_paseo_250",
          "Tisu Wajah Paseo Smart Soft Pack 250 Sheets",
          "Tisu lembut 2 ply higienis bebas bahan kimia berbahaya, aman untuk wajah dan kulit bayi.",
          listOf(
            MarketplaceCategories.HOUSEHOLD,
            MarketplaceCategories.PERSONAL_CARE,
            MarketplaceCategories.BABY_MOM,
          ),
          11_500L,
          14_500L,
          100L,
          1950L,
          4.9,
          "Pack 250 Lembar",
          "household_tissue",
          false,
        ),
        SeedSpec(
          "dmy_mamypoko_pants_m32",
          "MamyPoko Pants X-tra Kering Popok Bayi M32",
          "Daya serap tinggi hingga 10 jam menjaga kulit si kecil tetap kering, lembut, dan bebas ruam.",
          listOf(MarketplaceCategories.BABY_MOM, MarketplaceCategories.PROMO),
          54_900L,
          65_000L,
          28L,
          740L,
          4.9,
          "Size M (32 Pcs)",
          "baby_diaper",
          true,
        ),
        SeedSpec(
          "dmy_enervon_c_30",
          "Enervon-C Multivitamin Vitamin C & B Kompleks (30 Tab)",
          "Suplemen harian menjaga daya tahan tubuh agar tetap fit dan bertenaga sepanjang hari.",
          listOf(MarketplaceCategories.HEALTH, MarketplaceCategories.PERSONAL_CARE),
          38_500L,
          45_000L,
          45L,
          1180L,
          4.9,
          "Botol 30 Tablet",
          "health_vitamin",
          false,
        ),
        SeedSpec(
          "dmy_paket_sarapan_hemat",
          "Paket Sarapan Hemat: Roti Tawar + Selai Cokelat + Susu UHT",
          "Bundle spesial sarapan keluarga lebih hemat Rp10.000! Sudah termasuk roti tawar lembut, selai cokelat, dan susu UHT 1L.",
          listOf(
            MarketplaceCategories.PROMO,
            MarketplaceCategories.FOOD_DRINK,
            MarketplaceCategories.GROCERIES,
          ),
          32_900L,
          43_000L,
          25L,
          680L,
          5.0,
          "Paket 3 Item",
          "promo_bundle",
          true,
        ),
      )

    return specs.map { spec ->
      spec.id to
        mapOf(
          "name" to spec.name,
          "description" to spec.desc,
          "categories" to spec.categories,
          "price" to spec.price,
          "originalPrice" to spec.orig,
          "stock" to spec.stock,
          "soldCount" to spec.sold,
          "rating" to spec.rating,
          "unit" to spec.unit,
          "imageKey" to spec.imageKey,
          "sellerId" to sellerUid,
          "sellerName" to "DMYmart Official",
          "isFlashSale" to spec.flash,
          "isOfficialStore" to true,
          "isPublished" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
    }
  }

  companion object {
    const val COLLECTION_PRODUCTS = "products"
  }
}
