package com.subscriptionremover.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.subscriptionremover.data.models.SubscriptionCategory
import com.subscriptionremover.databinding.FragmentSubscriptionListBinding
import com.subscriptionremover.presentation.viewmodel.SubscriptionViewModel
import com.subscriptionremover.ui.adapters.SubscriptionsAdapter
import com.subscriptionremover.ui.helpers.SwipeToActionCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionListFragment : Fragment() {

    private var _binding: FragmentSubscriptionListBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var subscriptionsAdapter: SubscriptionsAdapter

    private var category: SubscriptionCategory? = null

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: SubscriptionCategory?): SubscriptionListFragment {
            return SubscriptionListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getSerializable(ARG_CATEGORY) as? SubscriptionCategory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeActions()
        observeData()
    }

    private fun setupRecyclerView() {
        subscriptionsAdapter = SubscriptionsAdapter(
            onItemClick = { subscription ->
                // Navigate to subscription details
            },
            onCancelClick = { subscription ->
                // Show cancel confirmation dialog
            },
            onKeepClick = { subscription ->
                // Mark subscription as kept/important
            }
        )

        binding.recyclerView.apply {
            adapter = subscriptionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSwipeActions() {
        val swipeCallback = SwipeToActionCallback(
            context = requireContext(),
            onSwipeLeft = { position ->
                // Cancel subscription
                val subscription = subscriptionsAdapter.getItemAt(position)
                subscription?.let {
                    // Show cancel confirmation
                }
            },
            onSwipeRight = { position ->
                // Keep subscription
                val subscription = subscriptionsAdapter.getItemAt(position)
                subscription?.let {
                    // Mark as kept/favorite
                }
            }
        )

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeData() {
        lifecycleScope.launch {
            if (category != null) {
                subscriptionViewModel.getSubscriptionsByCategory(category!!).collect { subscriptions ->
                    subscriptionsAdapter.submitList(subscriptions)
                    updateEmptyState(subscriptions.isEmpty())
                }
            } else {
                subscriptionViewModel.allActiveSubscriptions.collect { subscriptions ->
                    subscriptionsAdapter.submitList(subscriptions)
                    updateEmptyState(subscriptions.isEmpty())
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateGroup.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            
            binding.tvEmptyTitle.text = if (category != null) {
                "No ${category!!.displayName.lowercase()} subscriptions"
            } else {
                "No subscriptions found"
            }
            
            binding.tvEmptyMessage.text = if (category != null) {
                "Add your first ${category!!.displayName.lowercase()} subscription to get started"
            } else {
                "Add your first subscription to start tracking your expenses"
            }
        } else {
            binding.emptyStateGroup.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
