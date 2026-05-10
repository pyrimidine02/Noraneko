package cc.noraneko.routing

import cc.noraneko.http.HttpMethod

class RouteBuilder {
    private val routes = mutableListOf<Route>()

    fun get(path: String, handler: Handler) {
        route(HttpMethod.GET, path, handler)
    }

    fun post(path: String, handler: Handler) {
        route(HttpMethod.POST, path, handler)
    }

    fun put(path: String, handler: Handler) {
        route(HttpMethod.PUT, path, handler)
    }

    fun patch(path: String, handler: Handler) {
        route(HttpMethod.PATCH, path, handler)
    }

    fun delete(path: String, handler: Handler) {
        route(HttpMethod.DELETE, path, handler)
    }

    fun options(path: String, handler: Handler) {
        route(HttpMethod.OPTIONS, path, handler)
    }

    fun head(path: String, handler: Handler) {
        route(HttpMethod.HEAD, path, handler)
    }

    fun build(): List<Route> =
        routes.toList()

    private fun route(method: HttpMethod, pathPattern: String, handler: Handler) {
        routes += Route(method, pathPattern, handler)
    }
}
