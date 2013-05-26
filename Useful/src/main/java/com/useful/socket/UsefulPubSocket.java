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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
/**
* @author Ace Software Ltd
*
*/
public class UsefulPubSocket
{
	private static final Logger logger = LogManager.getFormatterLogger(UsefulPubSocket.class);
	//private static final Logger logger = StatusLogger.getLogger();

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try 
		{
			UsefulPubSocket s = new UsefulPubSocket();
			s.bind( 5555 );
			
			while (true)
			{
				byte[] bytes = "Hello World".getBytes();
				s.send(bytes);
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public UsefulPubSocket() throws IOException
	{
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		
		clients = new CopyOnWriteArrayList<Socket>();
		
	    scheduler.scheduleAtFixedRate(new Runnable()
	    {
	    	public void run()
	    	{
	    		logger.debug("help");
	    		logger.info("Outbound message rate : %d per second", numMessages.getAndSet(0));
	    	}
	    }, 1000, 1000, TimeUnit.MILLISECONDS);		
	}
		
	public void bind(int port) throws IOException
	{
		serverSocket.bind(new InetSocketAddress(port));
		
		// now we are bound to a port we can begin listening for connections from clients
		new Thread(new ClientAcceptor()).start();
	}
	
	public void close() throws IOException
	{
		for (Socket s : clients)
		{
			s.close();
		}
		clients.clear();
		
		if (serverSocket != null)
		{
			// close the server socket but do not delete
			// this allows us to re-open if desired
			serverSocket.close();
		}
	}
	
	public void send(byte[] bytes) throws IOException
	{
		// send copy of message to each connected client
		// any errors or exceptions will results in the
		// client being remove from our list
		for(Socket s : clients)
		{

			try
			{
				// write the size of the packet followed by the actual data
				byte[] msg = encodeMessage(bytes);
				s.getOutputStream().write(msg);
				numMessages.incrementAndGet();
			}
			catch (SocketException ex)
			{
				// client is no longer valid, drop the connection
				s.close();
				clients.remove(s);
				System.out.println(String.format("%d clients connected", clients.size()));
			}
		}
	}
	
	private byte[] encodeMessage(byte[] bytes) throws IOException
	{
		int size = bytes.length;
		byte[] tmpBuf = new byte[size+4];
		
		tmpBuf[3] = (byte)(size & 0xff);
		size = size >> 8;
		tmpBuf[2] = (byte)(size & 0xff);
		size = size >> 8;
		tmpBuf[1] = (byte)(size & 0xff);
		size = size >> 8;
		tmpBuf[0] = (byte)(size & 0xff);
		size = size >> 8;
		
		System.arraycopy(bytes, 0, tmpBuf, 4, bytes.length);
		
		return tmpBuf;
	}

	private class ClientAcceptor implements Runnable
	{
		public void run()
		{
			try
			{
				// accept connections until the server socket is closed
				// at which point we'll get an exception and will terminate
				while (true)
				{
					Socket clientSocket = serverSocket.accept();
					clientSocket.setTcpNoDelay(true);
					logger.info("Client connected");
					
					// Add this client to the list of connected clients
					clients.add(clientSocket);
					logger.info("%d clients connected", clients.size());
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
	}
	
	private ServerSocket					serverSocket	= null;
	private CopyOnWriteArrayList<Socket>	clients = null;
	
	private final AtomicInteger				numMessages = new AtomicInteger(0);
    private final ScheduledExecutorService	scheduler = Executors.newScheduledThreadPool(1);
}
