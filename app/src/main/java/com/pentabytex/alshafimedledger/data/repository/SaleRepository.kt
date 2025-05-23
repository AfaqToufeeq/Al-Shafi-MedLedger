package com.pentabytex.alshafimedledger.data.repository

import android.util.Log
import com.google.firebase.database.*
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.FirebaseCollections.MEDICINES
import com.pentabytex.alshafimedledger.utils.Constants.FirebaseCollections.SALES
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    private val salesRef = databaseReference.child(SALES)

    fun observeSales(): Flow<Resource<List<Sale>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val saleItems = snapshot.children.mapNotNull { it.getValue(Sale::class.java) }
                trySend(Resource.Success(saleItems))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        salesRef.addValueEventListener(listener)

        awaitClose { salesRef.removeEventListener(listener) }
    }

    suspend fun addSale(sale: Sale): Result<Unit> {
        return try {
            val id = sale.saleId.ifEmpty { salesRef.push().key ?: System.currentTimeMillis().toString() }
            val newSale = sale.copy(saleId = id)
            salesRef.child(id).setValue(newSale).await()
            updateMedicineStockAfterSale(sale.saleItems)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSales(): Result<List<Sale>> {
        return try {
            val snapshot = salesRef.get().await()
            val saleItems = snapshot.children.mapNotNull { it.getValue(Sale::class.java) }
            Result.success(saleItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSaleById(id: String): Result<Sale> {
        return try {
            val snapshot = salesRef.child(id).get().await()
            val sale = snapshot.getValue(Sale::class.java)
            if (sale != null) {
                Result.success(sale)
            } else {
                Result.failure(Exception("Sale not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteSale(id: String): Result<Unit> {
        return try {
            salesRef.child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSalesBulk(sale: List<Sale>): Result<Unit> {
        return try {
            sale.forEach {
                salesRef.child(it.saleId).removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateMedicineStockAfterSale(saleItems: List<SaleItem>) {
        val database = FirebaseDatabase.getInstance().reference

        for (item in saleItems) {
            val medicineRef = database.child(MEDICINES).child(item.medicineId)

            medicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val medicine = snapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        val updatedSoldStock = medicine.soldStock + item.quantitySold
                        val updatedSellingPrice = medicine.sellingPrice + item.totalSellingPrice

                        // Optional: prevent negative stock
                        val safeTotalStock = if (updatedSoldStock < 0 || updatedSoldStock > medicine.totalStock)
                            medicine.totalStock else updatedSoldStock

                        val updates = mapOf(
                            "soldStock" to safeTotalStock,
                            "sellingPrice" to updatedSellingPrice
                        )

                        medicineRef.updateChildren(updates)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MedicineUpdate", "Failed to update stock: ${error.message}")
                }
            })
        }
    }

}
