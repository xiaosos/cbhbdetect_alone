package com.cbhb.cbhbdetect;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class VipController {

    @RequestMapping("/vip")
    public String vipDownload(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        System.out.println("sesson:"+session.hashCode());
        System.out.println("request:"+request.hashCode());

        Cookie cookie = new Cookie("test","woc");
        response.addCookie(cookie);

        if("world".equals(session.getAttribute("hello"))){
            return "YES";
        }else{
            session.setAttribute("hello","world");
            return "FALSE";
        }


    }
}
