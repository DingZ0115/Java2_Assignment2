package com.example.assignment2_server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ServerThread extends Thread {
    Socket socket;
    ConcurrentMap<String, Socket> clients;
    HashMap<String, String> clientsInfo = new HashMap<>(); //账号，账号，昵称，个性签名
    BufferedReader bufferedReader;

    InputStream in;
    OutputStream out;
    PrintWriter writer;

    public ServerThread(Socket socket, ConcurrentMap<String, Socket> clients, HashMap<String, String> clientsInfo) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.clientsInfo = clientsInfo;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        writer = new PrintWriter(out, true);
        bufferedReader = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void run() {
        try {
            String message = "";
            while ((message = bufferedReader.readLine()) != null) {
                Message decodedMessage = deserialize(message);
                query(decodedMessage);
            }
        } catch (SocketException e) {
            System.out.println("一个用户离开了");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Message deserialize(String s) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(s, Message.class);
    }

    public void query(Message message) throws SQLException, IOException, ClassNotFoundException {
        // 加载 JDBC 驱动程序
        String driver = "org.postgresql.Driver";
        Connection conn = null;
        try {
            Class.forName(driver);
            String user = "postgres";
            // 数据库的 URL 和用户名、密码
            String url = "jdbc:postgresql://localhost/postgres";
            String password = "123456";
            conn = DriverManager.getConnection(url, user, password);

            switch (message.getMethod()) {
                case "signIn" -> {
                    String[] accountAndPasswd = message.data.split("%");
                    String xx = "";
                    if (clients.containsKey(accountAndPasswd[0])) {
                        xx = "Already login";
                    } else if (!clients.containsKey(accountAndPasswd[0])) {
                        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE account = ? AND passwd = ?;");
                        pstmt.setString(1, accountAndPasswd[0]);
                        pstmt.setString(2, accountAndPasswd[1]);
                        ResultSet rs = pstmt.executeQuery();
                        boolean flag = rs.next();
                        // 处理查询结果
                        if (flag) {//上线成功
                            //向列表中添加一个新用户，更新Main中的列表，返回前端所有当前在线的用户
                            String nameAndSignature = accountAndPasswd[0] + "|"
                                    + rs.getString("user_name") + "|"
                                    + rs.getString("personal_signature");
                            System.out.println("用户" + accountAndPasswd[0] + "来了");
                            StringBuilder sb = new StringBuilder();
                            sb.append("responseSignIn-Yes");
                            sb.append("%");
                            sb.append(nameAndSignature);
                            Set<String> keySet = clients.keySet();  // 获取所有的key
                            for (String key : keySet) {
                                sb.append("%");
                                String[] oneClientInfo = clientsInfo.get(key).split("\\|");
                                sb.append(oneClientInfo[0]);
                                sb.append("|");
                                sb.append(oneClientInfo[1]);
                                sb.append("|");
                                sb.append(oneClientInfo[2]);
                            }
                            xx = sb.toString();
                            String broadcast = "AUserCome" + nameAndSignature;
                            //更新所有其他用户的在线用户列表
                            for (String key : keySet) {
                                Socket toSocket = clients.get(key);
                                Message transMessage = new Message(new Date(), "server", key, broadcast, "broadcast");
                                String mm = transMessage.serialize();
                                OutputStream out = toSocket.getOutputStream();
                                PrintWriter w = new PrintWriter(out, true);
                                w.println(mm);
                            }
                            clients.put(accountAndPasswd[0], socket);
                            clientsInfo.put(accountAndPasswd[0], nameAndSignature);
                            Main.setClients(clients);
                            Main.setClientsInfo(clientsInfo);
                            rs.close();
                            pstmt.close();
                        }
                    } else {
                        xx = "responseSignIn-No";
                    }
                    Message response = new Message(new Date(), "0", "0", xx, "responseSignIn");
                    String mm = response.serialize();
                    writer.println(mm);
                    break;
                }
                case "signUp" -> {
                    String[] information = message.data.split("%");
                    PreparedStatement pstmt0 = conn.prepareStatement("SELECT * FROM users WHERE account = ? ;");
                    pstmt0.setString(1, information[1]);
                    ResultSet rs = pstmt0.executeQuery();
                    boolean flag = !rs.next();
                    pstmt0.close();
                    rs.close();
                    // 处理查询结果
                    String xx;
                    if (flag) {
                        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (user_name, account, passwd)\n" +
                                "VALUES (?, ?, ?);");

                        pstmt.setString(1, information[0]);
                        pstmt.setString(2, information[1]);
                        pstmt.setString(3, information[2]);
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            xx = "responseSignUp-Yes";
                            System.out.println("插入成功！");
                        } else {
                            xx = "responseSignUp-No";
                            System.out.println("插入失败！");
                        }
                        pstmt.close();
                    } else {
                        xx = "responseSignUp-No";
                        System.out.println("插入失败！");
                    }
                    Message response = new Message(new Date(), "0", "0", xx, "responseSignUp");
                    String mm = response.serialize();
                    writer.println(mm);
                    break;
                }
                case "exit" -> dealExit(message);
                case "createGroup" ->
                    //给所有被选中的人发通知，被加入了群聊
                    //在数据库里面创建群聊，给出对应的群聊号，把群聊号返回前端。前端存群聊号，群聊人[]
                        dealCreateGroup(message);
                case "groupChat" -> dealGroupChat(message);
                case "PrivateSendFile" -> dealPrivateSendFile(message);
                default -> dealChat(message);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find the PostgreSQL JDBC driver class.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Could not connect to the PostgreSQL database.");
            e.printStackTrace();
        } finally {
            // 关闭数据库连接
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error while closing the database connection.");
                e.printStackTrace();
            }
        }
    }

    public void dealChat(Message m) {
        try {
            String sendTo = m.getSendTo();
            Socket toSocket = clients.get(sendTo);
            Message transMessage = new Message(new Date(), m.getSendBy(), sendTo, m.getData(), "chat");
            String mm = transMessage.serialize();
            OutputStream out = toSocket.getOutputStream();
            PrintWriter w = new PrintWriter(out, true);
            w.println(mm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void dealPrivateSendFile(Message m) {
        try {
            String sendTo = m.getSendTo();
            Socket toSocket = clients.get(sendTo);
            Message transMessage = new Message(new Date(), m.getSendBy(), sendTo, m.getData(), "TransferFile");
            String mm = transMessage.serialize();
            OutputStream out = toSocket.getOutputStream();
            PrintWriter w = new PrintWriter(out, true);
            w.println(mm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void dealGroupChat(Message m) {
        //给所有groupUsers中的所有人发消息，被加入了群聊
        String[] groupUsers = m.getSendTo().split("%");
        for (String groupUser : groupUsers) {
            try {
                if (clients.containsKey(groupUser)) {
                    Socket toSocket = clients.get(groupUser);
                    Message transMessage = new Message(new Date(), m.sendBy,
                            groupUser, m.getData(), "broadcastGroupChat");
                    String mm = transMessage.serialize();
                    OutputStream out = toSocket.getOutputStream();
                    PrintWriter w = new PrintWriter(out, true);
                    w.println(mm);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void dealCreateGroup(Message message) {
        String[] groupUsers = message.getData().split("%");
        for (String groupUser : groupUsers) {
            try {
                String groupUserInfo = "1234567" + "%" + message.getData();
                if (clients.containsKey(groupUser)) {
                    Socket toSocket = clients.get(groupUser);
                    Message transMessage = new Message(new Date(), clientsInfo.get(message.sendBy), groupUser,
                            groupUserInfo, "broadcastCreateGroup");
                    String mm = transMessage.serialize();
                    OutputStream out = toSocket.getOutputStream();
                    PrintWriter w = new PrintWriter(out, true);
                    w.println(mm);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void dealExit(Message message) {
        try {
            String createUserInfo = clientsInfo.get(message.getData());
            System.out.println("用户" + message.getData() + "离开了");
            clients.remove(message.getData());
            clientsInfo.remove(message.getData());
            Set<String> keySet = clients.keySet();  // 获取所有的key
            for (String key : keySet) {
                Socket toSocket = clients.get(key);
                Message transMessage = new Message(new Date(), "server", key,
                        createUserInfo, "broadcastExit");
                String mm = transMessage.serialize();
                OutputStream out = toSocket.getOutputStream();
                PrintWriter w = new PrintWriter(out, true);
                w.println(mm);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
