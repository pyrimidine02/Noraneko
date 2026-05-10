package cc.noraneko

import cc.noraneko.config.ApplicationConfig
import cc.noraneko.config.ServerConfig
import cc.noraneko.engine.HttpEngine
import cc.noraneko.routing.RouteBuilder
import cc.noraneko.routing.Router

class NoranekoBuilder {
    private val config = ApplicationConfig()
    private val routeBuilder = RouteBuilder()
    private var engine: HttpEngine? = null

    fun server(block: ServerConfig.() -> Unit) {
        config.server.apply(block)
    }

    fun engine(engine: HttpEngine) {
        this.engine = engine
    }

    fun routes(block: RouteBuilder.() -> Unit) {
        routeBuilder.apply(block)
    }

    fun build(): NoranekoApplication {
        val configuredEngine = requireNotNull(engine) {
            "No HTTP engine configured. Call engine(...) in the noraneko DSL."
        }

        return NoranekoApplication(
            config = config,
            router = Router(routeBuilder.build()),
            engine = configuredEngine,
        )
    }
}
