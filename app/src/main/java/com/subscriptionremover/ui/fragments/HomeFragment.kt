package com.subscriptionremover.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.subscriptionremover.databinding.FragmentHomeBinding
import com.subscriptionremover.presentation.viewmodel.AuthViewModel
import com.subscriptionremover.presentation.viewmodel.SubscriptionViewModel
import com.subscriptionremover.ui.adapters.RecentSubscriptionsAdapter
import com.subscriptionremover.ui.adapters.UpcomingBillsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    private lateinit var recentSubscriptionsAdapter: RecentSubscriptionsAdapter
    private lateinit var upcomingBillsAdapter: UpcomingBillsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerViews()
        observeData()
    }

    private fun setupUI() {
        // Set greeting based on time of day
        binding.tvGreeting.text = getGreetingMessage()

        // Set up trial banner
        setupTrialBanner()

        // Set up click listeners
        binding.cardTotalSpending.setOnClickListener {
            // Navigate to detailed analytics
        }

        binding.cardActiveSubscriptions.setOnClickListener {
            // Navigate to subscriptions list
        }

        binding.cardUpcomingBills.setOnClickListener {
            // Navigate to bills view
        }

        binding.tvSeeAllRecent.setOnClickListener {
            // Navigate to all subscriptions
        }

        binding.tvSeeAllUpcoming.setOnClickListener {
            // Navigate to upcoming bills
        }

        binding.fabAddSubscription.setOnClickListener {
            // Navigate to add subscription
        }
    }

    private fun setupRecyclerViews() {
        // Recent subscriptions
        recentSubscriptionsAdapter = RecentSubscriptionsAdapter { subscription ->
            // Handle subscription click
        }
        binding.rvRecentSubscriptions.apply {
            adapter = recentSubscriptionsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Upcoming bills
        upcomingBillsAdapter = UpcomingBillsAdapter { subscription ->
            // Handle bill click
        }
        binding.rvUpcomingBills.apply {
            adapter = upcomingBillsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupTrialBanner() {
        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    if (it.isInFreeTrial()) {
                        binding.trialBanner.visibility = View.VISIBLE
                        binding.tvTrialDaysLeft.text = "${it.getTrialDaysRemaining()} days left"
                    } else if (it.isPremium) {
                        binding.trialBanner.visibility = View.GONE
                    } else {
                        binding.trialBanner.visibility = View.VISIBLE
                        binding.tvTrialStatus.text = "Trial Expired"
                        binding.tvTrialDaysLeft.text = "Upgrade to Premium"
                    }
                }
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            // Observe user data
            authViewModel.currentUser.collect { user ->
                user?.let {
                    binding.tvWelcomeUser.text = "Welcome back, ${it.displayName?.split(" ")?.first() ?: "User"}!"
                }
            }
        }

        lifecycleScope.launch {
            // Observe active subscriptions count
            subscriptionViewModel.activeSubscriptionCount.collect { count ->
                binding.tvActiveSubscriptionsCount.text = count.toString()
            }
        }

        lifecycleScope.launch {
            // Observe total monthly spending
            subscriptionViewModel.totalMonthlySpending.collect { total ->
                binding.tvTotalSpendingAmount.text = "$${"%.2f".format(total)}"
            }
        }

        lifecycleScope.launch {
            // Observe recent subscriptions
            subscriptionViewModel.recentSubscriptions.collect { subscriptions ->
                recentSubscriptionsAdapter.submitList(subscriptions.take(5))
                
                if (subscriptions.isEmpty()) {
                    binding.emptyStateRecent.visibility = View.VISIBLE
                    binding.rvRecentSubscriptions.visibility = View.GONE
                } else {
                    binding.emptyStateRecent.visibility = View.GONE
                    binding.rvRecentSubscriptions.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            // Observe upcoming bills
            subscriptionViewModel.upcomingBills.collect { bills ->
                upcomingBillsAdapter.submitList(bills.take(3))
                binding.tvUpcomingBillsCount.text = bills.size.toString()
                
                if (bills.isEmpty()) {
                    binding.emptyStateUpcoming.visibility = View.VISIBLE
                    binding.rvUpcomingBills.visibility = View.GONE
                } else {
                    binding.emptyStateUpcoming.visibility = View.GONE
                    binding.rvUpcomingBills.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            // Observe unused subscriptions for smart recommendations
            subscriptionViewModel.unusedSubscriptions.collect { unused ->
                if (unused.isNotEmpty()) {
                    binding.smartRecommendationCard.visibility = View.VISIBLE
                    binding.tvRecommendationText.text = 
                        "You have ${unused.size} unused ${if (unused.size == 1) "subscription" else "subscriptions"}. Consider canceling to save money."
                } else {
                    binding.smartRecommendationCard.visibility = View.GONE
                }
            }
        }
    }

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
