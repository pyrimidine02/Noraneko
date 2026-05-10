package cc.noraneko

fun noraneko(block: NoranekoBuilder.() -> Unit): NoranekoApplication =
    NoranekoBuilder().apply(block).build()
