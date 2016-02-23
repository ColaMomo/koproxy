package com.kolamomo.koproxy.server;

import com.kolamomo.koproxy.util.ApiLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;


import java.util.Map;

/**
 * Created by jiangchao on 16/2/18.
 */
public class DefaultProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private HttpRequest request;
    private ByteBuf buffer_body = UnpooledByteBufAllocator.DEFAULT.buffer();
    /*
     * for debug
     */
    private StringBuffer sb_debug = new StringBuffer();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg)
            throws Exception {
        ApiLogger.info("messageReceived");

        try {
            ApiLogger.info("111");
            ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            ApiLogger.info("222");
                ApiLogger.info("333");

                this.request = (HttpRequest)msg;
                sb_debug.append("\n>> HTTP REQUEST -----------\n");
                sb_debug.append(this.request.protocolVersion().toString())
                        .append(" ").append(this.request.method().name())
                        .append(" ").append(this.request.uri());
                sb_debug.append("\n");
                HttpHeaders headers = this.request.headers();
                if (!headers.isEmpty()) {
                    for (Map.Entry<String, String> header : headers.entriesConverted()) {
                        sb_debug.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
                    }
                }
                sb_debug.append("\n");


                ByteBuf thisContent = msg.content();
                if (thisContent.isReadable()) {
                    buffer_body.writeBytes(thisContent);
                }
                if (msg instanceof LastHttpContent) {
                    sb_debug.append(buffer_body.toString(CharsetUtil.UTF_8));
                    LastHttpContent trailer = (LastHttpContent) msg;
                    if (!trailer.trailingHeaders().isEmpty()) {
                        for (CharSequence name : trailer.trailingHeaders().names()) {
                            sb_debug.append(name).append("=");
                            for (String value : trailer.trailingHeaders().getAllAndConvert(name)) {
                                sb_debug.append(value).append(",");
                            }
                            sb_debug.append("\n\n");
                        }
                    }
                    sb_debug.append("\n<< HTTP REQUEST -----------");
                }

            writeJSON(ctx, HttpResponseStatus.OK, Unpooled.copiedBuffer("{}", CharsetUtil.UTF_8));


        } catch (Exception e) {
            ApiLogger.error("exception, e: " + e.getMessage());
        } finally {

        }

    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelRegistered(ctx);
        ApiLogger.info("[channelRegistered]");
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
    }
    private void writeJSON(ChannelHandlerContext ctx, HttpResponseStatus status,
                           ByteBuf content/*, boolean isKeepAlive*/) {
        if (ctx.channel().isWritable()) {
            FullHttpResponse msg = null;
            if (content != null) {
                msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
                msg.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
            } else {
                msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            }
            if (msg.content() != null) {
                msg.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(msg.content().readableBytes()));
            }

            //not keep-alive
            ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
        }

    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx)
            throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
