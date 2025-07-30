package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class CreateUserController extends AbstractController{
    @Override
    public void doPost(HttpRequest request, HttpResponse response){
        User user = new User(request.getHeader("userId"), request.getHeader("password"),
                request.getHeader("name"), request.getHeader("email"));
        DataBase.addUser(user);
        response.sendRedirect("localhost:8080/index.html");
    }


}
