import cc.noraneko.http.Response
import cc.noraneko.netty.NettyEngine
import cc.noraneko.noraneko

fun main() {
    noraneko {
        server {
            port = 8080
        }

        engine(NettyEngine())

        routes {
            get("/") {
                Response.text("Hello, noraneko")
            }

            get("/health") {
                Response.text("OK")
            }

            get("/users/{id}") { call ->
                Response.text("user id = ${call.requirePathParam("id")}")
            }
        }
    }.start()
}