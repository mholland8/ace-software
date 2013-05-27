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
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
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
public class NIOPubSocket implements Runnable
{
    private static final Logger logger = LogManager.getFormatterLogger(NIOPubSocket.class);
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
            NIOPubSocket s = new NIOPubSocket();
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

    public NIOPubSocket() throws IOException
    {
        // Create a new non-blocking server socket channel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

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
        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(port);
        serverChannel.socket().bind(isa);

        // Create a new selector
        selector = SelectorProvider.provider().openSelector();

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // now we are bound to a port we can begin listening for connections
        // from clients
        new Thread(this).start();
    }

    public void close() throws IOException
    {
        /*
         * for (ClientConnection c : clients) { c.close(); } clients.clear();
         * 
         * if (serverSocket != null) { // close the server socket but do not
         * delete // this allows us to re-open if desired serverSocket.close();
         * }
         */
    }

    public void send(byte[] bytes) throws IOException
    {
        /*
         * // send copy of message to each connected client // any errors or
         * exceptions will results in the // client being removed from our list
         * byte[] msg = encodeMessage(bytes); for (ClientConnection c : clients)
         * { try { // this just enqueues the message on the client queue
         * c.send(msg);
         * 
         * numMessages.incrementAndGet(); } catch (Exception ex) { // client is
         * no longer valid, drop the connection c.close(); clients.remove(c);
         * logger.info(String.format("%d clients connected", clients.size())); }
         * }
         */
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

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // Wait for an event one of the registered channels
                selector.select();

                // Iterate over the set of keys for which events are
                // available
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

                while (selectedKeys.hasNext())
                {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid())
                    {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable())
                    {
                        accept(key);
                    }
                    else if (key.isWritable())
                    {
                        write(key);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
        
    private void accept(SelectionKey key) throws IOException
    {
        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when the socket is available for writing
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        
        logger.info("Accepted new connection");
    }
    
    private void write(SelectionKey key) throws IOException
    {
    }
    
    private final AtomicInteger numMessages = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // private final CopyOnWriteArrayList<ClientConnection> clients = new
    // CopyOnWriteArrayList<ClientConnection>();

    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;

    // The selector we'll be monitoring
    private Selector selector;

    // The buffer into which we'll read data when it's available
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
}
