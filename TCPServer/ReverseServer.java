package TCPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ReverseServer {
    public static void main(String[] args) throws IOException {
        int port = 44444; // 默认端口号
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new TCPServerReadThread(clientSocket).start();
        }
    }
}
