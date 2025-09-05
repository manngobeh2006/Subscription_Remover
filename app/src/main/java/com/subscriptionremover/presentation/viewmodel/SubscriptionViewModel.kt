package com.subscriptionremover.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptionremover.data.models.*
import com.subscriptionremover.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _selectedSubscriptions = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubscriptions: StateFlow<Set<String>> = _selectedSubscriptions.asStateFlow()

    // Data flows
    val allActiveSubscriptions = subscriptionRepository.getAllActiveSubscriptions()
    val activeSubscriptionCount = subscriptionRepository.getActiveSubscriptionCount()
    val totalMonthlySpending = subscriptionRepository.getTotalMonthlySpending()
    val spendingByCategory = subscriptionRepository.getSpendingByCategory()
    val categoryDistribution = subscriptionRepository.getCategoryDistribution()
    val recentSubscriptions = subscriptionRepository.getRecentSubscriptions()

    // Computed properties
    val upcomingBills = flow {
        emit(subscriptionRepository.getUpcomingBills(7))
    }

    val unusedSubscriptions = flow {
        emit(subscriptionRepository.getUnusedSubscriptions(30))
    }

    val recommendations = flow {
        emit(subscriptionRepository.generateRecommendations())
    }

    data class SubscriptionUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isInSelectionMode: Boolean = false,
        val searchQuery: String = "",
        val selectedCategory: SubscriptionCategory? = null,
        val sortBy: SortBy = SortBy.DATE_CREATED,
        val filterBy: FilterBy = FilterBy.ALL
    )

    enum class SortBy(val displayName: String) {
        DATE_CREATED("Date Added"),
        NAME("Name"),
        PRICE_HIGH_TO_LOW("Price (High to Low)"),
        PRICE_LOW_TO_HIGH("Price (Low to High)"),
        LAST_USED("Last Used"),
        NEXT_BILLING("Next Billing")
    }

    enum class FilterBy(val displayName: String) {
        ALL("All"),
        ACTIVE("Active"),
        UNUSED("Unused"),
        SCHEDULED_FOR_CANCELLATION("Scheduled for Cancellation")
    }

    // Actions
    fun addSubscription(subscription: Subscription) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.insertSubscription(subscription).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to add subscription") 
                    }
                }
            )
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.updateSubscription(subscription).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to update subscription") 
                    }
                }
            )
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.deleteSubscription(subscription).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to delete subscription") 
                    }
                }
            )
        }
    }

    fun getSubscriptionsByCategory(category: SubscriptionCategory): Flow<List<Subscription>> {
        return subscriptionRepository.getSubscriptionsByCategory(category)
    }

    fun searchSubscriptions(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getSearchResults(): Flow<List<Subscription>> {
        return _uiState.map { it.searchQuery }.flatMapLatest { query ->
            if (query.isBlank()) {
                allActiveSubscriptions
            } else {
                subscriptionRepository.searchSubscriptions(query)
            }
        }
    }

    // Selection mode for batch operations
    fun toggleSelectionMode() {
        val currentState = _uiState.value
        _uiState.update { 
            it.copy(isInSelectionMode = !currentState.isInSelectionMode) 
        }
        if (!currentState.isInSelectionMode) {
            _selectedSubscriptions.value = emptySet()
        }
    }

    fun toggleSubscriptionSelection(subscriptionId: String) {
        val currentSelection = _selectedSubscriptions.value.toMutableSet()
        if (currentSelection.contains(subscriptionId)) {
            currentSelection.remove(subscriptionId)
        } else {
            currentSelection.add(subscriptionId)
        }
        _selectedSubscriptions.value = currentSelection
    }

    fun selectAllSubscriptions(subscriptionIds: List<String>) {
        _selectedSubscriptions.value = subscriptionIds.toSet()
    }

    fun clearSelection() {
        _selectedSubscriptions.value = emptySet()
    }

    // Batch operations
    fun cancelSelectedSubscriptions() {
        val selectedIds = _selectedSubscriptions.value.toList()
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.cancelSubscriptions(selectedIds).fold(
                onSuccess = { count ->
                    _uiState.update { it.copy(isLoading = false, isInSelectionMode = false) }
                    _selectedSubscriptions.value = emptySet()
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to cancel subscriptions") 
                    }
                }
            )
        }
    }

    fun scheduleSubscriptionCancellations(cancellationDate: LocalDateTime) {
        val selectedIds = _selectedSubscriptions.value.toList()
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.scheduleSubscriptionCancellations(selectedIds, cancellationDate).fold(
                onSuccess = { count ->
                    _uiState.update { it.copy(isLoading = false, isInSelectionMode = false) }
                    _selectedSubscriptions.value = emptySet()
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to schedule cancellations") 
                    }
                }
            )
        }
    }

    // Usage tracking
    fun updateLastUsedDate(subscriptionId: String, lastUsed: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            subscriptionRepository.updateLastUsedDate(subscriptionId, lastUsed)
        }
    }

    fun updateLastUsedByPackage(packageName: String, lastUsed: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            subscriptionRepository.updateLastUsedByPackageName(packageName, lastUsed)
        }
    }

    // Analytics
    suspend fun getSubscriptionUsageStats(subscriptionId: String): SubscriptionUsage? {
        return subscriptionRepository.getSubscriptionUsageStats(subscriptionId)
    }

    // Sorting and filtering
    fun setSortBy(sortBy: SortBy) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: FilterBy) {
        _uiState.update { it.copy(filterBy = filterBy) }
    }

    fun setSelectedCategory(category: SubscriptionCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun getSortedAndFilteredSubscriptions(): Flow<List<Subscription>> {
        return combine(
            allActiveSubscriptions,
            _uiState
        ) { subscriptions, state ->
            var filteredList = subscriptions

            // Apply category filter
            state.selectedCategory?.let { category ->
                filteredList = filteredList.filter { it.category == category }
            }

            // Apply filter
            filteredList = when (state.filterBy) {
                FilterBy.ALL -> filteredList
                FilterBy.ACTIVE -> filteredList.filter { it.isActive }
                FilterBy.UNUSED -> filteredList.filter { it.isUnused(30) }
                FilterBy.SCHEDULED_FOR_CANCELLATION -> filteredList.filter { 
                    it.scheduledCancellationDate != null 
                }
            }

            // Apply sorting
            when (state.sortBy) {
                SortBy.DATE_CREATED -> filteredList.sortedByDescending { it.createdAt }
                SortBy.NAME -> filteredList.sortedBy { it.name.lowercase() }
                SortBy.PRICE_HIGH_TO_LOW -> filteredList.sortedByDescending { it.getMonthlyEquivalentPrice() }
                SortBy.PRICE_LOW_TO_HIGH -> filteredList.sortedBy { it.getMonthlyEquivalentPrice() }
                SortBy.LAST_USED -> filteredList.sortedByDescending { it.lastUsedDate }
                SortBy.NEXT_BILLING -> filteredList.sortedBy { it.nextBillingDate }
            }
        }
    }

    // Data management
    fun syncWithCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.syncWithCloud().fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to sync with cloud") 
                    }
                }
            )
        }
    }

    fun exportSubscriptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            subscriptionRepository.exportSubscriptions().fold(
                onSuccess = { exportData ->
                    _uiState.update { it.copy(isLoading = false) }
                    // Handle export success (e.g., share file, show dialog)
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to export data") 
                    }
                }
            )
        }
    }

    // Error handling
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Helper functions
    fun getMonthlySpendingByCategory(): Flow<Map<SubscriptionCategory, Double>> {
        return spendingByCategory.map { categorySpending ->
            categorySpending.mapValues { (_, amount) -> amount.toDouble() }
        }
    }

    fun getPotentialSavingsFromUnusedSubscriptions(): Flow<Double> {
        return unusedSubscriptions.map { unused ->
            unused.sumOf { it.getMonthlyEquivalentPrice().toDouble() }
        }
    }
}
