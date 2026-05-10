package cc.noraneko.engine

import cc.noraneko.NoranekoApplication

interface HttpEngine {
    fun start(application: NoranekoApplication)

    fun stop() {
    }
}
