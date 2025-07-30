package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController extends AbstractController{

    @Override
    public void doPost(HttpRequest request, HttpResponse response){
       String userId = request.getParameter("userId");
       String password = request.getParameter("password");

        User user = DataBase.findUserById(userId);

        boolean logined = false;
        String redirectPath = "localhost:8080/user/login_failed.html";

        if(user != null && user.getPassword().equals(password)) {
            logined = true;
            response.addHeader("Set-Cookie", "logined=" + Boolean.toString(logined));
            redirectPath = "localhost:8080/index.html";
        }

        response.sendRedirect(redirectPath);
    }



}
