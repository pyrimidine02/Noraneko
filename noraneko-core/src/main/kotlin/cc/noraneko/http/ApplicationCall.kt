package cc.noraneko.http

class ApplicationCall(
    val request: Request,
    val response: Response = Response()
) {
    fun text(value: String) {
        response.status = 200
        response.contentType = "text/plain"
        response.body = value
    }

    fun pathParam(name: String): String? {
        return null
    }

    fun queryParam(name: String): String? {
        return request.queryParam(name)
    }
}