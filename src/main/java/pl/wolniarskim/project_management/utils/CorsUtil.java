package pl.wolniarskim.project_management.utils;

import javax.servlet.http.HttpServletResponse;

public class CorsUtil {

    public static void addCorsHeaders(HttpServletResponse response){
        if(!("*".equals(response.getHeader("Access-Control-Allow-Origin")))){
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "*");
            response.addHeader("Access-Control-Allow-Headers", "*");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addIntHeader("Access-Control-Max-Age", 10);
        }
    }
}
