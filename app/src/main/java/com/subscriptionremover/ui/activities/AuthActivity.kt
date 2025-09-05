package com.subscriptionremover.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.subscriptionremover.R
import com.subscriptionremover.databinding.ActivityAuthBinding
import com.subscriptionremover.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeAuthState()
    }

    private fun setupUI() {
        updateUIMode()

        // Text change listeners for validation
        binding.etEmail.addTextChangedListener {
            validateEmail()
        }
        
        binding.etPassword.addTextChangedListener {
            validatePassword()
        }
        
        binding.etConfirmPassword.addTextChangedListener {
            validateConfirmPassword()
        }

        // Click listeners
        binding.btnPrimary.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performSignUp()
            }
        }

        binding.tvToggleMode.setOnClickListener {
            toggleMode()
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
        }
    }

    private fun updateUIMode() {
        if (isLoginMode) {
            // Login mode
            binding.tvTitle.text = getString(R.string.welcome_back)
            binding.tvSubtitle.text = "Sign in to manage your subscriptions"
            binding.tilFullName.visibility = android.view.View.GONE
            binding.tilConfirmPassword.visibility = android.view.View.GONE
            binding.btnPrimary.text = getString(R.string.sign_in)
            binding.tvToggleMode.text = getString(R.string.dont_have_account)
            binding.tvForgotPassword.visibility = android.view.View.VISIBLE
        } else {
            // Sign up mode
            binding.tvTitle.text = getString(R.string.create_account)
            binding.tvSubtitle.text = "Start your 7-day free trial today"
            binding.tilFullName.visibility = android.view.View.VISIBLE
            binding.tilConfirmPassword.visibility = android.view.View.VISIBLE
            binding.btnPrimary.text = getString(R.string.sign_up)
            binding.tvToggleMode.text = getString(R.string.already_have_account)
            binding.tvForgotPassword.visibility = android.view.View.GONE
        }
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        updateUIMode()
        clearErrors()
    }

    private fun performLogin() {
        if (!validateInputs()) return

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        lifecycleScope.launch {
            showLoading(true)
            authViewModel.signInWithEmail(email, password)
        }
    }

    private fun performSignUp() {
        if (!validateInputs()) return

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val fullName = binding.etFullName.text.toString().trim()

        lifecycleScope.launch {
            showLoading(true)
            authViewModel.signUpWithEmail(email, password, fullName)
        }
    }

    private fun performGoogleSignIn() {
        lifecycleScope.launch {
            showLoading(true)
            authViewModel.signInWithGoogle()
        }
    }

    private fun showForgotPasswordDialog() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            lifecycleScope.launch {
                authViewModel.sendPasswordResetEmail(email)
            }
        } else {
            Toast.makeText(this, "Please enter a valid email address first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (!validateEmail()) isValid = false
        if (!validatePassword()) isValid = false
        if (!isLoginMode) {
            if (!validateFullName()) isValid = false
            if (!validateConfirmPassword()) isValid = false
        }

        return isValid
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = getString(R.string.error_invalid_email)
                false
            }
            else -> {
                binding.tilEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.etPassword.text.toString()
        return when {
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.tilPassword.error = getString(R.string.error_weak_password)
                false
            }
            else -> {
                binding.tilPassword.error = null
                true
            }
        }
    }

    private fun validateFullName(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        return when {
            fullName.isEmpty() -> {
                binding.tilFullName.error = "Full name is required"
                false
            }
            fullName.length < 2 -> {
                binding.tilFullName.error = "Please enter your full name"
                false
            }
            else -> {
                binding.tilFullName.error = null
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        return when {
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Please confirm your password"
                false
            }
            password != confirmPassword -> {
                binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
                false
            }
            else -> {
                binding.tilConfirmPassword.error = null
                true
            }
        }
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilFullName.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnPrimary.isEnabled = !isLoading
        binding.btnGoogleSignIn.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                showLoading(false)
                
                when (state) {
                    is AuthViewModel.AuthState.Success -> {
                        Toast.makeText(this@AuthActivity, "Welcome!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                    is AuthViewModel.AuthState.Error -> {
                        Toast.makeText(this@AuthActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthViewModel.AuthState.Loading -> {
                        showLoading(true)
                    }
                    is AuthViewModel.AuthState.PasswordResetSent -> {
                        Toast.makeText(this@AuthActivity, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
