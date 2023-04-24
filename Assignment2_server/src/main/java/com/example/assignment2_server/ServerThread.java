package com.example.assignment2_server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ServerThread extends Thread {
    Socket socket;
    ConcurrentMap<String, Socket> clients;
    HashMap<String, String> clientsInfo = new HashMap<>(); //账号，账号，昵称，个性签名
    BufferedReader bufferedReader;
    InputStream in;
    OutputStream out;
    PrintWriter writer;
    SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    /**
     * ServerThread类是用于处理与客户端Socket的通信的线程.
     * 该类的构造函数初始化了与Socket相关的流和客户端信息.
     *
     * @param socket      要处理的Socket对象
     * @param clients     存储客户端Socket对象的并发映射表
     * @param clientsInfo 存储客户端信息的哈希映射表
     * @throws IOException 如果发生I/O错误
     */
    public ServerThread(Socket socket, ConcurrentMap<String, Socket> clients,
                        HashMap<String, String> clientsInfo) throws IOException {
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
                case "signIn" -> dealSignIn(message, conn);
                case "signUp" -> dealSignUp(message, conn);
                case "exit" -> dealExit(message);
                case "createGroup" -> dealCreateGroup(message, conn);
                //给所有被选中的人发通知，被加入了群聊
                //在数据库里面创建群聊，给出对应的群聊号，把群聊号返回前端。前端存群聊号，群聊人[]
                case "groupChat" -> dealGroupChat(message, conn);
                case "PrivateSendFile" -> dealPrivateSendFile(message);
                case "getPrivateHistory" ->
                        getHistory(message.getSendBy(), message.getData(), conn, "getPrivateHistory");
                case "getPrivateInitialHistory" ->
                        getHistory(message.getSendBy(), message.getData(), conn, "getPrivateInitialHistory");
                case "getGroupHistory" -> getGroupHistroy(message, conn);
                default -> dealChat(message, conn);
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

    public void dealSignIn(Message message, Connection conn) {
        try {
            String[] accountAndPasswd = message.data.split("%");
            String xx = "";
            if (clients.containsKey(accountAndPasswd[0])) {
                xx = "Already login";
            } else if (!clients.containsKey(accountAndPasswd[0])) {
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE account = ? AND passwd = ?;");
                pstmt.setString(1, accountAndPasswd[0]);
                pstmt.setString(2, accountAndPasswd[1]);
                ResultSet rs = pstmt.executeQuery();
                boolean flag = rs.next();
                // 处理查询结果
                if (flag) { //上线成功
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
                    //把key按照聊天时间进行排序
                    PreparedStatement getChatTime = conn.prepareStatement(
                            "SELECT * FROM private_chat_list WHERE sendby_account = ?  OR sendto_account = ?");
                    getChatTime.setString(1, accountAndPasswd[0]);
                    getChatTime.setString(2, accountAndPasswd[0]);
                    ResultSet timeUser = getChatTime.executeQuery();
                    HashMap<String, Date> timeUserMap = new HashMap<>();
                    while (timeUser.next()) {
                        String from = timeUser.getString("sendby_account");
                        String to = timeUser.getString("sendto_account");
                        Date time = format.parse(timeUser.getString("time"));
                        if (!from.equals(accountAndPasswd[0])) {
                            timeUserMap.put(from, time);
                        } else if (!to.equals(accountAndPasswd[0])) {
                            timeUserMap.put(to, time);
                        }
                    }

                    for (String key : keySet) {
                        sb.append("%");
                        String[] oneClientInfo = clientsInfo.get(key).split("\\|");
                        sb.append(oneClientInfo[0]);
                        sb.append("|");
                        sb.append(oneClientInfo[1]);
                        sb.append("|");
                        sb.append(oneClientInfo[2]);
                        sb.append("|");
                        if (timeUserMap.containsKey(key)) {
                            sb.append(timeUserMap.get(key));
                        } else {
                            sb.append(new Date(0));
                        }
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
            Thread.sleep(200);
        } catch (SQLException | IOException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void dealSignUp(Message message, Connection conn) {
        try {
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
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO users (user_name, account, passwd,personal_signature)\n"
                                +
                                "VALUES (?, ?, ?, ?);");
                pstmt.setString(1, information[0]);
                pstmt.setString(2, information[1]);
                pstmt.setString(3, information[2]);
                pstmt.setString(4, information[3]);
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
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dealChat(Message m, Connection conn) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO private_chat_list (content, time, sendby_account, sendto_account) "
                            +
                            "VALUES (?,?,?,?);");
            pstmt.setString(1, m.getData());
            pstmt.setString(2, m.getTimestamp().toString());
            pstmt.setString(3, m.getSendBy());
            pstmt.setString(4, m.getSendTo());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Socket toSocket = clients.get(m.getSendTo());
                Message transMessage = new Message(new Date(), m.getSendBy(), m.getSendTo(), m.getData(), "chat");
                String mm = transMessage.serialize();
                OutputStream out = toSocket.getOutputStream();
                PrintWriter w = new PrintWriter(out, true);
                w.println(mm);
            }
            pstmt.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dealPrivateSendFile(Message m) {
        try {
            String sendTo = m.getSendTo();
            Socket toSocket = clients.get(sendTo);
            Message transMessage = new Message(new Date(), m.getSendBy(), sendTo,
                    m.getData(), "TransferFile");
            String mm = transMessage.serialize();
            OutputStream out = toSocket.getOutputStream();
            PrintWriter w = new PrintWriter(out, true);
            w.println(mm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void dealGroupChat(Message m, Connection conn) {
        try {
            String[] sendBy = m.getSendBy().split("%");  //[0]为群号，[1]为发消息人的昵称，[2]为发消息人账号
            //把这条消息放到数据库
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO group_chat_list(group_id, content,"
                    +
                    " time, user_account) VALUES(?,?,?,?);");
            pstmt.setInt(1, Integer.parseInt(sendBy[0]));
            pstmt.setString(2, m.getData());
            pstmt.setString(3, m.getTimestamp().toString());
            pstmt.setString(4, sendBy[2]);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
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
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dealCreateGroup(Message message, Connection conn) {
        try {
            String[] groupUsers = message.getData().split("%");

            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO groups (group_name) VALUES (?);",
                    Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, message.getData());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int groupNumber = rs.getInt(1);
                //通知所有人
                for (String groupUser : groupUsers) {
                    try {
                        String groupUserInfo = groupNumber + "%" + message.getData();
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
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public void getHistory(String requestUser, String curChat, Connection conn, String method) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM private_chat_list WHERE (sendby_account = ? AND sendto_account = ?) "
                            + "OR (sendby_account = ? AND sendto_account = ?)");
            pstmt.setString(1, requestUser);
            pstmt.setString(2, curChat);
            pstmt.setString(3, curChat);
            pstmt.setString(4, requestUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String content = rs.getString("content");
                String time = rs.getString("time");
                String user1 = rs.getString("sendby_account");
                String user2 = rs.getString("sendto_account");
                Message msg = new Message(format.parse(time), user1, user2, content, method);
                Socket toSocket = clients.get(requestUser);
                String mm = msg.serialize();
                OutputStream out = toSocket.getOutputStream();
                PrintWriter w = new PrintWriter(out, true);
                w.println(mm);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException | ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getGroupHistroy(Message message, Connection conn) {
        String requestUser = message.getSendBy();
        String groupNumber = message.getData();
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM group_chat_list WHERE group_id = ?");
            pstmt.setInt(1, Integer.parseInt(groupNumber));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String content = rs.getString("content");
                String time = rs.getString("time");
                String spokenUser = rs.getString("user_account");
                PreparedStatement checkName = conn.prepareStatement("SELECT * FROM users WHERE account = ?");
                checkName.setString(1, spokenUser);
                ResultSet checkNameRs = checkName.executeQuery();
                if (checkNameRs.next()) {
                    String spokenUserName = checkNameRs.getString("user_name");
                    Message msg = new Message(format.parse(time), spokenUser, spokenUserName,
                            content, "getGroupHistory");
                    Socket toSocket = clients.get(requestUser);
                    String mm = msg.serialize();
                    OutputStream out = toSocket.getOutputStream();
                    PrintWriter w = new PrintWriter(out, true);
                    w.println(mm);
                }
                checkName.close();
            }
            rs.close();
            pstmt.close();
        } catch (SQLException | ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

