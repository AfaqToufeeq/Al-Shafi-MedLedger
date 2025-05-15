package com.pentabytex.alshafimedledger.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.FirebaseCollections.MEDICINES
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    private val medicinesRef = databaseReference.child(MEDICINES)


    fun observeMedicines(): Flow<Resource<List<Medicine>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicines = snapshot.children.mapNotNull { it.getValue(Medicine::class.java) }
                trySend(Resource.Success(medicines))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        medicinesRef.addValueEventListener(listener)

        awaitClose { medicinesRef.removeEventListener(listener) }
    }

    suspend fun addMedicine(medicine: Medicine): Result<Unit> {
        return try {
            val id = medicine.id.ifEmpty { databaseReference.push().key ?: System.currentTimeMillis().toString() }
            val newMedicine = medicine.copy(id = id)
            medicinesRef.child(id).setValue(newMedicine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMedicines(): Result<List<Medicine>> {
        return try {
            val snapshot = medicinesRef.get().await()
            val medicines = snapshot.children.mapNotNull { it.getValue(Medicine::class.java) }
            Result.success(medicines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMedicine(medicine: Medicine): Result<Unit> {
        return try {
            if (medicine.id.isEmpty()) return Result.failure(Exception("Invalid medicine ID"))
            medicinesRef.child(medicine.id).setValue(medicine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMedicine(id: String): Result<Unit> {
        return try {
            medicinesRef.child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteMedicinesBulk(medicines: List<Medicine>): Result<Unit> {
        return try {
            medicines.forEach {
                medicinesRef.child(it.id).removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
