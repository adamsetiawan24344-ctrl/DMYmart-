package com.example.data.repository

import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CartItem
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

class CartRepository(private val db: FirebaseFirestore) {

  fun observeCart(userId: String): Flow<List<CartItem>> {
    val collectionRef = db.collection("users").document(userId).collection("cart")
    return collectionRef
      .snapshots()
      .map { snapshot ->
        snapshot
          .toObjects(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun addOrIncrementToCart(
    userId: String,
    product: Product,
    addQuantity: Int = 1,
  ): Result<Unit> {
    require(product.id.isNotBlank()) { "Product ID must not be blank" }
    val docRef = db.collection("users").document(userId).collection("cart").document(product.id)
    val maxAllowed = product.stock.coerceAtLeast(1L).coerceAtMost(999L)
    return try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        val currentQty = existing.getLong("quantity") ?: 1L
        val nextQty = (currentQty + addQuantity.toLong()).coerceIn(1L, maxAllowed)
        docRef
          .update(
            mapOf(
              "quantity" to nextQty,
              "isSelected" to true,
              "price" to product.price,
              "originalPrice" to maxOf(product.originalPrice, product.price),
              "stock" to product.stock,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } else {
        val cleanCategories =
          product.categories.distinct().filter { it.isNotBlank() }.ifEmpty {
            listOf(MarketplaceCategories.FOOD_DRINK)
          }.take(7)
        val payload =
          mapOf(
            "productId" to product.id,
            "productName" to product.name,
            "productImageKey" to product.imageKey.ifBlank { "food_noodle" },
            "categories" to cleanCategories,
            "unit" to product.unit.ifBlank { "1 Pcs" },
            "sellerId" to product.sellerId.ifBlank { userId },
            "sellerName" to product.sellerName.ifBlank { "DMYmart Official" },
            "price" to product.price,
            "originalPrice" to maxOf(product.originalPrice, product.price),
            "quantity" to addQuantity.toLong().coerceIn(1L, maxAllowed),
            "stock" to product.stock,
            "isSelected" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateQuantity(
    userId: String,
    cartItemId: String,
    newQuantity: Long,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("cart").document(cartItemId)
    return try {
      if (newQuantity <= 0L) {
        docRef.delete().await()
      } else {
        docRef
          .update(
            mapOf(
              "quantity" to newQuantity.coerceIn(1L, 999L),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun toggleItemSelection(
    userId: String,
    cartItemId: String,
    isSelected: Boolean,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("cart").document(cartItemId)
    return try {
      docRef
        .update(
          mapOf(
            "isSelected" to isSelected,
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

  suspend fun toggleSelectAll(
    userId: String,
    items: List<CartItem>,
    selectAll: Boolean,
  ): Result<Unit> {
    if (items.isEmpty()) return Result.success(Unit)
    val cartCollection = db.collection("users").document(userId).collection("cart")
    return try {
      val batch = db.batch()
      items.forEach { item ->
        if (item.id.isNotBlank() && item.isSelected != selectAll) {
          batch.update(
            cartCollection.document(item.id),
            mapOf(
              "isSelected" to selectAll,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, cartCollection.path)
      Result.failure(e)
    }
  }

  suspend fun removeFromCart(userId: String, cartItemId: String): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("cart").document(cartItemId)
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

  suspend fun removeSelectedItems(userId: String, cartItemIds: List<String>): Result<Unit> {
    if (cartItemIds.isEmpty()) return Result.success(Unit)
    val cartCollection = db.collection("users").document(userId).collection("cart")
    return try {
      val batch = db.batch()
      cartItemIds.forEach { id ->
        if (id.isNotBlank()) {
          batch.delete(cartCollection.document(id))
        }
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, cartCollection.path)
      Result.failure(e)
    }
  }
}
