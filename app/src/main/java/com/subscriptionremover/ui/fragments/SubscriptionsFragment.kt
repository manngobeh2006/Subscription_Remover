package com.subscriptionremover.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.subscriptionremover.R
import com.subscriptionremover.data.models.SubscriptionCategory
import com.subscriptionremover.databinding.FragmentSubscriptionsBinding
import com.subscriptionremover.presentation.viewmodel.SubscriptionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var pagerAdapter: SubscriptionsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupFAB()
        observeData()
    }

    private fun setupViewPager() {
        pagerAdapter = SubscriptionsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Setup tabs
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All"
                1 -> "Entertainment"
                2 -> "Social Media"
                3 -> "Productivity"
                4 -> "Fitness"
                5 -> "News"
                6 -> "Miscellaneous"
                else -> "Other"
            }
        }.attach()
    }

    private fun setupFAB() {
        binding.fabAddSubscription.setOnClickListener {
            // Navigate to add subscription screen
        }

        // Hide/show FAB on scroll
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Could implement scroll-based FAB hiding here
            }
        })
    }

    private fun observeData() {
        lifecycleScope.launch {
            subscriptionViewModel.activeSubscriptionCount.collect { count ->
                binding.tvSubscriptionCount.text = "$count active subscriptions"
            }
        }

        lifecycleScope.launch {
            subscriptionViewModel.totalMonthlySpending.collect { total ->
                binding.tvTotalSpending.text = "$${"%.2f".format(total)}/month"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SubscriptionsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 7

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SubscriptionListFragment.newInstance(null) // All subscriptions
                1 -> SubscriptionListFragment.newInstance(SubscriptionCategory.ENTERTAINMENT)
                2 -> SubscriptionListFragment.newInstance(SubscriptionCategory.SOCIAL_MEDIA)
                3 -> SubscriptionListFragment.newInstance(SubscriptionCategory.PRODUCTIVITY)
                4 -> SubscriptionListFragment.newInstance(SubscriptionCategory.FITNESS)
                5 -> SubscriptionListFragment.newInstance(SubscriptionCategory.NEWS)
                6 -> SubscriptionListFragment.newInstance(SubscriptionCategory.MISCELLANEOUS)
                else -> SubscriptionListFragment.newInstance(null)
            }
        }
    }
}
