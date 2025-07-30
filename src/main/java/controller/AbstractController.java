package controller;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

public abstract class AbstractController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if(request.getMethod() == HttpMethod.GET)
            doGet(request, response);
        else
            doPost(request, response);
    }

    public void doPost(HttpRequest request, HttpResponse response) {}

    public void doGet(HttpRequest request, HttpResponse response){}
}
