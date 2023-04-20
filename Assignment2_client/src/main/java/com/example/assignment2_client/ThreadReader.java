package com.example.assignment2_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

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
            // TODO exhdl
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message = "";
            while ((message = bufferedReader.readLine()) != null) {
                Message decodedMessage = deserialize(message);
                if (decodedMessage.method.equals("responseSignUp") || decodedMessage.method.equals("responseSignIn")) {
                    Client.setReplyMsg(decodedMessage);
                    Client.setReceiveMsgOrNot(true);
                } else if (decodedMessage.method.equals("chat")) {
                    System.out.println("chat!!!!");
                    Platform.runLater(() -> {
                        curController.chatContentList.getItems().add(decodedMessage);
                    });
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Message deserialize(String s) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(s, Message.class);
    }
}
