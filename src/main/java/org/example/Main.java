package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {
    //ConcurrentHashMap<String, Request> requests = new ConcurrentHashMap<>();
    //static CopyOnWriteArrayList<Request> listRequest = new CopyOnWriteArrayList<>();
    //Set<Request> set = ConcurrentHashMap.newKeySet();
    public static void main(String[] args) {
        final var server = new Server();
        new Thread(()-> {
            try (final var serverSocket = new ServerSocket(9999)) {
                while (true) {
                    try {
                        final var socket = serverSocket.accept();
                        final Server serverConnect = new Server(socket);
                        final ExecutorService threadPool = Executors.newFixedThreadPool(64);
                        final Future<?> task = threadPool.submit(serverConnect);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        server.addHandler("GET", "/classic.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                var filePath = Path.of(".", "public",request.path);
                try (final var out = responseStream) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    var length = Files.size(filePath);
                    var mimeType = Files.probeContentType(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}