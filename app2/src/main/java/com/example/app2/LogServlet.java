package com.example.app2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LogServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String timestamp = LocalDateTime.now().format(formatter);
        String clientIP = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        String logMessage = String.format(
            "[APP2] [%s] Request received from %s - URI: %s%s",
            timestamp,
            clientIP,
            requestURI,
            queryString != null ? "?" + queryString : ""
        );

        logger.info(logMessage);
        System.out.println(logMessage);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"application\": \"app2\",");
        out.println("  \"message\": \"Request logged successfully\",");
        out.println("  \"timestamp\": \"" + timestamp + "\",");
        out.println("  \"requestURI\": \"" + requestURI + "\",");
        out.println("  \"clientIP\": \"" + clientIP + "\"");
        out.println("}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
