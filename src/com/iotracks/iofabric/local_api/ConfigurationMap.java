package com.iotracks.iofabric.local_api;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration map to store the current containers configurations
 * @author ashita
 * @since 2016
 */
public class ConfigurationMap {
	static Map<String, String> containerConfigMap;

	private static ConfigurationMap instance = null;

	private ConfigurationMap(){

	}
	
	/**
	 * Singleton configuration map object
	 * @param None
	 * @return ConfigurationMap
	 */
	public static ConfigurationMap getInstance(){
		if (instance == null) {
			synchronized (ConfigurationMap.class) {
				if(instance == null){
					instance = new ConfigurationMap();
					containerConfigMap = new HashMap<String, String>();
				}
			}
		}
		return instance;
	}
}