package cc.noraneko.http

class ApplicationCall(
    val request: Request,
    private val pathParams: Map<String, String> = emptyMap(),
) {
    fun pathParam(name: String): String? {
        return pathParams[name]
    }

    fun requirePathParam(name: String): String {
        return pathParams[name]
            ?: throw IllegalArgumentException("Missing path parameter: $name")
    }

    fun queryParam(name: String): String? {
        return request.queryParam(name)
    }

    fun queryParams(name: String): List<String> {
        return request.queryParams(name)
    }

    fun header(name: String): String? {
        return request.headers[name]
    }
}