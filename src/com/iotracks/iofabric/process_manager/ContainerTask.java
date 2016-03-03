package com.iotracks.iofabric.process_manager;

public class ContainerTask {
	public enum Tasks {
		ADD,
		UPDATE,
		REMOVE;
	}
	
	public Tasks action;
	public Object data;
	
	public ContainerTask(Tasks action, Object data) {
		this.action = action;
		this.data = data;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(ContainerTask.class))
			return false;
		ContainerTask ac = (ContainerTask) o;
		return ac.action.equals(this.action) && ac.data.equals(data);
	}
	
	@Override
	public int hashCode() {
		return action.hashCode() + data.hashCode();
	}
}

