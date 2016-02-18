package com.kolamomo.koproxy.server;

import com.kolamomo.network.util.ApiLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * Created by jay on 16-1-22.
 */
public class NettyServer {
    private static final int DEFAULT_PORT = 8080;

    public NettyServer() {
        this(DEFAULT_PORT);
    }

    public NettyServer(int port) {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());
            //绑定端口
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            ApiLogger.info("netty server started in port: " + port);
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new DefaultServerHandler());
        }
    }

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
    }
}
