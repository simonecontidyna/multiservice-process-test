package com.example.app2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class LogServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(LogServlet.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SERVICE_NAME = "tomcat-app2";
    private static final String DT_SERVICE_ID = "SERVICE-APP2";

    @Override
    public void init() throws ServletException {
        super.init();
        // Set Dynatrace service ID in ThreadContext for all requests in this servlet
        logger.info("Initializing LogServlet for service: " + SERVICE_NAME);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set Dynatrace entity service in MDC (ThreadContext)
        ThreadContext.put("dt.entity.service", DT_SERVICE_ID);

        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String clientIP = request.getRemoteAddr();
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();

            String logMessage = String.format(
                "[APP2] Request received from %s - URI: %s%s",
                clientIP,
                requestURI,
                queryString != null ? "?" + queryString : ""
            );

            logger.info(logMessage);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.println("{");
            out.println("  \"application\": \"app2\",");
            out.println("  \"service\": \"" + SERVICE_NAME + "\",");
            out.println("  \"dt.entity.service\": \"" + DT_SERVICE_ID + "\",");
            out.println("  \"message\": \"Request logged successfully\",");
            out.println("  \"timestamp\": \"" + timestamp + "\",");
            out.println("  \"requestURI\": \"" + requestURI + "\",");
            out.println("  \"clientIP\": \"" + clientIP + "\"");
            out.println("}");
        } finally {
            // Clear ThreadContext to avoid memory leaks
            ThreadContext.clearAll();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
