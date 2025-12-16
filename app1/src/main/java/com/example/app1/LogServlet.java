package com.example.app1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LogServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SERVICE_NAME = "tomcat-app1";
    private static final String DT_SERVICE_ID = "SERVICE-APP1";

    static {
        // Configure Dynatrace log formatter
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new DynatraceLogFormatter(SERVICE_NAME, DT_SERVICE_ID));
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String timestamp = LocalDateTime.now().format(formatter);
        String clientIP = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        String logMessage = String.format(
            "[APP1] Request received from %s - URI: %s%s",
            clientIP,
            requestURI,
            queryString != null ? "?" + queryString : ""
        );

        logger.info(logMessage);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"application\": \"app1\",");
        out.println("  \"service\": \"" + SERVICE_NAME + "\",");
        out.println("  \"dt.entity.service\": \"" + DT_SERVICE_ID + "\",");
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
