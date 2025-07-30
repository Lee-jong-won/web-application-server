package webserver;

import java.io.*;
import java.net.Socket;
import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            Controller controller = RequestMapping.getController(request.getRequestPath());

            if(controller != null)
                controller.service(request, response);
            else {
                String requestPath = getDefaultPath(request.getRequestPath());
                response.forward(requestPath); //requestPath를 처리할 수 있는 controller가 없는 경우, 정적 리소스 리턴
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getDefaultPath(String requestPath){
        if(requestPath.equals("/"))
            return "/index.html";
        else
            return requestPath;
    }


}
