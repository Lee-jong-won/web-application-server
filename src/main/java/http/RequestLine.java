package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {

    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
    private HttpMethod method;
    private String requestPath;
    private Map<String, String> requestParams = new HashMap<>();

    public RequestLine(String requestLine){
        String url = HttpRequestUtils.parseRequestPath(requestLine);
        this.method = HttpRequestUtils.parseHttpMethod(requestLine);

        int idx = url.indexOf("?");

        if(idx != -1){
            this.requestPath = url.substring(0, idx);
            Map<String, String> queryParameter = HttpRequestUtils.parseQueryString(url.substring(idx + 1));
            requestParams.putAll(queryParameter);
        }else{
            this.requestPath = url;
        }
    }

    public String getRequestPath(){return requestPath;}
    public HttpMethod getMethod(){return method;}
    public Map<String, String> getRequestParams(){return requestParams;}

}
