package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuilder headerSb = new StringBuilder();
            String line;
            while( (line = br.readLine()) != null && !"".equals(line))
                headerSb.append(line + "\n");

            log.info("httpMessage = {}", headerSb.toString());

            String[] header = headerSb.toString().split("\n");

            String requestURL = HttpRequestUtils.parseRequestPath(header[0]);
            String httpMethod = HttpRequestUtils.parseHttpMethod(header[0]);
            byte[] body = "Hello World".getBytes();

            if(requestURL.equals("/index.html") && httpMethod.equals("GET"))
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());

            if(requestURL.equals("/user/form.html") && httpMethod.equals("GET"))
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());

            if(requestURL.startsWith("/user/create") && httpMethod.equals("GET")) {
                int idx = requestURL.indexOf("?");
                String params = requestURL.substring(idx + 1);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);
                User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"),
                        queryStringMap.get("name"), queryStringMap.get("email"));
                log.info("user = {}", user);
                body = "Register successfully finished!".getBytes();
            }

            if(requestURL.startsWith("/user/create") && httpMethod.equals("POST")){
                int contentLength = Integer.parseInt(header[3].split(" ")[1]);
                char[] buffer = new char[contentLength];
                br.read(buffer);
                String params = new String(buffer);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);
                User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"),
                        queryStringMap.get("name"), queryStringMap.get("email"));
                log.info("user = {}", user);
                body = "Register successfully finished!".getBytes();
            }


            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
