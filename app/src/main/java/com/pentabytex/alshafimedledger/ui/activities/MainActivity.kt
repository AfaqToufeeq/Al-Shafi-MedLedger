package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.databinding.ActivityMainBinding
import com.pentabytex.alshafimedledger.utils.InsetsUtil.applyInsetsWithInitialPadding
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.utils.Utils.setStatusBarColor
import com.pentabytex.alshafimedledger.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setStatusBarColor(this, R.color.home_status_bar)
        setContentView(binding.root)
        applyInsetsWithInitialPadding(binding.root)

        userViewModel.checkUserLoginStatus()

        observeViewModel()
        setUpNavigation()

    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                else -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.isUserLoggedIn.collect { isLoggedIn ->
                Log.d("MainActivity", "User login status: $isLoggedIn")
                if (!isLoggedIn) {
                    startLoginActivity()
                }
            }
        }
    }

    private fun startLoginActivity() {
        navigateToActivity(this, LoginActivity::class.java, finishCurrentActivity = true)
    }
}