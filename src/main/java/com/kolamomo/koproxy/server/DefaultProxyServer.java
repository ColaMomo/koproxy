package com.kolamomo.koproxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Created by jiangchao on 16/2/18.
 */
public class DefaultProxyServer {
    private final int port;

    private final ProxyFilterFactory filterFactory;

    private final ServerBootstrap serverBootstrap;

    private final ProxyRouter router;

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    private final ExecutorService handlerExecutor;

    private final int maxRequestSize;

    private final int idleTimeout;

    private Timer timer;

    public DefaultProxyServer(int port) {
        this.port = port;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChildChannelHandler())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = bootstrap.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
            ch.pipeline().addLast(new HttpResponseEncoder());
            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
            ch.pipeline().addLast(new HttpRequestDecoder());
            ch.pipeline().addLast(new DefaultProxyServerHandler());
        }
    }

}
