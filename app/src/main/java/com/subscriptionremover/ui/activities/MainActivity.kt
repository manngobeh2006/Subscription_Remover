package com.subscriptionremover.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.subscriptionremover.R
import com.subscriptionremover.databinding.ActivityMainBinding
import com.subscriptionremover.presentation.viewmodel.AuthViewModel
import com.subscriptionremover.ui.fragments.AnalyticsFragment
import com.subscriptionremover.ui.fragments.HomeFragment
import com.subscriptionremover.ui.fragments.SettingsFragment
import com.subscriptionremover.ui.fragments.SubscriptionsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupBottomNavigation()
        observeAuthState()

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_subscriptions -> {
                    replaceFragment(SubscriptionsFragment())
                    true
                }
                R.id.nav_analytics -> {
                    replaceFragment(AnalyticsFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.isAuthenticated.collect { isAuthenticated ->
                if (!isAuthenticated) {
                    navigateToAuth()
                }
            }
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }
}
