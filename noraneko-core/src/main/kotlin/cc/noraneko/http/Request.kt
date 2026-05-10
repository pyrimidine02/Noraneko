package cc.noraneko.http

data class Request(
    val method: HttpMethod,
    val path: String,
    val queryParameters: Map<String, List<String>> = emptyMap(),
    val headers: Map<String, String> = emptyMap()
) {
    fun queryParam(name: String): String? {
        return queryParameters[name]?.firstOrNull()
    }

    fun queryParams(name: String): List<String> {
        return queryParameters[name].orEmpty()
    }
}