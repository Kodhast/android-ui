package se.kodhast.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** En apps nycklar i liten skala: tre flikar och några undersidor. */
@Serializable
sealed interface TestKey : NavKey

@Serializable
data object Home : TestKey

@Serializable
data object People : TestKey

@Serializable
data object Shared : TestKey

@Serializable
data object Settings : TestKey

@Serializable
data class Detail(val id: String) : TestKey

@Serializable
data class Edit(val id: String? = null) : TestKey

val TABS: List<TestKey> = listOf(Home, People, Shared)

fun testBackStack(): TabBackStack<TestKey> = TabBackStack(TABS, TestKey.serializer())
