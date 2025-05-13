package com.pentabytex.alshafimedledger.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.FirebaseCollections.CUSTOMERS
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    private val customersRef = databaseReference.child(CUSTOMERS)

    fun observeCustomers(): Flow<Resource<List<Customer>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customers = snapshot.children.mapNotNull { it.getValue(Customer::class.java) }
                trySend(Resource.Success(customers))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        customersRef.addValueEventListener(listener)

        awaitClose { customersRef.removeEventListener(listener) }
    }

    suspend fun addCustomer(customer: Customer): Result<Unit> {
        return try {
            val id = customer.id.ifEmpty { customersRef.push().key ?: System.currentTimeMillis().toString() }
            val newCustomer = customer.copy(id = id)
            customersRef.child(id).setValue(newCustomer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomers(): Result<List<Customer>> {
        return try {
            val snapshot = customersRef.get().await()
            val customers = snapshot.children.mapNotNull { it.getValue(Customer::class.java) }
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            if (customer.id.isEmpty()) return Result.failure(Exception("Invalid customer ID"))
            customersRef.child(customer.id).setValue(customer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(id: String): Result<Unit> {
        return try {
            customersRef.child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
