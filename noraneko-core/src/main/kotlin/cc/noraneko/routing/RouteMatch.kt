package cc.noraneko.routing

data class RouteMatch(
    val route: Route,
    val pathParams: Map<String, String>,
)