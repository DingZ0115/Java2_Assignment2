package com.example.assignment2_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private String name;
    private long id;
    private long roomId;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    /**
     * @param name:设置user的姓名
     * @param id：设置user的id
     * @param socket:保存用户连接的socket
     * @throws IOException
     */
    public User(String name, long id, final Socket socket) throws IOException {
        this.name = name;
        this.id = id;
        this.socket = socket;
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.pw = new PrintWriter(socket.getOutputStream());
    }

    /**
     * 获得该用户的id
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * 设置该用户的id
     *
     * @param id 新的id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * 获得用户当前所在的房间号
     *
     * @return roomId
     */
    public long getRoomId() {
        return roomId;
    }

    /**
     * 设置当前用户的所在的房间号
     *
     * @param roomId
     */
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    /**
     * 设置当前用户在聊天室中的昵称
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回当前用户在房间中的昵称
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 返回当前用户连接的socket实例
     *
     * @return
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * 设置当前用户连接的socket
     *
     * @param socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * 获得该用户的消息读取辅助类BufferedReader实例
     *
     * @return
     */
    public BufferedReader getBr() {
        return br;
    }

    /**
     * 设置 用户的消息读取辅助类
     *
     * @param br
     */
    public void setBr(BufferedReader br) {
        this.br = br;
    }

    /**
     * 获得消息写入类实例
     *
     * @return
     */
    public PrintWriter getPw() {
        return pw;
    }

    /**
     * 设置消息写入类实例
     *
     * @param pw
     */
    public void setPw(PrintWriter pw) {
        this.pw = pw;
    }

    /**
     * 重写了用户类打印的函数
     */
    @Override
    public String toString() {
        return "#User" + id + "#" + name + "[#Room" + roomId + "#]<socket:" + socket + ">";
    }
}