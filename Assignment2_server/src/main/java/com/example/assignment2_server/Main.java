package com.example.assignment2_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Main {
    static int port = 9999; // 服务器端口号
    static ConcurrentMap<String, Socket> clients = new ConcurrentHashMap<>(); //user和socket的map

    static HashMap<String, String> clientsInfo = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            try {
                Socket socket = serverSocket.accept(); //监听是否来了一个新用户
                //传一个当前用户和socket的map
                ServerThread thread = new ServerThread(socket, clients, clientsInfo); //给新用户创建一个线程
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setClients(ConcurrentMap<String, Socket> clients) {
        Main.clients = clients;
    }

    public static void setClientsInfo(HashMap<String, String> clientsInfo) {
        Main.clientsInfo = clientsInfo;
    }
}
