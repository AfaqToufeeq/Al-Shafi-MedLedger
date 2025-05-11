package com.pentabytex.alshafimedledger.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.pentabytex.alshafimedledger.databinding.ActivityRegisterBinding
import com.pentabytex.alshafimedledger.utils.InsetsUtil.applyInsetsWithInitialPadding
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.User

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var loader: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsetsWithInitialPadding(binding.root)

        loader = Utils.progressDialog(this)
        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.userState.collect { firebaseUser ->
                firebaseUser?.let {
                    Utils.showToast(this@RegisterActivity,"Account created successfully!")
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            userViewModel.errorState.collect { error ->
                error?.let { Utils.showToast(this@RegisterActivity,it) }
                toggleProgressBar(false)
            }
        }
    }


    private fun validateAndSubmitUserData() {
        if (validateForm()) {
            toggleProgressBar(true)
            val userData = collectUserData()
            userViewModel.registerUser(userData)
        }
    }


    private fun validateForm(): Boolean = with(binding) {
        return isValidField(textInputLayout) &&
                isValidEmail(textInputLayout2) &&
                isValidField(textInputLayout3)
    }


    private fun isValidField(inputLayout: TextInputLayout): Boolean {
        val value = inputLayout.editText?.text.toString().trim()
        return if (value.isEmpty()) {
            inputLayout.error = getString(R.string.error_required_field)
            false
        } else {
            inputLayout.error = null
            true
        }
    }

    private fun isValidEmail(inputLayout: TextInputLayout): Boolean {
        val value = inputLayout.editText?.text.toString().trim()
        return if (value.isEmpty() || !value.contains("@")) {
            inputLayout.error = getString(R.string.error_invalid_email)
            false
        } else {
            inputLayout.error = null
            true
        }
    }

    private fun collectUserData(): User {
        return with(binding) {
            User(
                name = textInputLayout.editText?.text.toString().trim(),
                email = textInputLayout2.editText?.text.toString().trim(),
                password = textInputLayout3.editText?.text.toString().trim(),
                phone = textInputLayout4.editText?.text.toString().trim(),
                address = textInputLayout6.editText?.text.toString().trim(),
            )
        }
    }

    private fun setupListeners() {
        binding.apply {
            loginTV.setOnClickListener { finish() }
            buttonSignUp.setOnClickListener { validateAndSubmitUserData() }
            backButton.setOnClickListener { finish() }
        }
    }

    private fun toggleProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
