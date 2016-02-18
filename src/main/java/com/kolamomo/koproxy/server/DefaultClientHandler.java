package com.kolamomo.koproxy.server;

import com.kolamomo.network.util.ApiLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by jay on 16-1-29.
 */
public class DefaultClientHandler extends ChannelHandlerAdapter {
    private ByteBuf reqBuf = null;
    private static final  String DEFAULT_REQUEST = "lalala";

    public DefaultClientHandler() {
        this(DEFAULT_REQUEST);
    }

    public DefaultClientHandler(String request) {
        reqBuf = Unpooled.buffer(request.getBytes().length);
        reqBuf.writeBytes(request.getBytes());
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        context.writeAndFlush(reqBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ByteBuf respBuf = (ByteBuf)message;
        byte[] resp = new byte[respBuf.readableBytes()];
        respBuf.readBytes(resp);
        String body = new String(resp, "UTF-8");
        ApiLogger.info("server response: " + body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        ApiLogger.warn("DefaultServerHandler exception: " + cause.getMessage());
        context.close();
    }
}
