package com.iotracks.iofabric.supervisor;

import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;

public class SupervisorStatus {
	private ModulesStatus daemonStatus;			// FC
	private ModulesStatus[] modulesStatus;
	private long daemonLastStart;				// FC
	private long operationDuration;				// FC
	
	
	public SupervisorStatus() {
		modulesStatus = new ModulesStatus[Constants.NUMBER_OF_MODULES];
		for (int i = 0; i < Constants.NUMBER_OF_MODULES; i++)
			modulesStatus[i] = ModulesStatus.STARTING;
	}

	public SupervisorStatus setModuleStatus(int module, ModulesStatus status) {
		modulesStatus[module] = status;
		return this;
	}
	
	public ModulesStatus getModuleStatus(int module) {
		return modulesStatus[module];
	}
	
	public ModulesStatus getDaemonStatus() {
		return daemonStatus;
	}
	
	public SupervisorStatus setDaemonStatus(ModulesStatus daemonStatus) {
		this.daemonStatus = daemonStatus;
		return this;
	}
	
	public long getDaemonLastStart() {
		return daemonLastStart;
	}
	
	public SupervisorStatus setDaemonLastStart(long daemonLastStart) {
		this.daemonLastStart = daemonLastStart;
		return this;
	}
	
	public long getOperationDuration() {
		return operationDuration - daemonLastStart;
	}
	
	public SupervisorStatus setOperationDuration(long operationDuration) {
		this.operationDuration = operationDuration;
		return this;
	}
}
