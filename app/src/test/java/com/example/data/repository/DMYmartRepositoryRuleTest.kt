package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.MarketplaceCategories
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DMYmartRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun productRepository_createAndObserveMultiCategoryProduct_succeeds() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val productRepo = ProductRepository(firestore)

    val uniqueName = "Kopi Susu Gula Aren ${UUID.randomUUID().toString().take(6)}"
    val categories =
      listOf(
        MarketplaceCategories.FOOD_DRINK,
        MarketplaceCategories.PROMO,
      )
    val createResult =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        productRepo.createProduct(
          sellerId = aliceUid,
          sellerName = "Warung Alice",
          name = uniqueName,
          description = "Minuman kopi susu segar minimarket",
          categories = categories,
          price = 9_500L,
          originalPrice = 12_000L,
          stock = 40L,
          unit = "Botol 250ml",
          imageKey = "food_coffee",
          isFlashSale = true,
        )
      }
    assertTrue(createResult.isSuccess)
    val productId = createResult.getOrThrow()

    val products =
      withTimeout(FLOW_TIMEOUT_MS) {
        productRepo.observePublishedProducts().first { list -> list.any { it.id == productId } }
      }
    val saved = products.first { it.id == productId }
    assertEquals(uniqueName, saved.name)
    assertEquals(2, saved.categories.size)
    assertTrue(saved.categories.contains(MarketplaceCategories.FOOD_DRINK))
    assertTrue(saved.categories.contains(MarketplaceCategories.PROMO))
  }

  @Test
  fun productRepository_crossUserUpdate_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val productRepo = ProductRepository(firestore)
    val productId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        productRepo
          .createProduct(
            sellerId = aliceUid,
            sellerName = "Toko Alice",
            name = "Sabun Cuci Piring ${UUID.randomUUID().toString().take(5)}",
            description = "Bersih kesat",
            categories = listOf(MarketplaceCategories.HOUSEHOLD),
            price = 12_500L,
            originalPrice = 15_000L,
            stock = 20L,
            unit = "650ml",
            imageKey = "household_dish",
            isFlashSale = false,
          )
          .getOrThrow()
      }

    signInTestUser(BOB_EMAIL)
    val bobRepo = ProductRepository(firestore)
    val updateResult =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        bobRepo.updateProduct(
          productId = productId,
          name = "Hacked Product",
          description = "Hacked",
          categories = listOf(MarketplaceCategories.HOUSEHOLD),
          price = 100L,
          originalPrice = 100L,
          stock = 1L,
          unit = "1",
          imageKey = "household_dish",
          isFlashSale = false,
        )
      }
    assertTrue(updateResult.isFailure)
    val fsException = updateResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull(fsException)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
  }

  @Test
  fun cartRepository_addUpdateToggleDelete_persistsAcrossQueries() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val cartRepo = CartRepository(firestore)
    val product =
      Product(
        id = "prod_${UUID.randomUUID().toString().take(8)}",
        name = "Indomie Goreng",
        categories = listOf(MarketplaceCategories.FOOD_DRINK, MarketplaceCategories.GROCERIES),
        price = 3_100L,
        originalPrice = 3_500L,
        stock = 50L,
        unit = "85g",
        imageKey = "food_noodle",
        sellerId = aliceUid,
        sellerName = "DMYmart Official",
      )

    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.addOrIncrementToCart(aliceUid, product, 2) }.isSuccess)
    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.addOrIncrementToCart(aliceUid, product, 1) }.isSuccess)

    val cartAfterAdd =
      withTimeout(FLOW_TIMEOUT_MS) {
        cartRepo.observeCart(aliceUid).first { list ->
          list.any { it.id == product.id && it.quantity == 3L }
        }
      }
    assertEquals(3L, cartAfterAdd.first { it.id == product.id }.quantity)

    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.updateQuantity(aliceUid, product.id, 5L) }.isSuccess)
    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.toggleItemSelection(aliceUid, product.id, false) }.isSuccess)

    val cartUpdated =
      withTimeout(FLOW_TIMEOUT_MS) {
        cartRepo.observeCart(aliceUid).first { list ->
          val item = list.firstOrNull { it.id == product.id }
          item != null && item.quantity == 5L && !item.isSelected
        }
      }
    assertEquals(5L, cartUpdated.first { it.id == product.id }.quantity)

    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.removeFromCart(aliceUid, product.id) }.isSuccess)
    val cartAfterDelete =
      withTimeout(FLOW_TIMEOUT_MS) {
        cartRepo.observeCart(aliceUid).first { list -> list.none { it.id == product.id } }
      }
    assertTrue(cartAfterDelete.none { it.id == product.id })
  }

  @Test
  fun cartRepository_unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val cartRepo = CartRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) { cartRepo.observeCart(aliceUid).first() }
      fail("Expected PERMISSION_DENIED when unauthenticated")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  @Test
  fun orderRepository_placeOrderAndTerminalLock_enforcesRules() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val cartRepo = CartRepository(firestore)
    val orderRepo = OrderRepository(firestore)
    val prodId = "prod_${UUID.randomUUID().toString().take(8)}"
    val product =
      Product(
        id = prodId,
        name = "Beras Ramos 5kg",
        categories = listOf(MarketplaceCategories.GROCERIES),
        price = 72_500L,
        originalPrice = 78_000L,
        stock = 20L,
        unit = "5kg",
        imageKey = "pantry_rice",
        sellerId = aliceUid,
        sellerName = "DMYmart Official",
      )

    withTimeout(DEFAULT_TIMEOUT_MS) { cartRepo.addOrIncrementToCart(aliceUid, product, 1) }
    val cartItems =
      withTimeout(FLOW_TIMEOUT_MS) {
        cartRepo.observeCart(aliceUid).first { it.any { item -> item.id == prodId } }
      }

    val orderId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        orderRepo
          .placeOrder(
            userId = aliceUid,
            items = cartItems.filter { it.id == prodId },
            removeCartIds = listOf(prodId),
            shippingAddress = "Jl. Sudirman No. 12, Jakarta",
            recipientName = "Alice",
            recipientPhone = "08123456789",
            shippingMethod = "Reguler 1-2 Jam",
            shippingCost = 5_000L,
            voucherCode = "",
            discountAmount = 0L,
            coinsUsed = 0L,
            subtotal = 72_500L,
            totalAmount = 77_500L,
            paymentMethod = "QRIS",
          )
          .getOrThrow()
      }

    // Cart item was removed atomically
    val remainingCart =
      withTimeout(FLOW_TIMEOUT_MS) {
        cartRepo.observeCart(aliceUid).first { list -> list.none { it.id == prodId } }
      }
    assertTrue(remainingCart.none { it.id == prodId })

    // Progress to SELESAI
    assertTrue(
      withTimeout(DEFAULT_TIMEOUT_MS) {
        orderRepo.updateOrderStatus(aliceUid, orderId, OrderStatus.SELESAI)
      }.isSuccess
    )

    // Terminal lock prevents further mutation
    val afterTerminal =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        orderRepo.updateOrderStatus(aliceUid, orderId, OrderStatus.DIBATALKAN)
      }
    assertTrue(afterTerminal.isFailure)
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      afterTerminal.exceptionOrNull()?.unwrapFirestoreException()?.code,
    )
  }

  private companion object {
    const val ALICE_EMAIL = "alice_dmymart@test.com"
    const val BOB_EMAIL = "bob_dmymart@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
