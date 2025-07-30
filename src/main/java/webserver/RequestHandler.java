package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import http.HttpMethod;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            StringBuilder headerSb = new StringBuilder();
            String line;
            while( (line = br.readLine()) != null && !"".equals(line))
                headerSb.append(line + "\n");

            log.info("httpMessage = {}", headerSb.toString());

            String[] header = headerSb.toString().split("\n");
            String requestLine = header[0];
            String requestURL = HttpRequestUtils.parseRequestPath(requestLine);
            HttpMethod httpMethod = HttpRequestUtils.parseHttpMethod(requestLine);

            Map<String, String> headerFields = new HashMap<>();
            for(int i = 1; i < header.length; i++){
                String[] headerField = header[i].split(": ");
                headerFields.put(headerField[0], headerField[1]);
            }

            if(requestURL.startsWith("/user/create")){
                int contentLength = Integer.parseInt(headerFields.get("Content-Length"));
                String params = IOUtils.readData(br, contentLength);
                Map<String, String> queryStringMap = HttpRequestUtils.parseQueryString(params);
                User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"),
                        queryStringMap.get("name"), queryStringMap.get("email"));
                DataBase.addUser(user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "localhost:8080/index.html");
                dos.flush();
            } else if(requestURL.equals("/user/login")){
                int contentLength = Integer.parseInt(headerFields.get("Content-Length"));
                String params = IOUtils.readData(br, contentLength);
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

                DataOutputStream dos = new DataOutputStream(out);
                response302LoginSuccessFailHeader(dos, redirectPath, logined);
            } else if(requestURL.equals("/user/list")){
                Map<String,String> cookieMap = HttpRequestUtils.parseCookies(headerFields.get("Cookie"));
                boolean logined = isLogin(headerFields);
                DataOutputStream dos = new DataOutputStream(out);

                if(logined){
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    for(User user : DataBase.findAll()){
                        sb.append("<tr>");
                        sb.append("<td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td>");
                        sb.append("</tr>");
                    }
                    sb.append("</table>");
                    byte[] body = sb.toString().getBytes();
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }

                if(!logined) {
                    response302Header(dos, "localhost:8080/user/login.html");
                    dos.flush();
                }
            }else if(requestURL.endsWith(".css")){
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + requestURL).toPath());
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            }else{
                responseResource(out, requestURL);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(Map<String, String> headerFields){
        Map<String,String> cookieMap = HttpRequestUtils.parseCookies(headerFields.get("Cookie"));
        return Boolean.parseBoolean(cookieMap.get("logined"));
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

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302LoginSuccessFailHeader(DataOutputStream dos, String redirectPath, boolean logined) {
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

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent){
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
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
