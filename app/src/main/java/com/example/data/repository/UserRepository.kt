package com.example.data.repository

import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.FavoriteItem
import com.example.data.model.Product
import com.example.data.model.ShippingAddress
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

  fun observeProfile(userId: String): Flow<UserProfile?> {
    val docRef = db.collection("users").document(userId)
    return docRef
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(
            UserProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        } else {
          null
        }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  suspend fun ensureUserProfile(
    userId: String,
    displayName: String,
    email: String,
    photoUrl: String?,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId)
    return try {
      val snapshot = docRef.get().await()
      if (!snapshot.exists()) {
        val payload =
          buildMap<String, Any> {
            put("userId", userId)
            put("displayName", displayName.trim().ifBlank { "Sahabat DMYmart" }.take(100))
            put("email", email.trim().take(150))
            put("coins", 1500L)
            put("walletBalance", 250_000L)
            if (!photoUrl.isNullOrBlank()) {
              put("photoUrl", photoUrl.take(500))
            }
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
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

  suspend fun claimDailyCoins(
    userId: String,
    currentCoins: Long,
    todayDate: String,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId)
    return try {
      docRef
        .update(
          mapOf(
            "coins" to (currentCoins + 250L),
            "lastCheckInDate" to todayDate,
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

  suspend fun updateBalanceAndCoins(
    userId: String,
    newBalance: Long,
    newCoins: Long,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId)
    return try {
      docRef
        .update(
          mapOf(
            "walletBalance" to newBalance.coerceAtLeast(0L),
            "coins" to newCoins.coerceAtLeast(0L),
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

  fun observeAddresses(userId: String): Flow<List<ShippingAddress>> {
    val collectionRef = db.collection("users").document(userId).collection("addresses")
    return collectionRef
      .snapshots()
      .map { snapshot ->
        snapshot
          .toObjects(
            ShippingAddress::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
          .sortedByDescending { it.isDefault }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun addAddress(
    userId: String,
    label: String,
    recipientName: String,
    phone: String,
    fullAddress: String,
    notes: String,
    isDefault: Boolean,
    existingAddresses: List<ShippingAddress>,
  ): Result<String> {
    val collectionRef = db.collection("users").document(userId).collection("addresses")
    val docRef = collectionRef.document()
    return try {
      val batch = db.batch()
      if (isDefault) {
        existingAddresses.filter { it.isDefault && it.id.isNotBlank() }.forEach { addr ->
          batch.update(
            collectionRef.document(addr.id),
            mapOf("isDefault" to false, "updatedAt" to FieldValue.serverTimestamp()),
          )
        }
      }
      batch.set(
        docRef,
        mapOf(
          "label" to label.trim().ifBlank { "Rumah" }.take(50),
          "recipientName" to recipientName.trim().take(120),
          "phone" to phone.trim().take(40),
          "fullAddress" to fullAddress.trim().take(400),
          "notes" to notes.trim().take(200),
          "isDefault" to (isDefault || existingAddresses.isEmpty()),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun setDefaultAddress(
    userId: String,
    addressId: String,
    existingAddresses: List<ShippingAddress>,
  ): Result<Unit> {
    val collectionRef = db.collection("users").document(userId).collection("addresses")
    return try {
      val batch = db.batch()
      existingAddresses.forEach { addr ->
        if (addr.id.isNotBlank()) {
          val shouldBeDefault = addr.id == addressId
          if (addr.isDefault != shouldBeDefault) {
            batch.update(
              collectionRef.document(addr.id),
              mapOf("isDefault" to shouldBeDefault, "updatedAt" to FieldValue.serverTimestamp()),
            )
          }
        }
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, collectionRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteAddress(userId: String, addressId: String): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("addresses").document(addressId)
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

  fun observeFavorites(userId: String): Flow<List<FavoriteItem>> {
    val collectionRef = db.collection("users").document(userId).collection("favorites")
    return collectionRef
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          FavoriteItem::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun toggleFavorite(
    userId: String,
    product: Product,
    currentlyFavorite: Boolean,
  ): Result<Unit> {
    val docRef =
      db.collection("users").document(userId).collection("favorites").document(product.id)
    return try {
      if (currentlyFavorite) {
        docRef.delete().await()
      } else {
        docRef
          .set(
            mapOf(
              "productId" to product.id,
              "productName" to product.name,
              "price" to product.price,
              "imageKey" to product.imageKey.ifBlank { "food_noodle" },
              "createdAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }
}
