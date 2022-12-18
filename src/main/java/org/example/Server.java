package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//public class Server implements Callable {
public class Server implements Runnable {
    private Socket socket;
    public static ConcurrentHashMap<Request, Handler> requests = new ConcurrentHashMap<>();
    List validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    public Server(Socket clientSocket){
        this.socket = clientSocket;
    }
    public Server(){
        for(int i=0;i<validPaths.size();i++){
            Request request = new Request("GET", (String) validPaths.get(i),"HTTP/1.1");
            requests.put(request, new Handler() {
                //@Override
                public void handle(Request request, BufferedOutputStream responseStream) {
                    var filePath = Path.of(".", "public",request.path);
                    try (final var out = responseStream) {
                        var length = Files.size(filePath);
                        var mimeType = Files.probeContentType(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    @Override
    public void run(){


        while (true) {
            try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final var out = new BufferedOutputStream(socket.getOutputStream());) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
                    continue;
                }
                Request request = new Request(parts[0],parts[1],parts[2]);

                final var path = parts[1];
                if (!requests.keySet().contains(request)) {
                    notFound(out);
                    continue;
                }
                requests.get(request).handle(request,out);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void addHandler(String type, String path, Handler handler){
        Request request = new Request(type,path,"HTTP/1.1");
        requests.put(request, handler);
    }
    public void getFileClassic(Path filePath,BufferedOutputStream streamOut,String mimeType) {
        try (final var out = streamOut) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void getFile(Path filePath,BufferedOutputStream streamOut,String mimeType){
        try (final var out = streamOut) {
            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void notFound(BufferedOutputStream streamOut){
        System.out.println("Not Found");
        try (final var out = streamOut) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
