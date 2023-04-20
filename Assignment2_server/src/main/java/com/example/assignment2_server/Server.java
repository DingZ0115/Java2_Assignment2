package com.example.assignment2_server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * @author lannooo
 */
public class Server {
    private ArrayList<User> allUsers;
    private RoomList rooms;
    private int port;
    private ServerSocket ss;
    private long unusedUserID;
    public final long MAX_USERS = 999999;

    /**
     * 通过port号来构造服务器端对象
     * 维护一个总的用户列表和一个房间列表
     *
     * @param port
     * @throws Exception
     */
    public Server(int port) throws Exception {
        allUsers = new ArrayList<>();
        rooms = new RoomList();
        this.port = port;
        unusedUserID = 1;
        ss = new ServerSocket(port);
        System.out.println("Server is builded!");
    }

    /**
     * 获得下一个可用的用户id
     *
     * @return
     */
    private long getNextUserID() {
        if (unusedUserID < MAX_USERS)
            return unusedUserID++;
        else
            return -1;
    }

    /**
     * 开始监听，当接受到新的用户连接，就创建一个新的用户，并添加到用户列表中
     * 然后创建一个新的服务线程用于收发该用户的消息
     *
     * @throws Exception
     */
    public void startListen() throws Exception {
        while (true) {
            Socket socket = ss.accept();
            long id = getNextUserID();
            if (id != -1) {
                User user = new User("User" + id, id, socket);
                System.out.println(user.getName() + " is login...");
                allUsers.add(user);
                ServerT thread = new ServerT(user, allUsers, rooms);
                thread.start();
            } else {
                System.out.println("Server is full!");
                socket.close();
            }
        }
    }

    /**
     * 测试用main方法，设置侦听端口为9999，并开始监听
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Server server = new Server(9999);
            server.startListen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}