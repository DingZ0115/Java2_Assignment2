package com.example.assignment2_server;

import java.util.ArrayList;

public class Room {
    private String name;
    private long roomId;
    private ArrayList<User> list;
    private int totalUsers;

    /**
     * 获得房间的名字
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置房间的新名字
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得房间的id号
     *
     * @return
     */
    public long getRoomId() {
        return roomId;
    }

    /**
     * 设置房间的id
     *
     * @param roomId
     */
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    /**
     * 向房间中加入一个新用户
     *
     * @param user
     */
    public void addUser(User user) {
        if (!list.contains(user)) {
            list.add(user);
            totalUsers++;
        } else {
            System.out.println("User is already in Room<" + name + ">:" + user);
        }
    }

    /**
     * 从房间中删除一个用户
     *
     * @param user
     * @return 目前该房间中的总用户数目
     */
    public int delUser(User user) {
        if (list.contains(user)) {
            list.remove(user);
            return --totalUsers;
        } else {
            System.out.println("User is not in Room<" + name + ">:" + user);
            return totalUsers;
        }
    }

    /**
     * 获得当前房间的用户列表
     *
     * @return
     */
    public ArrayList<User> getUsers() {
        return list;
    }

    /**
     * 获得当前房间的用户昵称的列表
     *
     * @return
     */
    public String[] getUserNames() {
        String[] userList = new String[list.size()];
        int i = 0;
        for (User each : list) {
            userList[i++] = each.getName();
        }
        return userList;
    }

    /**
     * 使用房间的名称和id来new一个房间
     *
     * @param name
     * @param roomId
     */
    public Room(String name, long roomId) {
        this.name = name;
        this.roomId = roomId;
        this.totalUsers = 0;
        list = new ArrayList<>();
    }
}