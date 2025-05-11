package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pentabytex.alshafimedledger.data.sharedpreference.SharedPreferencesManager
import com.pentabytex.alshafimedledger.databinding.ActivityForgotPasswordBinding
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.USER_EMAIL
import com.pentabytex.alshafimedledger.utils.InsetsUtil.applyInsetsWithInitialPadding
import com.pentabytex.alshafimedledger.utils.Utils.isValidEmail
import com.pentabytex.alshafimedledger.utils.Utils.showToast
import com.pentabytex.alshafimedledger.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val userViewModel: UserViewModel by viewModels()
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsetsWithInitialPadding(binding.root)

        // Pre-fill email if you have a saved email, but ensure the field remains editable.
        val savedEmail = sharedPreferencesManager.getString(USER_EMAIL, "")
        if(savedEmail.isNotEmpty()){
            binding.etEmail.setText(savedEmail)
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (!email.isValidEmail()) {
                    binding.etEmail.error = "Please enter a valid email address"
                } else {
                    binding.etEmail.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty() || binding.etEmail.error != null) {
                binding.etEmail.error = "Enter your registered email"
                return@setOnClickListener
            }
            setLoading(true)
            userViewModel.forgotPassword(email)
        }

        binding.ivBack.setOnClickListener { finish() }

        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.forgotPasswordState.collect { result ->
                setLoading(false)
                result?.let {
                    if (it.isSuccess) {
                        showToast(this@ForgotPasswordActivity, "Password reset email sent! Check your inbox.")
                        finish()  // Close the activity after success.
                    } else {
                        showToast(this@ForgotPasswordActivity, "Error: ${it.exceptionOrNull()?.message}")
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}