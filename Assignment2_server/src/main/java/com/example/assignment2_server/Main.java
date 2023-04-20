package com.example.assignment2_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Main {
    private static int port = 9999; // 服务器端口号
    private static ConcurrentMap<String, Socket> clients = new ConcurrentHashMap<>(); //user和socket的map

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            try {
                Socket socket = serverSocket.accept();//监听是否来了一个新用户
                System.out.println("A user comes");
                //传一个当前用户和socket的map
                ServerThread thread = new ServerThread(socket, clients); //给新用户创建一个线程
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setClients(ConcurrentMap<String, Socket> clients) {
        Main.clients = clients;
    }
}
