package com.example.assignment2_server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

public class ServerThread extends Thread {
    Socket socket;
    ConcurrentMap<String, Socket> clients;
    BufferedReader bufferedReader;

    InputStream in;
    OutputStream out;
    PrintWriter writer;

    public ServerThread(Socket socket, ConcurrentMap<String, Socket> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
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
                System.out.println(decodedMessage.getData());
                query(decodedMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            //TODO: handle e
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
            // 连接数据库
            String user = "postgres";
            // 数据库的 URL 和用户名、密码
            String url = "jdbc:postgresql://localhost/postgres";
            String password = "123456";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");

            if (message.getMethod().equals("signIn")) {
                String[] accountAndPasswd = message.data.split("%");
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE account = ? AND passwd = ?;");
                pstmt.setString(1, accountAndPasswd[0]);
                pstmt.setString(2, accountAndPasswd[1]);
                ResultSet rs = pstmt.executeQuery();
                boolean flag = rs.next();
                // 处理查询结果
                String xx;
                if (flag) {
                    xx = "responseSignIn-Yes"+rs.getString("user_name");
                    //向列表中添加一个新用户，更新Main中的列表
                    clients.put(accountAndPasswd[0], socket);
                    Main.setClients(clients);
                } else {
                    xx = "responseSignIn-No";
                }
                Message response = new Message(new Date(), "0", "0", xx, "responseSignIn");
                String mm = response.serialize();
                writer.println(mm);
                rs.close();
                pstmt.close();
            } else if (message.getMethod().equals("signUp")) {
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
            } else {
                System.out.println("chat");
                chat(message);
            }
//            in.close();
//            out.close();
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

    public void chat(Message m) throws IOException {
        String sendBy = m.getSendBy();
        String sendTo = m.getSendTo();
//        Socket fromSocket = clients.get(sendBy);
        Socket toSocket = clients.get(sendTo);
        Message transMessage = new Message(new Date(), sendBy, sendTo, m.getData(), "chat");
        String mm = transMessage.serialize();
        OutputStream out = toSocket.getOutputStream();
        PrintWriter w = new PrintWriter(out, true);
        w.println(mm);
    }
}
