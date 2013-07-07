package com.useful.http;

import java.io.InputStream;
import java.util.Set;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * http://evgenyg.wordpress.com/2010/05/01/uploading-files-multipart-post-apache
 * http://hc.apache.org/httpcomponents-core-ga/tutorial/html/
 * http://commons.apache.org/proper/commons-fileupload/using.html
 * 
 * @author Mark
 * 
 */
public class UsefulHttpHandler implements Processor
{
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder()
        {
            public void configure()
            {
                from("jetty:http://localhost:8000/myapp/myservice").process(new UsefulHttpHandler());
            }
        });

        context.start();

    }

    @Override
    public void process(Exchange exchange) throws Exception
    {

        // we have access to the HttpServletRequest here and we can grab it if
        // we need it
        HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);

        // Check that we have a file upload request
        if (!ServletFileUpload.isMultipartContent(req))
        {
            Set<String> attachments = exchange.getIn().getAttachmentNames();
            for (String name : attachments)
            {
                System.out.println(name);
                DataHandler hndl = exchange.getIn().getAttachment(name);
                System.out.println(hndl.getContentType());

                saveToFile(name, hndl.getInputStream());
            }
        }
        else
        {
            System.out.println(req.toString());
            String response = "<!DOCTYPE html><html><body><form method=\"POST\" enctype=\"multipart/form-data\" action=\"myservice\">  File to upload: <input type=\"file\" name=\"upfile\"><br/>" + "<br/>" + "<input type=\"submit\" value=\"Press\"> to upload the file!" + "</form></body></form>";
            exchange.getOut().setBody(response);
        }
    }

    private void saveToFile(String name, InputStream stream) throws Exception
    {
    }
}
