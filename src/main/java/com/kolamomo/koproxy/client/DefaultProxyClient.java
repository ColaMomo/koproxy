package com.kolamomo.koproxy.client;

import com.kolamomo.koproxy.server.DefaultProxyServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

/**
 * Created by jiangchao on 16/2/20.
 */
public class DefaultProxyClient {
    private Bootstrap createBootStrap(final ChannelHandlerContext ctx, final String host, final String uri) {
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop());
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.config().setAllocator(new PooledByteBufAllocator());
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(new HttpResponseDecoder());
                ch.pipeline().addLast(new DefaultProxyServerHandler());
            }
        });
        return b;
    }
}
