/**
 
 * Copyright 2013 Ace Software Ltd
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.useful.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 /**
 * @author Ace Software Ltd
 *
 */
public class UsefulSubSocket
{
	private static final Logger logger = LogManager.getFormatterLogger(UsefulSubSocket.class);

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final AtomicInteger numMessages = new AtomicInteger(0);
		final AtomicInteger corruptedMessages = new AtomicInteger(0);
		
	    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	    scheduler.scheduleAtFixedRate(new Runnable()
	    {
	    	public void run()
	    	{
	    		logger.info("Message received in last interval = %d, corrupted %d", numMessages.getAndSet(0), corruptedMessages.get());
	    	}
	    }, 1000, 1000, TimeUnit.MILLISECONDS);
	    
		UsefulSubSocket s;
		try
		{
			s = new UsefulSubSocket();
			s.connect("localhost", 5555);
			
			while (true)
			{
				String msg = new String(s.recv());
				if (!msg.equals("Hello World"))
					corruptedMessages.getAndIncrement();
				numMessages.getAndIncrement();
			}

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a new Subscription socket.
	 * Default behaviour of disconnect is to throw an exception from the recv method
	 * Application must handle any reconnect logic and cope with any messages missed
	 */
	public UsefulSubSocket()
	{
		s = new Socket();
	}
	
	/**
	 * Create a new Subscription socket with reconnect logic.
	 * Use only if you are not concerned with missing messages while the reconnect is taking place
	 * 
	 * @param opts specifies number of times to retry and interval between each reconnect attempt
	 */
	public UsefulSubSocket(ReconnectOptions opts)
	{
		reconnectOpts = opts;
		
		s = new Socket();
	}
	
	public void connect(String hostname, int port) throws IOException
	{
		// save the hostname and port in case we need to reconnect later
		this.hostname = hostname;
		this.port = port;
		
		reconnect();
	}
	
	private void reconnect()
	{
		int attempts = 0;
		int maxAttempts = 1;
		
		if (reconnectOpts != null)
		{
			maxAttempts = reconnectOpts.getNumRetries();
		}
		try
		{
			s.connect(new InetSocketAddress(hostname, port));
			s.setTcpNoDelay(true);
		}
		catch (IOException ex)
		{
			logger.info("Failed to connect to %s:%d", hostname, port);
			if (reconnectOpts != null)
			{
				// 
			}
		}
	}
	
	public byte[] recv() throws IOException
	{
		byte[] bytes = null;
		try
		{			
			int size = readSize(s.getInputStream());
			bytes = readBytes(size, s.getInputStream());
		}
		catch (IOException ex)
		{
			// server has disappeared. Attempt to reconnect
			// ToDo
			reconnect();
		}
		
		return bytes;
	}
	
	private int readSize(InputStream s) throws IOException
	{
		int size = 0;
		byte[] sizeHdr = readBytes(4,s);
		
		for (int i = 0; i < 4 ; ++i)
			size = (size << 8) | sizeHdr[i];
		
		return size;
	}
	
	private byte[] readBytes(int n, InputStream s) throws IOException
	{
		byte[] bytes = new byte[n];
		int bytes_read = 0;
		int bytes_remaining = n;
		
		while (bytes_read < n)
		{
			bytes_read += s.read(bytes, bytes_read, bytes_remaining);
			bytes_remaining = n - bytes_read;
		}
		
		return bytes;
	}
	
	private Socket	s;
	private String  hostname;
	private int     port;
	
	private ReconnectOptions reconnectOpts = null;
}
