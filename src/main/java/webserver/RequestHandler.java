package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
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
            DataOutputStream dos = new DataOutputStream(out);

            StringBuilder headerSb = new StringBuilder();
            String line;
            while( (line = br.readLine()) != null && !"".equals(line))
                headerSb.append(line + "\n");

            log.info("httpMessage = {}", headerSb.toString());

            String[] header = headerSb.toString().split("\n");
            String requestLine = header[0];
            String requestURL = HttpRequestUtils.parseRequestPath(requestLine);
            String httpMethod = HttpRequestUtils.parseHttpMethod(requestLine);

            Map<String, String> headerFields = new HashMap<>();
            for(int i = 1; i < header.length; i++){
                String[] headerField = header[i].split(": ");
                headerFields.put(headerField[0], headerField[1]);
            }

            byte[] body = "Hello World".getBytes();

            if(requestURL.equals("/") && httpMethod.equals("GET")){
                response200Header(dos, body.length);
            }

            if(requestURL.equals("/index.html") && httpMethod.equals("GET")) {
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());
                response200Header(dos, body.length);
            }

            if(requestURL.equals("/user/form.html") && httpMethod.equals("GET")) {
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());
                response200Header(dos, body.length);
            }

            if(requestURL.equals("/user/login_failed.html") && httpMethod.equals("GET")) {
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());
                response200Header(dos, body.length);
            }
            
            if(requestURL.startsWith("/user/create") && httpMethod.equals("GET")) {
                int idx = requestURL.indexOf("?");
                String params = requestURL.substring(idx + 1);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);
                User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"),
                        queryStringMap.get("name"), queryStringMap.get("email"));
                DataBase.addUser(user);
                body = "register successfully finished".getBytes();
                response302Header(dos, "localhost:8080/index.html");
            }

            if(requestURL.startsWith("/user/create") && httpMethod.equals("POST")){
                int contentLength = Integer.parseInt(headerFields.get("Content-Length"));
                char[] buffer = new char[contentLength];
                br.read(buffer);
                String params = new String(buffer);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);
                User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"),
                        queryStringMap.get("name"), queryStringMap.get("email"));
                DataBase.addUser(user);
                body = "register successfully finished".getBytes();
                response302Header(dos, "localhost:8080/index.html");
            }

            if(requestURL.equals("/user/login.html") && httpMethod.equals("GET")) {
                body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());
                response200Header(dos, body.length);
            }

            if(requestURL.equals("/user/login") && httpMethod.equals("POST")) {
                int contentLength = Integer.parseInt(headerFields.get("Content-Length"));
                char[] buffer = new char[contentLength];
                br.read(buffer);
                String params = new String(buffer);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);

                String userId = queryStringMap.get("userId");
                String userPassword = queryStringMap.get("password");
                User user = DataBase.findUserById(userId);
                boolean logined = false;
                String redirectPath = "localhost:8080/user/login_failed.html";

                if(user != null && user.getPassword().equals(userPassword)) {
                    logined = true;
                    redirectPath = "localhost:8080/index.html";
                }
                response302HeaderWithCookie(dos, redirectPath, logined);
            }

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

    private void response302HeaderWithCookie(DataOutputStream dos, String redirectPath, boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://" + redirectPath + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + logined);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectPath){
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://" + redirectPath + "\r\n");
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
