package cc.noraneko.routing

import cc.noraneko.http.ApplicationCall

typealias Handler = suspend (ApplicationCall) -> Unit
