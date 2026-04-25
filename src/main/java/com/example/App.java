package com.example;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            requestCounter.incrementAndGet();

            String response = """
                <html>
                <head>
                    <title>CI/CD POC</title>
                    <style>
                        body {
                            font-family: Arial;
                            text-align: center;
                            background-color: #f4f4f4;
                            margin-top: 50px;
                        }
                        h1 { color: #2c3e50; }
                        p { font-size: 18px; }
                    </style>
                </head>
                <body>
                    <h1>🚀 CI/CD POC Successful!</h1>
                    <p>Java app deployed on Kubernetes</p>
                    <p>Using Jenkins + Docker + Kubernetes + Prometheus</p>
                </body>
                </html>
            """;

            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.createContext("/metrics", exchange -> {
            String response =
                    "# HELP cicd_app_up Application health status\n" +
                    "# TYPE cicd_app_up gauge\n" +
                    "cicd_app_up 1\n" +
                    "# HELP cicd_app_requests_total Total requests served\n" +
                    "# TYPE cicd_app_requests_total counter\n" +
                    "cicd_app_requests_total " + requestCounter.get() + "\n";

            exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.start();
        System.out.println("Server started on port 8080");
    }
}
