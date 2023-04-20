package com.example.assignment2_client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public ImageView myImage;
    public Label showMyName;
    public Label showMyAccount;
    public TextField textSearch;
    public ImageView emoj;
    public Button btnSend;
    public Label showFriendName;
    public ScrollPane friendSCP;
    public VBox friendVBox;
    public VBox msgVBox;
    public ScrollPane msgSCP;
    @FXML
    TextArea inputArea;
    PrintWriter writer;
    String userName;
    private boolean updateFlag = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String[] imgs = {"photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg"};
        for (int i = 1; i <= 20; i++) {
            int count = (int) (Math.random() * 100);
            AnchorPane an = new AnchorPane();
            an.setPrefHeight(60);
            ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Image/" + imgs[count % 4])).toExternalForm()));
            iv.setFitWidth(40);
            iv.setFitHeight(40);
            Label l1 = new Label("朋友" + i);
            l1.setFont(new Font(15));
            l1.setMaxWidth(160);
            Label l2 = new Label("今天天气不错");
            l2.setFont(new Font(12));
            l2.setMaxWidth(160);
            l2.setTextFill(Paint.valueOf("#aba6a6"));
            an.getChildren().addAll(iv, l1, l2);
            AnchorPane.setLeftAnchor(iv, 34.0);
            AnchorPane.setTopAnchor(iv, 10.0);
            AnchorPane.setLeftAnchor(l1, 91.0);
            AnchorPane.setTopAnchor(l1, 10.0);
            AnchorPane.setLeftAnchor(l2, 91.0);
            AnchorPane.setTopAnchor(l2, 36.0);
            friendVBox.getChildren().add(an);
            an.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent arg0) {
                    an.setCursor(Cursor.HAND);
                }
            });
            an.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    showFriendName.setText(l1.getText());
                }
            });
        }
        inputArea.setFont(new Font(14));
        msgSCP.setVvalue(1.0);
        msgSCP.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (updateFlag) {
                    msgSCP.setVvalue(1.0);
                    updateFlag = false;
                }
            }
        });
        myImage.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                myImage.setCursor(Cursor.HAND);
            }
        });
        btnSend.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                btnSend.setCursor(Cursor.HAND);
            }
        });
        emoj.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                emoj.setCursor(Cursor.HAND);
            }
        });
        inputArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    sendMsg();
                }
            }
        });
    }

    public void sendMsg() {
        try {
            String ss = inputArea.getText();
            System.out.println("Send " + ss);
            Message message = new Message(new Date(), "1201736", "12011736", ss, "chat");
            String mm = message.serialize();
            writer.println(mm);
            showSendMsg(ss);
            inputArea.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage(ActionEvent event) {
        //空消息不允许发送
        if (inputArea.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText("空消息警告");
            alert.setContentText("不允许发送空消息，请您重新编辑内容");
            alert.showAndWait();
            return;
        }
        sendMsg();
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void setUserName(String userName, String account) {
        this.userName = userName;
        showMyName.setText(userName);
        showMyAccount.setText(account);
    }

    void showSendMsg(String msg) {
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo1.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);

        Label l1 = new Label(userName);
        l1.setFont(new Font(13));
        l1.setTextAlignment(TextAlignment.RIGHT);
        l1.setMaxWidth(400);

        Label l2 = new Label(msg);
        l2.setFont(new Font(18));
        l2.setStyle("-fx-background-color: #ffffff");
        l2.setPadding(new Insets(5, 10, 5, 10));
        l2.setWrapText(true);
        l2.setMaxWidth(400);

        an.getChildren().addAll(iv, l1, l2);
        AnchorPane.setLeftAnchor(iv, 537.0);
        AnchorPane.setTopAnchor(iv, 25.0);
        AnchorPane.setRightAnchor(l1, 56.0);
        AnchorPane.setTopAnchor(l1, 25.0);
        AnchorPane.setRightAnchor(l2, 56.0);
        AnchorPane.setTopAnchor(l2, 55.0);
        msgVBox.getChildren().add(an);
    }

    void showRecvMsg(Message msg) {
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo2.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);

        Label l1 = new Label("好友名字");
        l1.setFont(new Font(13));
        l1.setMaxWidth(400);
        Label l2 = new Label(msg.data);
        l2.setFont(new Font(18));
        l2.setStyle("-fx-background-color: #55a3ec");
        l2.setPadding(new Insets(5, 10, 5, 10));
        l2.setMaxWidth(400);
        l2.setWrapText(true);

        an.getChildren().addAll(iv, l1, l2);
        AnchorPane.setLeftAnchor(iv, 22.0);
        AnchorPane.setTopAnchor(iv, 23.0);
        AnchorPane.setLeftAnchor(l1, 76.0);
        AnchorPane.setTopAnchor(l1, 23.0);
        AnchorPane.setLeftAnchor(l2, 76.0);
        AnchorPane.setTopAnchor(l2, 51.0);
        msgVBox.getChildren().add(an);
    }

    @FXML
    void emojClickAction(MouseEvent event) {

    }

    //头像点击事件
    @FXML
    void mouseClickMyImage(MouseEvent event) {

    }
}