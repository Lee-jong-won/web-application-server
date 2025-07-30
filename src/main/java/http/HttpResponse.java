package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private Map<String, String> headerFields = new HashMap<>();
    private DataOutputStream dos = null;

    public HttpResponse(OutputStream out){
        this.dos = new DataOutputStream(out);
    }

    public void forward(String requestPath){
        if(requestPath.endsWith(".css"))
            addHeader("Content-Type", "text/css;charset=utf-8");
        else if(requestPath.endsWith(".js"))
            addHeader("Content-Type", "application/javascript");
        else
            addHeader("Content-Type", "text/html;charset=utf-8");

        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            addHeader("Content-Length", Integer.toString(body.length));
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public void forwardBody(byte[] body, String type){
        if(type.equals("css"))
            addHeader("Content-Type", "text/css;charset=utf-8");
        else if(type.equals("js"))
            addHeader("Content-Type", "application/javascript");
        else
            addHeader("Content-Type", "text/html;charset=utf-8");

        try {
            addHeader("Content-Length", Integer.toString(body.length));
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String redirectPath){
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            addHeader("Location", "http://" + redirectPath);
            processHeaders();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders(){
        try {
            for (Map.Entry<String, String> headerEntry : headerFields.entrySet())
                dos.writeBytes(headerEntry.getKey() + ": " + headerEntry.getValue() + "\r\n");
            dos.writeBytes("\r\n");
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body){
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value){
        headerFields.put(key, value);
    }
}
