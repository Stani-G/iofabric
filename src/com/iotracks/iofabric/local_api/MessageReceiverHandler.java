package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.utils.logging.LoggingService;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

public class MessageReceiverHandler {
	
	private final String MODULE_NAME = "Local API";

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In MessageReceiverHandler : handle");

		if (req.getMethod() != POST) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}
		
		HttpHeaders headers = req.headers();
		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json"))){
			LoggingService.logInfo(MODULE_NAME,"header content type failure..." + headers.get("CONTENT_TYPE"));
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		ByteBuf msgBytes = req.content();
		String requestBody = msgBytes.toString(io.netty.util.CharsetUtil.US_ASCII);
		LoggingService.logInfo(MODULE_NAME,"body :"+ requestBody);
		JsonReader reader = Json.createReader(new StringReader(requestBody));
		JsonObject jsonObject = reader.readObject();
		
		if(!jsonObject.containsKey("id")){
			LoggingService.logInfo(MODULE_NAME,"id not found");
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}
		
		String receiverId = jsonObject.getString("id");
	
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder builder = factory.createObjectBuilder();
		JsonArrayBuilder messagesArray = factory.createArrayBuilder();
		MessageBus bus = MessageBus.getInstance();
		List<Message> messageList = bus.getMessages(receiverId);
		int msgCount = 0;
		for(Message msg : messageList){
			String msgJson = msg.toString();
			messagesArray.add(msgJson);
			msgCount++;
		}
				
		builder.add("status", "okay");
		builder.add("count", msgCount);
		builder.add("messages", messagesArray);
		
		String configData = builder.build().toString();
		LoggingService.logInfo(MODULE_NAME,"Config: "+ configData);
		ByteBuf	bytesData = ctx.alloc().buffer();
		bytesData.writeBytes(configData.getBytes());
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
		HttpHeaders.setContentLength(res, bytesData.readableBytes());
		
		sendHttpResponse( ctx, req, res); 
		return;

	}

	private static void sendHttpResponse(
			ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
