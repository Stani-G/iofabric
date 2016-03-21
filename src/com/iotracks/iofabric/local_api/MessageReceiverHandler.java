package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class MessageReceiverHandler implements Callable<Object> {

	private final String MODULE_NAME = "Local API";
	
	private final FullHttpRequest req;
	private ByteBuf bytesData;
	
	public MessageReceiverHandler(FullHttpRequest req, ByteBuf	bytesData) {
		this.req = req;
		this.bytesData = bytesData;
	}
	
	public Object handleMessageRecievedRequest() throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In MessageReceiverHandler : handle");
		HttpHeaders headers = req.headers();

		if (req.getMethod() != POST) {
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
		}

		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json"))){
			String errorMsg = " Incorrect content/data format ";
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		ByteBuf msgBytes = req.content();
		String requestBody = msgBytes.toString(io.netty.util.CharsetUtil.US_ASCII);
		LoggingService.logInfo(MODULE_NAME,"body :"+ requestBody);
		JsonReader reader = Json.createReader(new StringReader(requestBody));
		JsonObject jsonObject = reader.readObject();

		if(getErrorMessageInReq(jsonObject) != null){
			String errorMsg = getErrorMessageInReq(jsonObject);
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}
		
		String receiverId = jsonObject.getString("id");

		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder builder = factory.createObjectBuilder();
		JsonArrayBuilder messagesArray = factory.createArrayBuilder();
		long lStartTime = System.currentTimeMillis();
		MessageBus bus = MessageBus.getInstance();
		List<Message> messageList = bus.getMessages(receiverId);
		long lEndTime = System.currentTimeMillis();
		long difference = lEndTime - lStartTime;
		System.out.println("Message Bus Retrival elapsed milliseconds: " + difference);
		if(messageList == null){
			String errorMsg = "No message found";
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}
		
		int msgCount = 0;
		for(Message msg : messageList){
			System.out.println("Message: " + msg);
			String msgJson = msg.toString();
			messagesArray.add(msgJson);
			msgCount++;
		}

		builder.add("status", "okay");
		builder.add("count", msgCount);
		builder.add("messages", messagesArray);

		String configData = builder.build().toString();
		LoggingService.logInfo(MODULE_NAME,"Config: "+ configData);
		bytesData.writeBytes(configData.getBytes());
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
		HttpHeaders.setContentLength(res, bytesData.readableBytes());
		return res;
	}

	private String getErrorMessageInReq(JsonObject jsonObject){
		String error = null;
		if(!jsonObject.containsKey("id")) return " Id not found ";
		if(jsonObject.getString("id").equals(null) || jsonObject.getString("id").trim().equals("")) return " Id value not found ";
		return error;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return handleMessageRecievedRequest();
	}
}
