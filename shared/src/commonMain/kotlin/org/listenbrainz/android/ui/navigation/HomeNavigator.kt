package org.listenbrainz.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.shared.BottomNavDefaults
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.navigation.navigationSavedStateConfiguration

/**
 * Navigation within the home screen. Each tab keeps its own back stack, and the start tab's stays
 * below the current one, so backing out of another tab's root returns to the start tab.
 */
@Stable
class HomeNavigator internal constructor(
    internal val tabStacks: Map<AppNavigationItem, NavBackStack<NavKey>>,
    private val startTab: State<AppNavigationItem>,
    // Null follows the start tab
    private val selectedTabRoute: MutableState<String?>
) {
    val currentTab: AppNavigationItem
        get() = tabStacks.keys.firstOrNull { it.route == selectedTabRoute.value } ?: startTab.value

    private val currentStack: NavBackStack<NavKey>
        get() = tabStacks.getValue(currentTab)

    internal val visibleTabs: List<AppNavigationItem>
        get() = listOf(startTab.value, currentTab).distinct()

    fun navigate(key: NavKey) {
        if (currentStack.lastOrNull() != key) {
            currentStack.add(key)
        }
    }

    fun pop() {
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
        } else {
            selectedTabRoute.value = null
        }
    }

    /** Stays on the current tab when the start tab changes, rather than following it. */
    fun pinCurrentTab() {
        selectedTabRoute.value = currentTab.route
    }

    /** Returns true when [tab] is already showing its root, so its content should scroll to top. */
    fun selectTab(tab: AppNavigationItem): Boolean {
        if (tab == currentTab) {
            val stack = tabStacks.getValue(tab)
            if (stack.size == 1) return true
            while (stack.size > 1) stack.removeAt(stack.lastIndex)
        } else {
            if (currentStack.lastOrNull() == NavigationItem.SettingsScreen) {
                currentStack.removeAt(currentStack.lastIndex)
            }
            selectedTabRoute.value = tab.route
        }
        return false
    }
}

@Composable
fun rememberHomeNavigator(startTab: AppNavigationItem): HomeNavigator {
    val tabStacks = BottomNavDefaults.items().associateWith {
        rememberNavBackStack(navigationSavedStateConfiguration, it.rootKey())
    }
    val selectedTabRoute = rememberSaveable { mutableStateOf<String?>(null) }
    val currentStartTab = rememberUpdatedState(startTab)
    return remember { HomeNavigator(tabStacks, currentStartTab, selectedTabRoute) }
}

private fun AppNavigationItem.rootKey(): NavKey = when (this) {
    AppNavigationItem.Explore -> NavigationItem.ExploreScreen
    AppNavigationItem.Profile -> NavigationItem.ProfileScreen()
    else -> NavigationItem.FeedScreen
}
