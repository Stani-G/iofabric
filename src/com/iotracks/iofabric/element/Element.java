package com.iotracks.iofabric.element;

import java.util.List;

/**
 * Class represents IOElements
 * 
 * @author saeid
 *
 */
public class Element {
	private final String elementId;
	private final String imageName;
	private List<PortMapping> portMappings;
	private long lastModified;
	private long lastUpdated;
	private String containerId;
	private String registry;
	private String containerIpAddress;
	private boolean rebuild;
	
	public boolean isRebuild() {
		return rebuild;
	}

	public void setRebuild(boolean rebuild) {
		this.rebuild = rebuild;
	}

	public String getContainerIpAddress() {
		return containerIpAddress;
	}

	public void setContainerIpAddress(String containerIpAddress) {
		this.containerIpAddress = containerIpAddress;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public Element(String elementId, String imageName) {
		this.elementId = elementId;
		this.imageName = imageName;
		containerId = "";
	}

	public List<PortMapping> getPortMappings() {
		return portMappings;
	}

	public void setPortMappings(List<PortMapping> portMappings) {
		this.portMappings = portMappings;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getElementId() {
		return elementId;
	}

	public String getImageName() {
		return imageName;
	}
	
	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	@Override
	public boolean equals(Object e) {
		if (e == null)
			return false;
		Element element = (Element) e;
		return this.elementId.equals(element.getElementId());
	}
	
}
