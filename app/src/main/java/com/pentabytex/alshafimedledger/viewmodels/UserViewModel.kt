package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pentabytex.alshafimedledger.data.models.User
import com.pentabytex.alshafimedledger.data.repository.UserRepository
import com.pentabytex.alshafimedledger.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> get() = _loadingState

    private val _userState = MutableStateFlow<FirebaseUser?>(null)
    val userState: StateFlow<FirebaseUser?> get() = _userState

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> get() = _isUserLoggedIn

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _isUserLoggedIn.value = auth.currentUser != null
    }

    private val _forgotPasswordState = MutableStateFlow<Result<Unit>?>(null)
    val forgotPasswordState: StateFlow<Result<Unit>?> get() = _forgotPasswordState

    private val _profileState = MutableStateFlow<User?>(null)
    val profileState: StateFlow<User?> get() = _profileState

    private val _profileListState = MutableStateFlow<List<User>>(emptyList())
    val profileListState: StateFlow<List<User>> get() = _profileListState

    private val _filteredProfileListState = MutableStateFlow<List<User>>(emptyList())
    val filteredProfileListState: StateFlow<List<User>> get() = _filteredProfileListState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery


    private val _errorState = MutableSharedFlow<String>()
    val errorState: SharedFlow<String> get() = _errorState

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage: SharedFlow<String> get() = _successMessage

    private val unKnownError = "Unknown Error"


    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    private val _filteredAllUsers = MutableLiveData<List<User>>()
    val filteredAllUsers: LiveData<List<User>> get() = _filteredAllUsers

    init {
        getCurrentUser()
        userRepository.addAuthStateListener(authStateListener)
    }

    fun registerUser(user: User) {
        viewModelScope.launch {
            _loadingState.value = true
            val result = userRepository.registerUser(user)
            result.onSuccess { _userState.value = it }
            result.onFailure { _errorState.emit(it.message ?: unKnownError) }
            _loadingState.value = false
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loadingState.value = true
            val result = userRepository.loginUser(email, password)
            result.onSuccess { user ->
                _userState.value = user
                _isUserLoggedIn.emit(true)
            }.onFailure { exception ->
                _errorState.emit(exception.message ?: unKnownError)
                _isUserLoggedIn.emit(false)
            }
            _loadingState.value = false
        }
    }



    fun logoutUser() {
        userRepository.logoutUser()
        _userState.value = null
    }

    fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            _loadingState.value = true
            userRepository.fetchUserProfile(
                uid,
                onDataChange = { user -> _profileState.value = user },
                onError = { error -> _errorState.tryEmit(error.message ?: unKnownError) }
            )
            _loadingState.value = false
        }
    }

    fun fetchUserProfiles(uids: List<String>) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val users = userRepository.fetchUsersByUids(uids)
                _profileListState.value = users
                _filteredProfileListState.value = _profileListState.value
            } catch (e: Exception) {
                _errorState.tryEmit(e.message ?: unKnownError)
            } finally {
                _loadingState.value = false
            }
        }
    }

    /** Search users by name or email */
    fun searchAppointmentUsers(query: String) {
        viewModelScope.launch(dispatcherProvider.default) {
            val filteredList = if (query.isEmpty()) {
                _profileListState.value
            } else {
                _profileListState.value.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true)
                }
            }
            _filteredProfileListState.emit(filteredList)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }


    fun updateUserProfile(uid: String, user: User) {
        viewModelScope.launch {
            _loadingState.value = true
            val result = userRepository.updateUserProfile(uid, user)
            result.onSuccess { fetchUserProfile(uid) }
            result.onFailure { _errorState.emit(it.message ?: unKnownError) }
            _loadingState.value = false
        }
    }

    fun deleteUserProfile(uid: String) {
        viewModelScope.launch {
            _loadingState.value = true
            val result = userRepository.deleteUserProfile(uid)
            result.onSuccess { logoutUser() }
            result.onFailure { _errorState.emit(it.message ?: unKnownError) }
            _loadingState.value = false
        }
    }

    private fun getCurrentUser() {
        _userState.value = userRepository.getCurrentUser()
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            val result = userRepository.forgotPassword(email)
            _forgotPasswordState.value = result
        }
    }

    fun checkUserLoginStatus() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            _isUserLoggedIn.emit(currentUser != null)
        }
    }


}
