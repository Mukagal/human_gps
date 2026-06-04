package com.example.pmadvanced.navigation

object MainRoutes {
    const val MAIN_SCREEN = "MAIN_SCREEN"
    const val CHAT_SCREEN = "CHAT_SCREEN"
    const val PROFILE_SCREEN = "PROFILE_SCREEN"
    const val SEARCH_SCREEN = "SEARCH_SCREEN"
    const val MAP_SCREEN = "MAP_SCREEN"
    const val KOMEK_SCREEN = "KOMEK_SCREEN"

    const val CHAT_GRAPH = "CHAT_GRAPH"
    const val PROFILE_GRAPH = "PROFILE_GRAPH"
    const val MAP_GRAPH = "MAP_GRAPH"
    const val KOMEK_GRAPH = "KOMEK_GRAPH"

    const val PROFILE_WITH_USER = "$PROFILE_SCREEN/{userId}"
    const val MAP_WITH_USER = "$MAP_SCREEN/{userId}"

    fun profile(userId: Int) = "$PROFILE_SCREEN/$userId"
    fun map(userId: Int) = "$MAP_SCREEN/$userId"
}
