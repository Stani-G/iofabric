package com.iotracks.iofabric.local_api;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class LocalApiServer {
	private final String MODULE_NAME = "Local API";

	EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	EventLoopGroup workerGroup = new NioEventLoopGroup();

	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = 54321;

	public void start() throws Exception {
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new LocalApiServerPipelineFactory(sslCtx));

		Channel ch = b.bind(PORT).sync().channel();	
		LoggingService.logInfo(MODULE_NAME, "Local api server started at port: " + PORT + "\n");

		ch.closeFuture().sync();
	}

	protected void stop() throws Exception {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		LoggingService.logInfo(MODULE_NAME, "Local api server stopped\n");
	}
}