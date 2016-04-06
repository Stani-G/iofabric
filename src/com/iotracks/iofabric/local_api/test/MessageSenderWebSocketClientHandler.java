package com.iotracks.iofabric.local_api.test;

import org.bouncycastle.util.Arrays;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.utils.BytesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class MessageSenderWebSocketClientHandler extends SimpleChannelInboundHandler<Object>{
	
	private static int testCounter = 0;
	
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_RECEIPT = 0xE;
	private String publisherId = "";

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;

	public MessageSenderWebSocketClientHandler(WebSocketClientHandshaker handshaker, String id) {
		this.handshaker = handshaker;
		publisherId = id;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("WebSocket Client disconnected!");
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("client channelRead0 "+ctx);
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			System.out.println("WebSocket Client connected!");
			handshakeFuture.setSuccess();
		}

		if(msg instanceof WebSocketFrame){
			WebSocketFrame frame = (WebSocketFrame)msg;
			if(frame instanceof BinaryWebSocketFrame){
				handleWebSocketFrame(ctx,  frame);
			}
			return;
		}
		sendRealTimeMessageTest(ctx);
		return;
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		System.out.println("In client handleWebSocketFrame.....");
		
		if (frame instanceof BinaryWebSocketFrame) {
			System.out.println("In websocket client.....  Text WebSocket Frame...Receiving Receipt" );
			ByteBuf input = frame.content();
			if (!input.isReadable()) {
				return;
			}

			byte[] byteArray = new byte[input.readableBytes()];
			int readerIndex = input.readerIndex();
			input.getBytes(readerIndex, byteArray);


			Byte opcode = byteArray[0];
			System.out.println("Opcode: " + opcode);
			if(opcode.intValue() == OPCODE_RECEIPT){
				int size = byteArray[1];
				int pos = 3;
				if (size > 0) {
					String messageId = BytesUtil.bytesToString(Arrays.copyOfRange(byteArray, pos, pos + size));
					System.out.println("Message Id: " + messageId + "\n");
					pos += size;
				}

				size = byteArray[2];
				if (size > 0) {
					long timeStamp = BytesUtil.bytesToLong(Arrays.copyOfRange(byteArray, pos, pos + size));
					System.out.println("Timestamp: " + timeStamp + "\n");
					pos += size;
				}

			}
		}
		return;

	}

	public void sendRealTimeMessageTest(ChannelHandlerContext ctx){
		System.out.println("In clienttest : sendRealTimeMessageTest");
		System.out.println("Test Counter: " + testCounter);
		
		ByteBuf buffer1 = Unpooled.buffer(1024);
		buffer1.writeByte(OPCODE_MSG);

		//Actual Message
		//short version = 4;//version
		String id = ""; //id
		String tag = "Bosch Camera 8798797"; //tag
		String messageGroupId = "group1"; //messageGroupId
		Integer seqNum = testCounter; //sequence number
		Integer seqTot = 100; //sequence total
		Byte priority = 5; //priority 
		Long timestamp = (long)0; //timestamp
		String publisher = publisherId; //publisher
		String authid = "auth"; //authid
		String authGroup = "authgrp"; //auth group
		Long chainPos = (long)10; //chain position
		String hash = "hashingggg";  //hash
		String prevHash = "prevhashingggg"; //previous hash
		String nounce = "nounceee";  //nounce
		Integer diffTarget = 30;//difficultytarget
		String infotype = "image/jpeg"; //infotype
		String infoformat = "base64"; //infoformat
		String contextData = "gghh";
		String contentData = "sdkjhwrtiy8wrtgSDFOiuhsrgowh4touwsdhsDFDSKJhsdkljasjklweklfjwhefiauhw98p328testcounter" + testCounter;

		Message m = new Message();
		m.setId(id);
		m.setTag(tag);
		m.setMessageGroupId(messageGroupId);
		m.setSequenceNumber(seqNum);
		m.setSequenceTotal(seqTot);
		m.setPriority(priority);
		m.setTimestamp(timestamp);
		m.setPublisher(publisher);
		m.setAuthIdentifier(authid);
		m.setAuthGroup(authGroup);
		m.setChainPosition(chainPos);
		m.setHash(hash);
		m.setPreviousHash(prevHash);
		m.setNonce(nounce);
		m.setDifficultyTarget(diffTarget);
		m.setInfoType(infotype);
		m.setInfoFormat(infoformat);
		m.setContextData(contextData.getBytes());
		m.setContentData(contentData.getBytes());

		//Send Total Length of IOMessage - 4 bytes 
		int totalMsgLength = 0;

		byte[] bmsg = null;
		try {
			bmsg = m.getBytes();
		} catch (Exception e) {}
		totalMsgLength = bmsg.length;
		System.out.println("Total message length: "+ totalMsgLength);
		buffer1.writeBytes(BytesUtil.integerToBytes(totalMsgLength));
		buffer1.writeBytes(bmsg);

		ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
		System.out.println("Send RealTime Message : done");
		testCounter++;
		return;
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (!handshakeFuture.isDone()) {
			handshakeFuture.setFailure(cause);
		}
		ctx.close();
	}
}