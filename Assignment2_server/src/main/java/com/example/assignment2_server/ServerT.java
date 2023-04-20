package com.example.assignment2_server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerT extends Thread {
    private User user;
    private ArrayList<User> userList;/*保存用户列表*/
    private RoomList map;   /*保存房间列表*/
    private long roomId;
    private PrintWriter pw;

    /**
     * 通过用户的对象实例、全局的用户列表、房间列表进行构造
     *
     * @param user
     * @param userList
     * @param map
     */
    public ServerT(User user, ArrayList<User> userList, RoomList map) {
        this.user = user;
        this.userList = userList;
        this.map = map;
        pw = null;
        roomId = -1;
    }

    /**
     * 线程运行部分，持续读取用户socket发送来的数据，并解析
     */
    public void run() {
        try {
            while (true) {
                String msg = user.getBr().readLine();
                System.out.println(msg); /*解析用户的数据格式*/
                parseMsg(msg);
            }
        } catch (SocketException se) { /*处理用户断开的异常*/
            System.out.println("user " + user.getName() + " logout.");

        } catch (Exception e) { /*处理其他异常*/
            e.printStackTrace();
        } finally {
            try {
                /*
                 * 用户断开或者退出，需要把该用户移除
                 * 并关闭socket
                 */
                remove(user);
                user.getBr().close();
                user.getSocket().close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * 用正则表达式匹配数据的格式，根据不同的指令类型，来调用相应的方法处理
     *
     * @param msg
     */
    private void parseMsg(String msg) {
        String code = null;
        String message = null;
        if (msg.length() > 0) {
            /*匹配指令类型部分的字符串*/
            Pattern pattern = Pattern.compile("<code>(.*)</code>");
            Matcher matcher = pattern.matcher(msg);
            if (matcher.find()) {
                code = matcher.group(1);
            }
            /*匹配消息部分的字符串*/
            pattern = Pattern.compile("<msg>(.*)</msg>");
            matcher = pattern.matcher(msg);
            if (matcher.find()) {
                message = matcher.group(1);
            }

            switch (code) {
                case "join":
                    // add to the room
                    // code = 1, 直接显示在textArea中
                    // code = 11, 在list中加入
                    // code = 21, 把当前房间里的所有用户返回给client
                    if (roomId == -1) {
                        roomId = Long.parseLong(message);
                        map.join(user, roomId);
                        sendRoomMsgExceptSelf(buildCodeWithMsg("<name>" + user.getName() + "</name><id>" + user.getId() + "</id>", 11));
                        // 这个消息需要加入房间里已有用户的列表
                        returnMsg(buildCodeWithMsg("你加入了房间:" + map.getRoom(roomId).getName(), 1));
                        returnMsg(buildCodeWithMsg(getMembersInRoom(), 21));
                    } else {
                        map.esc(user, roomId);
                        sendRoomMsg(buildCodeWithMsg("" + user.getId(), 12));
                        long oldRoomId = roomId;
                        roomId = Long.parseLong(message);
                        map.join(user, roomId);
                        sendRoomMsgExceptSelf(buildCodeWithMsg("<name>" + user.getName() + "</name><id>" + user.getId() + "</id>", 11));
                        returnMsg(buildCodeWithMsg("你退出房间:" + map.getRoom(oldRoomId).getName() + ",并加入了房间:" + roomId, 1));
                        returnMsg(buildCodeWithMsg(getMembersInRoom(), 21));
                    }
                    break;
                case "esc":
                    // delete from room list
                    // code = 2， 弹窗提示
                    // code = 12, 对所有该房间的其他用户发送该用户退出房间的信息，从list中删除
                    if (roomId != -1) {
                        int flag = map.esc(user, roomId);
                        sendRoomMsgExceptSelf(buildCodeWithMsg("" + user.getId(), 12));
                        long oldRoomId = roomId;
                        roomId = -1;
                        returnMsg(buildCodeWithMsg("你已经成功退出房间，不会收到消息", 2));
                        if (flag == 0) {
                            sendMsg(buildCodeWithMsg("" + oldRoomId, 13));
                        }
                    } else {
                        returnMsg(buildCodeWithMsg("你尚未加入任何房间", 2));
                    }
                    break;
                case "list":
                    // list all the rooms
                    // code = 3, 在客户端解析rooms，并填充roomlist
                    returnMsg(buildCodeWithMsg(getRoomsList(), 3));
                    break;
                case "message":
                    // send message
                    // code = 4, 自己收到的话，打印的是‘你说：....'否则打印user id对应的name
                    sendRoomMsg(buildCodeWithMsg("<from>" + user.getId() + "</from><smsg>" + message + "</smsg>", 4));
                    break;
                case "create":
                    // create a room
                    // code=5,提示用户进入了房间
                    // code=15,需要在其他所有用户的room列表中更新
                    roomId = map.createRoom(message);
                    map.join(user, roomId);
                    sendMsg(buildCodeWithMsg("<rid>" + roomId + "</rid><rname>" + message + "</rname>", 15));
                    returnMsg(buildCodeWithMsg("你进入了创建的房间：" + map.getRoom(roomId).getName(), 5));
                    returnMsg(buildCodeWithMsg(getMembersInRoom(), 21));
                    break;
                case "setname":
                    // set name for user
                    // code=16,告诉房间里的其他人，你改了昵称
                    user.setName(message);
                    sendRoomMsg(buildCodeWithMsg("<id>" + user.getId() + "</id><name>" + message + "</name>", 16));
                    break;
                default:
                    // returnMsg("something unknown");
                    System.out.println("not valid message from user" + user.getId());
                    break;
            }
        }
    }

    /**
     * 获得该用户房间中的所有用户列表，并构造成一定格式的消息返回
     *
     * @return
     */
    private String getMembersInRoom() {
        /*先从room列表获得该用户的room*/
        Room room = map.getRoom(roomId);
        StringBuffer stringBuffer = new StringBuffer();
        if (room != null) {
            /*获得房间中所有的用户的列表，然后构造成一定的格式发送回去*/
            ArrayList<User> users = room.getUsers();
            for (User each : users) {
                stringBuffer.append("<member><name>" + each.getName() +
                        "</name><id>" + each.getId() + "</id></member>");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 获得所有房间的列表，并构造成一定的格式
     *
     * @return
     */
    private String getRoomsList() {
        String[][] strings = map.listRooms();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            sb.append("<room><rname>" + strings[i][1] +
                    "</rname><rid>" + strings[i][0] + "</rid></room>");
        }
        return sb.toString();
    }

    /**
     * 构造成一个统一的消息格式
     *
     * @param msg
     * @param code
     * @return
     */
    private String buildCodeWithMsg(String msg, int code) {
        return "<code>" + code + "</code><msg>" + msg + "</msg>\n";
    }

    /**
     * 这个是群发消息：全体用户,code>10
     *
     * @param msg
     */
    private void sendMsg(String msg) {
//  System.out.println("In sendMsg()");
        /*取出用户列表中的每一个用户来发送消息*/
        for (User each : userList) {
            try {
                pw = each.getPw();
                pw.println(msg);
                pw.flush();
                System.out.println(msg);
            } catch (Exception e) {
                System.out.println("exception in sendMsg()");
            }
        }
    }

    /**
     * 只对同一房间的用户发：code>10
     *
     * @param msg
     */
    private void sendRoomMsg(String msg) {
        /*先获得该用户的房间号，然后往该房间发送消息*/
        Room room = map.getRoom(roomId);
        if (room != null) {
            ArrayList<User> users = room.getUsers();
            for (User each : users) {
                pw = each.getPw();
                pw.println(msg);
                pw.flush();
            }
        }
    }

    /**
     * 向房间中除了该用户自己，发送消息
     *
     * @param msg
     */
    private void sendRoomMsgExceptSelf(String msg) {
        Room room = map.getRoom(roomId);
        if (room != null) {
            ArrayList<User> users = room.getUsers();
            for (User each : users) {
                if (each.getId() != user.getId()) {
                    pw = each.getPw();
                    pw.println(msg);
                    pw.flush();
                }
            }
        }
    }

    /**
     * 对于client的来信，返回一个结果,code<10
     *
     * @param msg
     */
    private void returnMsg(String msg) {
        try {
            pw = user.getPw();
            pw.println(msg);
            pw.flush();
        } catch (Exception e) {
            System.out.println("exception in returnMsg()");
        }
    }

    /**
     * 移除该用户，并向房间中其他用户发送该用户已经退出的消息
     * 如果房间中没人了，那么就更新房间列表给所有用户
     *
     * @param user
     */
    private void remove(User user) {
        if (roomId != -1) {
            int flag = map.esc(user, roomId);
            sendRoomMsgExceptSelf(buildCodeWithMsg("" + user.getId(), 12));
            long oldRoomId = roomId;
            roomId = -1;
            if (flag == 0) {
                sendMsg(buildCodeWithMsg("" + oldRoomId, 13));
            }
        }
        userList.remove(user);
    }
}

