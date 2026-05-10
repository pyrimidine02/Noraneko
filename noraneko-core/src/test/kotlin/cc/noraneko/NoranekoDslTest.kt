package cc.noraneko

import cc.noraneko.engine.HttpEngine
import cc.noraneko.http.ApplicationCall
import cc.noraneko.http.HttpMethod
import cc.noraneko.http.Request
import cc.noraneko.routing.RouteBuilder
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
                get("/") { call ->
                    call.text("Hello, noraneko")
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
                get("/") { call ->
                    call.text("GET")
                }

                post("/") { call ->
                    call.text("POST")
                }
            }
        }

        val getRoute = application.router.find(HttpMethod.GET, "/")
        val postRoute = application.router.find(HttpMethod.POST, "/")

        assertEquals("GET", getRoute?.handleText())
        assertEquals("POST", postRoute?.handleText())
        assertNull(application.router.find(HttpMethod.GET, "/missing"))
    }

    @Test
    fun `route builder creates route definitions without owning runtime lookup`() {
        val builder = RouteBuilder()

        builder.get("/") { call ->
            call.text("GET")
        }
        builder.post("/") { call ->
            call.text("POST")
        }

        val routes = builder.build()
        val router = cc.noraneko.routing.Router(routes)

        assertEquals(listOf(HttpMethod.GET, HttpMethod.POST), routes.map { it.method })
        assertEquals("GET", router.find(HttpMethod.GET, "/")?.handleText())
        assertEquals("POST", router.find(HttpMethod.POST, "/")?.handleText())
    }

    private fun cc.noraneko.routing.Route.handleText(): String {
        val call = ApplicationCall(
            request = Request(
                method = HttpMethod.GET,
                path = "/"
            )
        )
        var failure: Throwable? = null

        handler.startCoroutine(
            call,
            object : Continuation<Unit> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    failure = result.exceptionOrNull()
                }
            },
        )

        failure?.let { throw it }
        return call.response.body
    }

    private class RecordingEngine : HttpEngine {
        var startedApplication: NoranekoApplication? = null

        override fun start(application: NoranekoApplication) {
            startedApplication = application
        }
    }
}
