package com.kolamomo.koproxy.server;

import com.kolamomo.koproxy.util.ApiLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by jay on 16-1-22.
 */
public class DefaultServerHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ByteBuf reqBuf = (ByteBuf) message;
        byte[] request = new byte[reqBuf.readableBytes()];
        reqBuf.readBytes(request);

        String body = new String(request, "UTF-8");
        ApiLogger.info("server receive: " + body);
        ByteBuf respBuf = Unpooled.copiedBuffer(("hello: " + body).getBytes());
        context.write(respBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) throws Exception {
        context.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        ApiLogger.warn("DefaultServerHandler exception: " + cause.getMessage());
        context.close();
    }
}
