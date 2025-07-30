package http;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RequestLineTest {

    @Test
    public void create_method_get() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/index.html", line.getRequestPath());
    }

    @Test
    public void create_method_post() {
        RequestLine line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("/index.html", line.getRequestPath());
    }

    @Test
    public void create_path_and_params() {
        RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/user/create", line.getRequestPath());
        Map<String, String> params = line.getRequestParams();
        assertEquals(2, params.size());
    }

}