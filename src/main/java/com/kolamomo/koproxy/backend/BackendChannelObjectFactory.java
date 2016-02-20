package com.kolamomo.koproxy.backend;

import org.apache.commons.pool.BasePoolableObjectFactory;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jiangchao on 16/2/20.
 */
class BackendChannelObjectFactory extends BasePoolableObjectFactory {

    private final String host;
    private final int port;
    private final int idleTimeout;
    private final int connectionTimeout;
    private final int maxResponseLength;
    private final ClientSocketChannelFactory clientSocketChannelFactory;
    private final HostBackendHandlerListener handlerListener;

    private final Timer timer;

    public BackendChannelObjectFactory(String host, int port,int idleTimeout,int connectionTimeout,int maxResponseLength,
                                       ClientSocketChannelFactory clientSocketChannelFactory,Timer timer,HostBackendHandlerListener handlerListener) {
        this.host = host;
        this.port = port;
        this.clientSocketChannelFactory = clientSocketChannelFactory;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxResponseLength = maxResponseLength;
        this.timer = timer;
        this.handlerListener = handlerListener;
    }


    @Override
    public void destroyObject(Object obj) throws Exception {
        BackendConnection connection = (BackendConnection)obj;
        if(LogUtils.isTraceEnabled()){
            LogUtils.trace("BackendChannelPool destroyObject "+obj);
        }
        if(connection.isOpen()||connection.isConnected()){
            connection.close();
        }
    }

    @Override
    public boolean validateObject(Object obj) {
        BackendConnection connection = (BackendConnection)obj;
        if(LogUtils.isTraceEnabled()){
            LogUtils.trace("BackendChannelPool validateObject "+obj);
        }
        return connection.isOpen()&&connection.isConnected();
    }

    @Override
    public void activateObject(Object obj) throws Exception {
        BackendConnection connection = (BackendConnection)obj;
        connection.setActive(true);
        if(LogUtils.isTraceEnabled()){
            LogUtils.trace("BackendChannelPool activateObject "+obj);
        }
    }

    @Override
    public void passivateObject(Object obj) throws Exception {
        BackendConnection connection = (BackendConnection)obj;
        connection.setActive(false);
        if(LogUtils.isTraceEnabled()){
            LogUtils.trace("BackendChannelPool passivateObject "+obj);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
     */
    @Override
    public Object makeObject() throws Exception {

        if(LogUtils.isTraceEnabled()){
            LogUtils.trace("BackendChannelPool makeObject");
        }

        // await*() in I/O thread causes a dead lock or sudden performance drop.
        //  pool.borrowObject() 必须在新的线程中执行

        // Configure the client.
        final ClientBootstrap cb = new ClientBootstrap(
                clientSocketChannelFactory);
        final BlockingQueue<BackendRequest> requestQueue = new LinkedBlockingQueue<BackendRequest>();

        final ChannelPipelineFactory cpf = new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                // Create a default pipeline implementation.
                final ChannelPipeline pipeline = Channels.pipeline();

                pipeline.addLast("timeout", new ProxyIdleStateHandler(timer, 0, 0, idleTimeout));
                pipeline.addLast("decoder", new ProxyHttpResponseDecoder());
                pipeline.addLast("aggregator", new ProxyHttpChunkAggregator(maxResponseLength));

                final BackendRelayingHandler handler = new BackendRelayingHandler(
                        handlerListener,requestQueue);

                final BackendRequestEncoder encoder = new BackendRequestEncoder(requestQueue);
                pipeline.addLast("encoder", encoder);
                pipeline.addLast("handler", handler);
                return pipeline;
            }
        };

        // Set up the event pipeline factory.
        cb.setPipelineFactory(cpf);
        //TODO more option config.
        cb.setOption("connectTimeoutMillis", connectionTimeout * 1000);

        ChannelFuture future = cb.connect(new InetSocketAddress(host,
                port));
        if(LogUtils.isDebugEnabled()){
            LogUtils.debug("ClientChannelObjectFactory.makeObject ChannelFuture: "+host+":"+port);
        }
        future = future.await();
        if(future.isCancelled()){
            throw new ConnectTimeoutException("request cancelled.");
        } else if(!future.isSuccess()){
            throw new ConnectTimeoutException(future.getCause());
        } else {
            return new BackendConnection(future.getChannel());
        }
    }

}
