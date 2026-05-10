package cc.noraneko.netty

import cc.noraneko.NoranekoApplication
import cc.noraneko.engine.HttpEngine
import cc.noraneko.http.ApplicationCall
import cc.noraneko.http.HttpMethod
import cc.noraneko.http.Request
import cc.noraneko.http.Response
import cc.noraneko.routing.Route
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.util.CharsetUtil
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class NettyEngine : HttpEngine {
    @Volatile
    private var bossGroup: EventLoopGroup? = null

    @Volatile
    private var workerGroup: EventLoopGroup? = null

    @Volatile
    private var serverChannel: Channel? = null

    override fun start(application: NoranekoApplication) {
        val boss = NioEventLoopGroup(1)
        val worker = NioEventLoopGroup()

        bossGroup = boss
        workerGroup = worker

        try {
            val bootstrap = ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(channel: SocketChannel) {
                            channel.pipeline()
                                .addLast(HttpServerCodec())
                                .addLast(HttpObjectAggregator(MAX_CONTENT_LENGTH))
                                .addLast(NettyHttpHandler(application))
                        }
                    },
                )

            val channel = bootstrap
                .bind(application.config.server.host, application.config.server.port)
                .sync()
                .channel()

            serverChannel = channel
            channel.closeFuture().sync()
        } finally {
            shutdown()
        }
    }

    override fun stop() {
        serverChannel?.close()?.syncUninterruptibly()
        shutdown()
    }

    private fun shutdown() {
        workerGroup?.shutdownGracefully()
        bossGroup?.shutdownGracefully()
        workerGroup = null
        bossGroup = null
        serverChannel = null
    }

    private class NettyHttpHandler(
        private val application: NoranekoApplication,
    ) : SimpleChannelInboundHandler<FullHttpRequest>() {
        override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
            val method = request.method().toNoranekoMethod()
            if (method == null) {
                context.writeAndFlush(textResponse(405, "Method Not Allowed"))
                    .addListener(ChannelFutureListener.CLOSE)
                return
            }

            val decoder = QueryStringDecoder(request.uri())
            val route = application.router.find(method, decoder.path())
            if (route == null) {
                context.writeAndFlush(textResponse(404, "Not Found"))
                    .addListener(ChannelFutureListener.CLOSE)
                return
            }

            val call = ApplicationCall(
                request = Request(
                    method = method,
                    path = decoder.path(),
                    queryParameters = decoder.parameters().mapValues { (_, values) ->
                        values.toList()
                    },
                    headers = request.headers().entries().associate { header ->
                        header.key to header.value
                    },
                ),
                response = Response(),
            )

            try {
                route.handle(call)
                val response = textResponse(
                    status = call.response.status,
                    body = call.response.body,
                    contentType = call.response.contentType,
                )

                if (HttpUtil.isKeepAlive(request)) {
                    response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
                    context.writeAndFlush(response)
                } else {
                    context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
                }
            } catch (error: Throwable) {
                context.writeAndFlush(textResponse(500, "Internal Server Error"))
                    .addListener(ChannelFutureListener.CLOSE)
            }
        }

        private fun Route.handle(call: ApplicationCall) {
            val latch = CountDownLatch(1)
            var failure: Throwable? = null

            handler.startCoroutine(
                call,
                object : Continuation<Unit> {
                    override val context = EmptyCoroutineContext

                    override fun resumeWith(result: Result<Unit>) {
                        failure = result.exceptionOrNull()
                        latch.countDown()
                    }
                },
            )

            latch.await()
            failure?.let { throw it }
        }

        private fun io.netty.handler.codec.http.HttpMethod.toNoranekoMethod(): HttpMethod? =
            runCatching { HttpMethod.valueOf(name()) }.getOrNull()

        private fun textResponse(
            status: Int,
            body: String,
            contentType: String = "text/plain",
        ): DefaultFullHttpResponse {
            val content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8)
            val response = DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(status),
                content,
            )

            response.headers()[HttpHeaderNames.CONTENT_TYPE] = "$contentType; charset=utf-8"
            response.headers()[HttpHeaderNames.CONTENT_LENGTH] = content.readableBytes()

            return response
        }
    }

    private companion object {
        const val MAX_CONTENT_LENGTH = 1_048_576
    }
}
