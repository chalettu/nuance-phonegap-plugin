package net.ninjaenterprises.nuance;

public interface ICredentials {
    
	/**
	 * Returns the application ID
	 */
	public String getAppId();
	/**
	 * Returns the application key
	 */
	public byte[] getAppKey();
	
}
