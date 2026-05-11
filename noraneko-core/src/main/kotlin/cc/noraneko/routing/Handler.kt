package cc.noraneko.routing

import cc.noraneko.http.ApplicationCall
import cc.noraneko.http.Response

typealias Handler = suspend (ApplicationCall) -> Response