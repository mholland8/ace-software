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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
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
public class UsefulPubSocket
{
    private static final Logger logger = LogManager.getFormatterLogger(UsefulPubSocket.class);
    /**
     * Defines maximum size of client queue
     */
    private static final int HIGH_WATER_MARK = 1024;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            UsefulPubSocket s = new UsefulPubSocket();
            s.bind(5555);

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
        serverSocket.setReuseAddress(true);

        scheduler.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                logger.info("Outbound message rate : %d per second", numMessages.getAndSet(0));
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void bind(int port) throws IOException
    {
        serverSocket.bind(new InetSocketAddress(port));

        // now we are bound to a port we can begin listening for connections
        // from clients
        new Thread(new ClientAcceptor()).start();
    }

    public void close() throws IOException
    {
        for (ClientConnection c : clients)
        {
            c.close();
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
        // client being removed from our list
        byte[] msg = encodeMessage(bytes);
        for (ClientConnection c : clients)
        {
            try
            {
                // this just enqueues the message on the client queue
                c.send(msg);

                numMessages.incrementAndGet();
            }
            catch (Exception ex)
            {
                // client is no longer valid, drop the connection
                c.close();
                clients.remove(c);
                logger.info(String.format("%d clients connected", clients.size()));
            }
        }
    }

    private byte[] encodeMessage(byte[] bytes) throws IOException
    {
        int size = bytes.length;
        byte[] tmpBuf = new byte[size + 4];

        tmpBuf[3] = (byte) (size & 0xff);
        size = size >> 8;
        tmpBuf[2] = (byte) (size & 0xff);
        size = size >> 8;
        tmpBuf[1] = (byte) (size & 0xff);
        size = size >> 8;
        tmpBuf[0] = (byte) (size & 0xff);
        size = size >> 8;

        System.arraycopy(bytes, 0, tmpBuf, 4, bytes.length);

        return tmpBuf;
    }

    private class ClientAcceptor implements Runnable
    {
        @Override
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

                    // Add this client to the list of connected clients
                    clients.add(new ClientConnection(clientSocket));
                    logger.info("New client connection, %d clients connected", clients.size());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * Encapsulates client socket, publishing thread and queue
     * 
     */
    private class ClientConnection implements Runnable
    {
        ClientConnection(Socket clientSocket)
        {
            socket = clientSocket;
            publishThread = new Thread(this);
            publishThread.start();
        }
        
        void close()
        {
            try
            {
                // shutdown the thread
                publishThread.interrupt();
                socket.close();
            }
            catch (Exception ex)
            {
                if (logger.isDebugEnabled())
                    logger.debug(ex);
            }
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    byte[] msg = outboundQueue.take();
                    
                    if (!socket.isClosed())
                        socket.getOutputStream().write(msg);
                }
                catch (IOException ex)
                {
                    if (logger.isDebugEnabled())                        
                        logger.debug(ex);
                    
                    try
                    {
                        outboundQueue.clear();
                        socket.close();
                    }
                    catch (IOException e) {}
                }
                catch (InterruptedException ex)
                {
                    if (logger.isDebugEnabled())                        
                        logger.debug("Client thread shutting down");
                    
                    break;
                }
            }
        }
        
        void send(byte[] bytes) throws InterruptedException, IOException
        {
            if (!socket.isClosed())
            {
                outboundQueue.put(bytes);
            }
            else
                throw new IOException("Socket closed");
        }

        private final ArrayBlockingQueue<byte[]> outboundQueue = new ArrayBlockingQueue<byte[]>( HIGH_WATER_MARK );
        private final Socket socket;
        private final Thread publishThread;
    }

    private final ServerSocket                      serverSocket = new ServerSocket();
    private final AtomicInteger                      numMessages = new AtomicInteger(0);
    private final ScheduledExecutorService             scheduler = Executors.newScheduledThreadPool(1);
    private final CopyOnWriteArrayList<ClientConnection> clients = new CopyOnWriteArrayList<ClientConnection>();
}
