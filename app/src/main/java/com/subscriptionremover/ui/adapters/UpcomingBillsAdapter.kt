package com.subscriptionremover.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.subscriptionremover.R
import com.subscriptionremover.data.models.Subscription
import com.subscriptionremover.databinding.ItemUpcomingBillBinding
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class UpcomingBillsAdapter(
    private val onItemClick: (Subscription) -> Unit
) : ListAdapter<Subscription, UpcomingBillsAdapter.UpcomingBillViewHolder>(SubscriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingBillViewHolder {
        val binding = ItemUpcomingBillBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UpcomingBillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpcomingBillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UpcomingBillViewHolder(
        private val binding: ItemUpcomingBillBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            with(binding) {
                // Basic info
                tvSubscriptionName.text = subscription.name
                
                // Amount
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                tvAmount.text = formatter.format(subscription.monthlyPrice.toDouble())
                
                // Due date and urgency
                val daysUntilBilling = ChronoUnit.DAYS.between(LocalDate.now(), subscription.nextBillingDate)
                
                tvDueDate.text = when {
                    daysUntilBilling == 0L -> "Due today"
                    daysUntilBilling == 1L -> "Due tomorrow"
                    daysUntilBilling <= 7 -> "Due in $daysUntilBilling days"
                    else -> "Due ${subscription.nextBillingDate.format(DateTimeFormatter.ofPattern("MMM dd"))}"
                }
                
                // Urgency color
                val urgencyColor = when {
                    daysUntilBilling <= 1 -> ContextCompat.getColor(binding.root.context, R.color.error)
                    daysUntilBilling <= 3 -> ContextCompat.getColor(binding.root.context, R.color.warning)
                    else -> ContextCompat.getColor(binding.root.context, R.color.on_surface_variant)
                }
                tvDueDate.setTextColor(urgencyColor)
                urgencyIndicator.setBackgroundColor(urgencyColor)
                
                // Category indicator
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
