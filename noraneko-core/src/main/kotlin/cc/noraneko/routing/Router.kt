package cc.noraneko.routing

import cc.noraneko.http.HttpMethod

class Router(
    routes: List<Route> = emptyList(),
) {
    private val registeredRoutes = routes.toList()

    val routes: List<Route>
        get() = registeredRoutes

    fun find(method: HttpMethod, path: String): Route? =
        registeredRoutes.firstOrNull { route ->
            route.method == method && route.pathPattern == path
        }
}
