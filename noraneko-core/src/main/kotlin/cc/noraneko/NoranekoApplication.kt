package cc.noraneko

import cc.noraneko.config.ApplicationConfig
import cc.noraneko.engine.HttpEngine
import cc.noraneko.routing.Router

class NoranekoApplication(
    val config: ApplicationConfig,
    val router: Router,
    private val engine: HttpEngine,
) {
    fun start() {
        engine.start(this)
    }

    fun stop() {
        engine.stop()
    }
}
