package http;

public enum HttpMethod {

    GET,POST;

    public boolean isPost(){
        return this == POST;
    }

    public static HttpMethod fromString(String method) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }
        switch (method.toUpperCase()) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }
}
