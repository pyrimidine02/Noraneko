import cc.noraneko.netty.NettyEngine
import cc.noraneko.noraneko

fun main() {
    noraneko {
        server {
            port = 8080
        }

        engine(NettyEngine())

        routes {
            get("/") { call ->
                call.text("Hello, noraneko")
            }

            get("/health") { call ->
                call.text("OK")
            }
        }
    }.start()
}
