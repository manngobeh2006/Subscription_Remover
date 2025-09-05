package com.subscriptionremover.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.subscriptionremover.R
import com.subscriptionremover.data.models.Subscription
import com.subscriptionremover.data.models.getDaysSinceLastUsed
import com.subscriptionremover.databinding.ItemSubscriptionBinding
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SubscriptionsAdapter(
    private val onItemClick: (Subscription) -> Unit,
    private val onCancelClick: (Subscription) -> Unit,
    private val onKeepClick: (Subscription) -> Unit,
    private val onItemLongClick: ((Subscription) -> Unit)? = null
) : ListAdapter<Subscription, SubscriptionsAdapter.SubscriptionViewHolder>(SubscriptionDiffCallback()) {

    private var selectedItems = mutableSetOf<String>()
    private var isSelectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItemAt(position: Int): Subscription? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    fun setSelectionMode(isSelectionMode: Boolean) {
        this.isSelectionMode = isSelectionMode
        if (!isSelectionMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun setSelectedItems(selectedIds: Set<String>) {
        selectedItems.clear()
        selectedItems.addAll(selectedIds)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Subscription> {
        return currentList.filter { selectedItems.contains(it.id) }
    }

    inner class SubscriptionViewHolder(
        private val binding: ItemSubscriptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            with(binding) {
                // Basic subscription info
                tvSubscriptionName.text = subscription.name
                tvDescription.text = subscription.description ?: "No description"
                
                // Price formatting
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                tvPrice.text = formatter.format(subscription.getMonthlyEquivalentPrice().toDouble())
                tvBillingCycle.text = "/${subscription.billingCycle.displayName.lowercase()}"
                
                // Category badge
                tvCategory.text = subscription.category.displayName
                tvCategory.setBackgroundColor(Color.parseColor(subscription.category.colorHex))
                
                // Logo/Icon
                if (!subscription.logoUrl.isNullOrEmpty()) {
                    ivLogo.load(subscription.logoUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_subscription_placeholder)
                        error(R.drawable.ic_subscription_placeholder)
                    }
                } else {
                    ivLogo.setImageResource(getCategoryIcon(subscription.category))
                }
                
                // Usage status
                setupUsageIndicator(subscription)
                
                // Next billing date
                tvNextBilling.text = "Next billing: ${subscription.nextBillingDate.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                )}"
                
                // Selection state
                if (isSelectionMode) {
                    checkboxSelection.visibility = View.VISIBLE
                    checkboxSelection.isChecked = selectedItems.contains(subscription.id)
                    actionButtons.visibility = View.GONE
                } else {
                    checkboxSelection.visibility = View.GONE
                    actionButtons.visibility = View.VISIBLE
                }
                
                // Selection highlight
                val isSelected = selectedItems.contains(subscription.id)
                cardView.strokeWidth = if (isSelected && isSelectionMode) 4 else 0
                cardView.strokeColor = if (isSelected && isSelectionMode) {
                    ContextCompat.getColor(binding.root.context, R.color.primary)
                } else {
                    Color.TRANSPARENT
                }
                
                // Scheduled cancellation indicator
                if (subscription.scheduledCancellationDate != null) {
                    scheduledCancellationIndicator.visibility = View.VISIBLE
                    tvScheduledCancellation.text = "Scheduled for cancellation on ${
                        subscription.scheduledCancellationDate.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                        )
                    }"
                } else {
                    scheduledCancellationIndicator.visibility = View.GONE
                }
                
                // Click listeners
                cardView.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(subscription.id)
                    } else {
                        onItemClick(subscription)
                    }
                }
                
                cardView.setOnLongClickListener {
                    if (!isSelectionMode) {
                        onItemLongClick?.invoke(subscription)
                        true
                    } else {
                        false
                    }
                }
                
                btnCancel.setOnClickListener {
                    onCancelClick(subscription)
                }
                
                btnKeep.setOnClickListener {
                    onKeepClick(subscription)
                }
                
                checkboxSelection.setOnClickListener {
                    toggleSelection(subscription.id)
                }
            }
        }

        private fun setupUsageIndicator(subscription: Subscription) {
            val daysSinceLastUsed = subscription.getDaysSinceLastUsed()
            
            with(binding) {
                when {
                    daysSinceLastUsed == -1 -> {
                        usageIndicator.setBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.on_surface_variant)
                        )
                        tvUsageStatus.text = "Never used"
                        tvUsageStatus.setTextColor(
                            ContextCompat.getColor(binding.root.context, R.color.on_surface_variant)
                        )
                    }
                    daysSinceLastUsed <= 7 -> {
                        usageIndicator.setBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.success)
                        )
                        tvUsageStatus.text = "Active"
                        tvUsageStatus.setTextColor(
                            ContextCompat.getColor(binding.root.context, R.color.success)
                        )
                    }
                    daysSinceLastUsed <= 30 -> {
                        usageIndicator.setBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.warning)
                        )
                        tvUsageStatus.text = "Used $daysSinceLastUsed days ago"
                        tvUsageStatus.setTextColor(
                            ContextCompat.getColor(binding.root.context, R.color.warning)
                        )
                    }
                    else -> {
                        usageIndicator.setBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.error)
                        )
                        tvUsageStatus.text = "Unused for $daysSinceLastUsed days"
                        tvUsageStatus.setTextColor(
                            ContextCompat.getColor(binding.root.context, R.color.error)
                        )
                    }
                }
            }
        }

        private fun toggleSelection(subscriptionId: String) {
            if (selectedItems.contains(subscriptionId)) {
                selectedItems.remove(subscriptionId)
            } else {
                selectedItems.add(subscriptionId)
            }
            
            // Update UI for this item
            val isSelected = selectedItems.contains(subscriptionId)
            binding.checkboxSelection.isChecked = isSelected
            binding.cardView.strokeWidth = if (isSelected && isSelectionMode) 4 else 0
            binding.cardView.strokeColor = if (isSelected && isSelectionMode) {
                ContextCompat.getColor(binding.root.context, R.color.primary)
            } else {
                Color.TRANSPARENT
            }
        }

        private fun getCategoryIcon(category: com.subscriptionremover.data.models.SubscriptionCategory): Int {
            return when (category) {
                com.subscriptionremover.data.models.SubscriptionCategory.ENTERTAINMENT -> R.drawable.ic_entertainment_24
                com.subscriptionremover.data.models.SubscriptionCategory.SOCIAL_MEDIA -> R.drawable.ic_social_media_24
                com.subscriptionremover.data.models.SubscriptionCategory.PRODUCTIVITY -> R.drawable.ic_productivity_24
                com.subscriptionremover.data.models.SubscriptionCategory.FITNESS -> R.drawable.ic_fitness_24
                com.subscriptionremover.data.models.SubscriptionCategory.NEWS -> R.drawable.ic_news_24
                com.subscriptionremover.data.models.SubscriptionCategory.GAMING -> R.drawable.ic_gaming_24
                com.subscriptionremover.data.models.SubscriptionCategory.SHOPPING -> R.drawable.ic_shopping_24
                com.subscriptionremover.data.models.SubscriptionCategory.MUSIC -> R.drawable.ic_music_24
                com.subscriptionremover.data.models.SubscriptionCategory.VIDEO_STREAMING -> R.drawable.ic_video_24
                com.subscriptionremover.data.models.SubscriptionCategory.CLOUD_STORAGE -> R.drawable.ic_cloud_24
                com.subscriptionremover.data.models.SubscriptionCategory.DATING -> R.drawable.ic_dating_24
                com.subscriptionremover.data.models.SubscriptionCategory.FOOD_DELIVERY -> R.drawable.ic_food_24
                com.subscriptionremover.data.models.SubscriptionCategory.MISCELLANEOUS -> R.drawable.ic_misc_24
            }
        }
    }

    class SubscriptionDiffCallback : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem == newItem
        }
    }
}
