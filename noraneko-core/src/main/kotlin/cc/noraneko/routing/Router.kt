package cc.noraneko.routing

import cc.noraneko.http.HttpMethod

class Router(
    routes: List<Route> = emptyList(),
) {
    private val registeredRoutes = routes.toList()

    val routes: List<Route>
        get() = registeredRoutes

    fun find(method: HttpMethod, path: String): RouteMatch? {
        for (route in registeredRoutes) {
            if (route.method != method) {
                continue
            }

            val pathParams = matchPath(
                pattern = route.pathPattern,
                path = path,
            )

            if (pathParams != null) {
                return RouteMatch(
                    route = route,
                    pathParams = pathParams,
                )
            }
        }

        return null
    }

    private fun matchPath(
        pattern: String,
        path: String,
    ): Map<String, String>? {
        val patternSegments = splitPath(pattern)
        val pathSegments = splitPath(path)

        if (patternSegments.size != pathSegments.size) {
            return null
        }

        val params = mutableMapOf<String, String>()

        for (index in patternSegments.indices) {
            val patternSegment = patternSegments[index]
            val pathSegment = pathSegments[index]

            if (isPathParameter(patternSegment)) {
                val name = patternSegment
                    .removePrefix("{")
                    .removeSuffix("}")

                if (name.isBlank()) {
                    return null
                }

                params[name] = pathSegment
            } else {
                if (patternSegment != pathSegment) {
                    return null
                }
            }
        }

        return params
    }

    private fun splitPath(path: String): List<String> {
        val normalized = path.trim('/')

        if (normalized.isEmpty()) {
            return emptyList()
        }

        return normalized.split("/")
    }

    private fun isPathParameter(segment: String): Boolean {
        return segment.startsWith("{") && segment.endsWith("}")
    }
}