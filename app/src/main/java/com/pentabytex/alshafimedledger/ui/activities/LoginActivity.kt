package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.pentabytex.alshafimedledger.databinding.ActivityLoginBinding
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.utils.Utils.showToast
import com.pentabytex.alshafimedledger.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply { setKeepOnScreenCondition { false } }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        observeViewModel()
        initializeUI()
    }


    private fun initializeUI() {
        binding.apply {

            signUpTV.setOnClickListener {
                navigateToActivity(this@LoginActivity,
                    RegisterActivity::class.java,
                    isAnimation = true
                )
            }

            forgotPassTV.setOnClickListener {
                navigateToActivity(this@LoginActivity,
                    ForgotPasswordActivity::class.java,
                    isAnimation = true)
            }
            buttonLogin.setOnClickListener { loginUser() }
        }
    }

    private fun observeViewModel() {

        lifecycleScope.launch {
            authViewModel.isUserLoggedIn.collect { flag ->
                if (flag)
                    navigateToActivity(this@LoginActivity, MainActivity::class.java, finishCurrentActivity = true, isAnimation = true)
                else
                    showToast(this@LoginActivity, "Failed to login!")
            }
        }


        lifecycleScope.launch {
            authViewModel.errorState.collect { error ->
                if (error != null) {
                    showToast(this@LoginActivity, error)
                }
            }
        }

        //Observer Progress loading
        lifecycleScope.launch {
            authViewModel.loadingState.collectLatest { isLoading ->
                binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }


    private fun loginUser() {
        val email = binding.loginIdText.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        authViewModel.loginUser(email, password)
    }


    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast(this, "Email cannot be empty")
                false
            }
            password.isEmpty() -> {
                showToast(this, "Password cannot be empty")
                false
            }
            else -> true
        }
    }

}
