package com.iotracks.iofabric.local_api;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Pipeline factory to initialize the channel and assign handler for the request.
 * Thread pool for the performance
 * @author ashita
 * @since 2016
 */
public class LocalApiServerPipelineFactory extends ChannelInitializer<SocketChannel>{
	private final SslContext sslCtx;
	private final EventExecutorGroup executor;
	
	public LocalApiServerPipelineFactory(SslContext sslCtx) {
		this.sslCtx = sslCtx;
		this.executor = new DefaultEventExecutorGroup(10);
	}
	
	/**
	 * Initialize channel for communication and assign handler
	 * @param SocketChannel
	 * @return void
	 */
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
//		pipeline.addLast(new HttpRequestDecoder(4 * Constants.KiB, 64 * Constants.KiB, 2 * Constants.MiB));
		pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
		pipeline.addLast(new LocalApiServerHandler(executor));	
	}
}	