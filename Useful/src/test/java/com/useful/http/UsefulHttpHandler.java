package com.useful.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * http://evgenyg.wordpress.com/2010/05/01/uploading-files-multipart-post-apache
 * http://hc.apache.org/httpcomponents-core-ga/tutorial/html/
 * http://commons.apache.org/proper/commons-fileupload/using.html
 * 
 * @author Mark
 * 
 */
public class UsefulHttpHandler implements HttpHandler
{

    @Override
    public void handle(HttpExchange arg0) throws IOException
    {
        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File("C:\\temp"));

        // Create a new file upload handler
        PortletFileUpload upload = new PortletFileUpload(factory);

        // Set overall request size constraint
        // upload.setSizeMax(yourMaxRequestSize);

        // Parse the request
        // List<FileItem> items = upload.parseRequest();

        String response = "<!DOCTYPE html><html><body><form method=\"POST\" enctype=\"multipart/form-data\" action=\"fup.cgi\">  File to upload: <input type=\"file\" name=\"upfile\"><br/>" + "<br/>" + "<input type=\"submit\" value=\"Press\"> to upload the file!" + "</form></body></form>";
        arg0.sendResponseHeaders(200, response.length());
        OutputStream os = arg0.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), -1);
        server.createContext("/applications/myapp", new UsefulHttpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

    }

}
