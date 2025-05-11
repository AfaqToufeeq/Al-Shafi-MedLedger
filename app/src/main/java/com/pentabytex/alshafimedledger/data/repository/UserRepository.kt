package com.pentabytex.alshafimedledger.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pentabytex.alshafimedledger.data.models.User
import com.pentabytex.alshafimedledger.enums.LogLevel
import com.pentabytex.alshafimedledger.utils.Constants.FirebaseCollections.USERS
import com.pentabytex.alshafimedledger.utils.CoroutineDispatcherProvider
import com.pentabytex.alshafimedledger.utils.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: DatabaseReference,
    private val dispatcher: CoroutineDispatcherProvider
) {

    suspend fun registerUser(user: User): Result<FirebaseUser?> = withContext(dispatcher.io) {
        runCatching {
            val firebaseUser = auth.createUserWithEmailAndPassword(user.email, user.password)
                .await().user ?: error("User registration failed: FirebaseUser is null")

            saveUserToDatabase(user.apply { userId = firebaseUser.uid })
            Utils.log(LogLevel.INFO, "User Registered: ${user.email}")
            firebaseUser
        }.onFailure {
            Utils.log(LogLevel.ERROR, "Registration Failed", throwable = it)
        }
    }

    private suspend fun saveUserToDatabase(user: User) = withContext(dispatcher.io) {
        runCatching {
            database.child(USERS).child(user.userId).setValue(user).await()
            Utils.log(LogLevel.INFO, "User data saved: ${user.userId}")
        }.onFailure {
            Utils.log(LogLevel.ERROR, "Failed to save user data", throwable = it)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> = withContext(dispatcher.io) {
        try {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
                ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "User does not exist")
            Utils.log(LogLevel.INFO, "User Logged In: ${user.email}")
            Result.success(user)
        } catch (exception: Exception) {
            val errorMessage = when (exception) {
                is FirebaseAuthInvalidUserException -> "No account found with this email."
                is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                is FirebaseAuthWeakPasswordException -> "Your password is too weak. Use a stronger password."
                is FirebaseNetworkException -> "No internet connection. Please check your network."
                else -> "Login failed. Please try again."
            }
            Utils.log(LogLevel.ERROR, "Login Failed: $errorMessage", throwable = exception)
            Result.failure(Exception(errorMessage))
        }
    }

    fun logoutUser() {
        auth.signOut()
        Utils.log(LogLevel.INFO, "User Logged Out")
    }


   suspend fun fetchUserProfile(uid: String, onDataChange: (User?) -> Unit, onError: (Exception) -> Unit)
   = withContext(dispatcher.io) {
        database.child(USERS).child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onDataChange(user)
                Utils.log(LogLevel.INFO, "User Profile Updated: $user")
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
                Utils.log(LogLevel.ERROR, "Listening to User Profile Failed", throwable = error.toException())
            }
        })
    }

    suspend fun fetchUsersByUids(uids: List<String>): List<User> = withContext(dispatcher.io) {
        try {
            uids.distinct().map { uid ->
                async {
                    try {
                        val snapshot = database.child(USERS).child(uid).get().await()
                        snapshot.getValue(User::class.java)
                    } catch (e: Exception) {
                        Utils.log(LogLevel.ERROR, "Failed to fetch user: $uid - ${e.message}")
                        null
                    }
                }
            }.awaitAll().filterNotNull().also {
                Utils.log(LogLevel.INFO, "Fetched Users List: $it")
            }
        } catch (e: Exception) {
            Utils.log(LogLevel.ERROR, "Error during fetchUsersByUids: $e")
            throw e
        }
    }



    suspend fun updateUserProfile(uid: String, user: User): Result<Boolean> = withContext(dispatcher.io) {
        runCatching {
            database.child(USERS).child(uid).setValue(user).await()
            Utils.log(LogLevel.INFO, "User Profile Updated: ${user.name}")
            true
        }.onFailure {
            Utils.log(LogLevel.ERROR, "Updating User Profile Failed", throwable = it)
        }
    }

    suspend fun deleteUserProfile(uid: String): Result<Boolean> = withContext(dispatcher.io) {
        runCatching {
            database.child(USERS).child(uid).removeValue().await()
            Utils.log(LogLevel.INFO, "User data deleted from database")

            auth.currentUser?.takeIf { it.uid == uid }?.delete()?.await()
            Utils.log(LogLevel.INFO, "User deleted from Firebase Authentication")

            true
        }.onFailure {
            Utils.log(LogLevel.ERROR, "Deleting user profile failed", throwable = it)
        }
    }



    suspend fun forgotPassword(email: String): Result<Unit> = withContext(dispatcher.io) {
        runCatching {
            auth.sendPasswordResetEmail(email).await()
            Utils.log(LogLevel.INFO, "Password reset email sent to: $email")
        }.onFailure {
            Utils.log(LogLevel.ERROR, "Password reset failed", throwable = it)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser


    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }


}
