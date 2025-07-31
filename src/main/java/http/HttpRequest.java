package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private Map<String, String> headerFields = new HashMap<>();
    private RequestLine requestLine;

    public HttpRequest(InputStream in){

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            StringBuilder headerSb = new StringBuilder();
            String line;
            while( (line = br.readLine()) != null && !"".equals(line))
                headerSb.append(line + "\n");

            log.info("httpMessage = {}", headerSb.toString());

            String[] header = headerSb.toString().split("\n");
            requestLine = new RequestLine(header[0]);

            for(int i = 1; i < header.length; i++){
                String[] headerField = header[i].split(": ");
                headerFields.put(headerField[0], headerField[1]);
            }

            if(getMethod().isPost()){
                int contentLength = Integer.parseInt(getHeader("Content-Length"));
                String requestBody = IOUtils.readData(br, contentLength);
                requestLine.getRequestParams().putAll(HttpRequestUtils.parseQueryString(requestBody));
            }

        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public String getOneCookieValue(String cookieName){
        String cookies = getHeader("Cookie");
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);
        return cookieMap.get(cookieName);
    }


    public String getHeader(String fieldName){
        return headerFields.get(fieldName);
    }

    public String getParameter(String parameterName){
        return requestLine.getRequestParams().get(parameterName);
    }

    public HttpMethod getMethod(){
        return requestLine.getMethod();
    }

    public String getRequestPath(){
        return requestLine.getRequestPath();
    }



}
