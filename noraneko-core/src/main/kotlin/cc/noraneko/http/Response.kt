package cc.noraneko.http

data class Response(
    val status: Int,
    val body: String = "",
    val contentType: String = "text/plain",
    val headers: Map<String, String> = emptyMap(),
) {
    companion object {
        fun text(
            value: String,
            status: Int = 200,
            headers: Map<String, String> = emptyMap(),
        ): Response {
            return Response(
                status = status,
                body = value,
                contentType = "text/plain",
                headers = headers,
            )
        }

        fun html(
            value: String,
            status: Int = 200,
            headers: Map<String, String> = emptyMap(),
        ): Response {
            return Response(
                status = status,
                body = value,
                contentType = "text/html",
                headers = headers,
            )
        }

        fun notFound(): Response {
            return text("Not Found", status = 404)
        }

        fun methodNotAllowed(): Response {
            return text("Method Not Allowed", status = 405)
        }

        fun internalServerError(): Response {
            return text("Internal Server Error", status = 500)
        }

        fun noContent(): Response {
            return Response(status = 204)
        }
    }
}