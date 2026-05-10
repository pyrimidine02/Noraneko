package cc.noraneko.routing

import cc.noraneko.http.HttpMethod

data class Route(
    val method: HttpMethod,
    val pathPattern: String,
    val handler: Handler,
)