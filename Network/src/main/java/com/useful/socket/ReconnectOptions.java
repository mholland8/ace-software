package com.useful.socket;

public class ReconnectOptions
{
	public ReconnectOptions(int retries, int interval)
	{
		numRetries = retries;
		reconnectInterval = interval;
	}
	
	public int getNumRetries()
	{
		return numRetries;
	}
	
	public int getReconnectInterval()
	{
		return reconnectInterval;
	}
	
	private final int numRetries;        // number of times to attempt reconnect
	private final int reconnectInterval; // milliseconds
}
