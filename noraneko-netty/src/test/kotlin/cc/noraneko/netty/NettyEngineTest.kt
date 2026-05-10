package cc.noraneko.netty

import cc.noraneko.noraneko
import java.io.IOException
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class NettyEngineTest {
    @Test
    fun `serves text responses for exact GET routes`() {
        val port = findAvailablePort()
        val engine = NettyEngine()
        val application = noraneko {
            server {
                this.port = port
            }

            engine(engine)

            routes {
                get("/") { call ->
                    call.text("Hello, noraneko")
                }

                get("/health") { call ->
                    call.text("OK")
                }

                get("/inspect") { call ->
                    call.text(
                        listOf(
                            call.request.queryParam("tag").orEmpty(),
                            call.request.queryParams("tag").joinToString(","),
                            call.request.headers["X-Test"].orEmpty(),
                        ).joinToString(":"),
                    )
                }
            }
        }

        val serverThread = Thread {
            application.start()
        }

        serverThread.start()

        try {
            assertEquals("Hello, noraneko", eventuallyGet("http://127.0.0.1:$port/").body())
            assertEquals("OK", get("http://127.0.0.1:$port/health").body())
            assertEquals(
                "one:one,two:present",
                get(
                    url = "http://127.0.0.1:$port/inspect?tag=one&tag=two",
                    headers = mapOf("X-Test" to "present"),
                ).body(),
            )
            assertEquals(404, get("http://127.0.0.1:$port/missing").statusCode())
        } finally {
            engine.stop()
            serverThread.join(3_000)
        }
    }

    private fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()

        headers.forEach { (name, value) ->
            request.header(name, value)
        }

        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun eventuallyGet(url: String): HttpResponse<String> {
        val deadline = System.nanoTime() + 5_000_000_000
        var lastFailure: Throwable? = null

        while (System.nanoTime() < deadline) {
            try {
                return get(url)
            } catch (error: IOException) {
                lastFailure = error
                Thread.sleep(50)
            }
        }

        throw AssertionError("Server did not respond at $url", lastFailure)
    }

    private fun findAvailablePort(): Int =
        ServerSocket(0).use { it.localPort }

    private companion object {
        val httpClient: HttpClient = HttpClient.newHttpClient()
    }
}
