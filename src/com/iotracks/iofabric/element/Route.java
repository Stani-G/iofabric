package com.iotracks.iofabric.element;

import java.util.ArrayList;
import java.util.List;

public class Route {
	private List<String> receivers;
	
	public Route() {
		receivers = new ArrayList<>();
	}

	public List<String> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<String> receivers) {
		this.receivers = receivers;
	}

	@Override
	public String toString() {
		String in = "\"receivers\" : [";
		if (receivers != null)
			for (String e : receivers)
				in += "\"" + e + "\",";
		in += "]";
		return "{" + in + "}";
	}
}
