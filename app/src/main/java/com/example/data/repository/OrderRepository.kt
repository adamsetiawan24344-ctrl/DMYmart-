package com.example.data.repository

import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrderRepository(private val db: FirebaseFirestore) {

  fun observeOrders(userId: String): Flow<List<Order>> {
    val collectionRef = db.collection("users").document(userId).collection("orders")
    return collectionRef
      .snapshots()
      .map { snapshot ->
        snapshot
          .toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun placeOrder(
    userId: String,
    items: List<CartItem>,
    removeCartIds: List<String>,
    shippingAddress: String,
    recipientName: String,
    recipientPhone: String,
    shippingMethod: String,
    shippingCost: Long,
    voucherCode: String,
    discountAmount: Long,
    coinsUsed: Long,
    subtotal: Long,
    totalAmount: Long,
    paymentMethod: String,
  ): Result<String> {
    require(items.isNotEmpty()) { "Order must contain at least one item" }
    val ordersCollection = db.collection("users").document(userId).collection("orders")
    val cartCollection = db.collection("users").document(userId).collection("cart")
    val orderDocRef = ordersCollection.document()

    val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    val suffix = orderDocRef.id.take(5).uppercase(Locale.US)
    val orderNumber = "DMY-$datePrefix-$suffix"
    val initialStatus =
      if (paymentMethod.contains("Transfer", ignoreCase = true)) {
        OrderStatus.BELUM_BAYAR
      } else {
        OrderStatus.DIKEMAS
      }

    val serializedItems =
      items.map { item ->
        mapOf(
          "productId" to item.productId,
          "productName" to item.productName,
          "productImageKey" to item.productImageKey,
          "unit" to item.unit,
          "sellerName" to item.sellerName,
          "price" to item.price,
          "quantity" to item.quantity,
        )
      }

    val firstItem = items.first()
    val summary =
      if (items.size == 1) {
        "${firstItem.productName} (${firstItem.quantity}x)"
      } else {
        "${firstItem.productName} (${firstItem.quantity}x) + ${items.size - 1} produk lainnya"
      }

    val payload =
      mapOf(
        "orderNumber" to orderNumber,
        "status" to initialStatus,
        "items" to serializedItems,
        "itemSummary" to summary.take(300),
        "shippingAddress" to shippingAddress.trim().take(400),
        "recipientName" to recipientName.trim().take(120),
        "recipientPhone" to recipientPhone.trim().take(40),
        "shippingMethod" to shippingMethod.take(80),
        "shippingCost" to shippingCost.coerceAtLeast(0L),
        "voucherCode" to voucherCode.take(40),
        "discountAmount" to discountAmount.coerceAtLeast(0L),
        "coinsUsed" to coinsUsed.coerceAtLeast(0L),
        "subtotal" to subtotal.coerceAtLeast(0L),
        "totalAmount" to totalAmount.coerceAtLeast(0L),
        "paymentMethod" to paymentMethod.take(60),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )

    return try {
      val batch = db.batch()
      batch.set(orderDocRef, payload)
      removeCartIds.forEach { cartId ->
        if (cartId.isNotBlank()) {
          batch.delete(cartCollection.document(cartId))
        }
      }
      batch.commit().await()
      Result.success(orderDocRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, orderDocRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateOrderStatus(
    userId: String,
    orderId: String,
    newStatus: String,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("orders").document(orderId)
    return try {
      docRef
        .update(
          mapOf(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }
}
