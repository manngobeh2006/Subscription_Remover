package com.subscriptionremover.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.subscriptionremover.R
import com.subscriptionremover.data.models.Subscription
import com.subscriptionremover.data.models.getDaysSinceLastUsed
import com.subscriptionremover.databinding.ItemRecentSubscriptionBinding
import java.text.NumberFormat
import java.util.*

class RecentSubscriptionsAdapter(
    private val onItemClick: (Subscription) -> Unit
) : ListAdapter<Subscription, RecentSubscriptionsAdapter.RecentSubscriptionViewHolder>(SubscriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSubscriptionViewHolder {
        val binding = ItemRecentSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecentSubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSubscriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentSubscriptionViewHolder(
        private val binding: ItemRecentSubscriptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            with(binding) {
                // Basic info
                tvSubscriptionName.text = subscription.name
                
                // Price
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                tvPrice.text = formatter.format(subscription.getMonthlyEquivalentPrice().toDouble())
                
                // Category color indicator
                categoryIndicator.setBackgroundColor(Color.parseColor(subscription.category.colorHex))
                
                // Logo
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
                val daysSinceLastUsed = subscription.getDaysSinceLastUsed()
                tvUsageStatus.text = when {
                    daysSinceLastUsed == -1 -> "Never used"
                    daysSinceLastUsed <= 7 -> "Active"
                    else -> "Used $daysSinceLastUsed days ago"
                }
                
                // Click listener
                root.setOnClickListener {
                    onItemClick(subscription)
                }
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
