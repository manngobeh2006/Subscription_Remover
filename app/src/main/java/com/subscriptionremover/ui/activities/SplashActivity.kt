package com.subscriptionremover.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.subscriptionremover.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen on-screen for longer period
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Simulate some initialization work
        lifecycleScope.launch {
            // Minimum splash screen time
            delay(2000)
            
            // Check authentication status
            authViewModel.isAuthenticated.collect { isAuthenticated ->
                isReady = true
                navigateToNextScreen(isAuthenticated)
            }
        }
    }

    private fun navigateToNextScreen(isAuthenticated: Boolean) {
        val intent = if (isAuthenticated) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
