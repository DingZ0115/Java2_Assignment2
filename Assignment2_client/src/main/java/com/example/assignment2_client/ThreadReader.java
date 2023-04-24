package com.example.assignment2_client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ThreadReader extends Thread {
    Socket socket;
    BufferedReader bufferedReader;
    Controller curController;

    public ThreadReader(Socket socket, Controller controller) {
        try {
            this.socket = socket;
            this.curController = controller;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message = "";
            while ((message = bufferedReader.readLine()) != null) {
                Message decodedMessage = deserialize(message);
                switch (decodedMessage.method) {
                    case "responseSignUp", "responseSignIn" -> {
                        System.out.println("You are trying to log in.");
                        Client.setReplyMsg(decodedMessage);
                        Client.setReceiveMsgOrNot(true);
                    }
                    case "chat", "broadcastGroupChat", "getPrivateInitialHistory" -> {
                        Platform.runLater(() -> {
                            curController.showRecvMsg(decodedMessage);
                        });
                    }
                    case "broadcast" -> {
                        System.out.println("A new user comes");
                        Platform.runLater(() -> {
                            curController.updateOnlineUserMap(decodedMessage);
                        });
                    }
                    case "broadcastExit" -> {
                        System.out.println("A new user leaves");
                        Platform.runLater(() -> {
                            curController.exitOnlineUserMap(decodedMessage);
                        });
                    }
                    case "broadcastCreateGroup" -> {
                        System.out.println("您已成功加入群聊");
                        Platform.runLater(() -> {
                            curController.responseCreateGroup(decodedMessage);
                        });
                    }
                    case "TransferFile" -> curController.saveFile(decodedMessage);
                    case "getPrivateHistory", "getGroupHistory" -> {
                        Platform.runLater(() -> {
                            curController.showHistory(decodedMessage);
                        });
                    }
                    default -> {

                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("服务器挂了");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("警告");
                alert.setHeaderText("服务器因不明原因断开连接");
                alert.setContentText("点击确定退出程序");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    System.exit(0);
                }
                alert.close();
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Message deserialize(String s) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(s, Message.class);
    }

}
