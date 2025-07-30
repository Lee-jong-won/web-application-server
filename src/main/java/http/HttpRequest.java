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
    private HttpMethod method;
    private String requestPath;
    private Map<String, String> headerFields = new HashMap<>();
    private Map<String, String> requestParams;


    public HttpRequest(InputStream in){

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            StringBuilder headerSb = new StringBuilder();
            String line;
            while( (line = br.readLine()) != null && !"".equals(line))
                headerSb.append(line + "\n");

            log.info("httpMessage = {}", headerSb.toString());

            String[] header = headerSb.toString().split("\n");
            String requestLine = header[0];

            processRequestLine(requestLine);

            for(int i = 1; i < header.length; i++){
                String[] headerField = header[i].split(": ");
                headerFields.put(headerField[0], headerField[1]);
            }

            if(method.isPost()){
                int contentLength = Integer.parseInt(getHeader("Content-Length"));
                String requestBody = IOUtils.readData(br, contentLength);

                if(requestParams == null)
                    this.requestParams = HttpRequestUtils.parseQueryString(requestBody);
                else{
                    this.requestParams.putAll(HttpRequestUtils.parseQueryString(requestBody));
                }
            }


        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void processRequestLine(String requestLine){

        String url = HttpRequestUtils.parseRequestPath(requestLine);
        this.method = HttpRequestUtils.parseHttpMethod(requestLine);
        int idx = url.indexOf("?");

        if(idx != -1){
            this.requestPath = url.substring(0, idx);
            this.requestParams = HttpRequestUtils.parseQueryString(url.substring(idx + 1));
        }else{
            this.requestPath = url;
        }

    }

    public String getHeader(String fieldName){
        return headerFields.get(fieldName);
    }

    public String getParameter(String parameterName){
        return requestParams.get(parameterName);
    }

    public HttpMethod getMethod(){
        return this.method;
    }

    public String getRequestPath(){
        return this.requestPath;
    }

}
