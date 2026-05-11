package cc.noraneko

import cc.noraneko.engine.HttpEngine
import cc.noraneko.http.ApplicationCall
import cc.noraneko.http.HttpMethod
import cc.noraneko.http.Request
import cc.noraneko.http.Response
import cc.noraneko.routing.RouteBuilder
import cc.noraneko.routing.RouteMatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class NoranekoDslTest {
    @Test
    fun `start delegates to the configured engine`() {
        val engine = RecordingEngine()

        val application = noraneko {
            server {
                port = 9090
            }

            engine(engine)

            routes {
                get("/") {
                    Response.text("Hello, noraneko")
                }
            }
        }

        application.start()

        assertSame(application, engine.startedApplication)
        assertEquals(9090, application.config.server.port)
    }

    @Test
    fun `router matches method and exact path`() {
        val application = noraneko {
            engine(RecordingEngine())

            routes {
                get("/") {
                    Response.text("GET")
                }

                post("/") {
                    Response.text("POST")
                }
            }
        }

        val getMatch = application.router.find(HttpMethod.GET, "/")
        val postMatch = application.router.find(HttpMethod.POST, "/")

        assertEquals("GET", getMatch?.handleText())
        assertEquals("POST", postMatch?.handleText())
        assertNull(application.router.find(HttpMethod.GET, "/missing"))
    }

    @Test
    fun `router matches path parameters`() {
        val application = noraneko {
            engine(RecordingEngine())

            routes {
                get("/users/{id}") { call ->
                    Response.text(call.requirePathParam("id"))
                }
            }
        }

        val match = application.router.find(HttpMethod.GET, "/users/123")

        assertEquals("123", match?.pathParams?.get("id"))
        assertEquals("123", match?.handleText(path = "/users/123"))
    }

    @Test
    fun `route builder creates route definitions without owning runtime lookup`() {
        val builder = RouteBuilder()

        builder.get("/") {
            Response.text("GET")
        }

        builder.post("/") {
            Response.text("POST")
        }

        val routes = builder.build()
        val router = cc.noraneko.routing.Router(routes)

        assertEquals(listOf(HttpMethod.GET, HttpMethod.POST), routes.map { it.method })
        assertEquals("GET", router.find(HttpMethod.GET, "/")?.handleText())
        assertEquals("POST", router.find(HttpMethod.POST, "/")?.handleText())
    }

    private fun RouteMatch.handleText(
        method: HttpMethod = HttpMethod.GET,
        path: String = "/",
    ): String {
        val call = ApplicationCall(
            request = Request(
                method = method,
                path = path,
            ),
            pathParams = pathParams,
        )

        var failure: Throwable? = null
        var response: Response? = null

        route.handler.startCoroutine(
            call,
            object : Continuation<Response> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<Response>) {
                    failure = result.exceptionOrNull()
                    response = result.getOrNull()
                }
            },
        )

        failure?.let { throw it }

        return requireNotNull(response) {
            "Handler completed without a Response."
        }.body
    }

    private class RecordingEngine : HttpEngine {
        var startedApplication: NoranekoApplication? = null

        override fun start(application: NoranekoApplication) {
            startedApplication = application
        }
    }
}