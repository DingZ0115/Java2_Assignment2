package com.example.assignment2_client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.MouseInfo;
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
    Stage curStage;
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

    public void setCurStage(Stage curStage) {
        this.curStage = curStage;
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
        DialogPane dp = new DialogPane();

        dp.setPrefWidth(200);
        dp.setPrefHeight(230);

        BorderPane bp = new BorderPane();

        ImageView myInfo = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/account.png")).toExternalForm()));

        myInfo.setFitWidth(200);
        myInfo.setFitHeight(200);

        bp.setCenter(myInfo);

        AnchorPane ap = new AnchorPane();
        ap.setPrefWidth(200);
        ap.setPrefHeight(50);
        ap.setStyle("-fx-background-color:#ffffff");

        ImageView icon1 = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/addfriend.png")).toExternalForm()));
        icon1.setFitWidth(20);
        icon1.setFitHeight(20);

        ImageView icon2 = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/exit.png")).toExternalForm()));
        icon2.setFitWidth(20);
        icon2.setFitHeight(20);

        Button button1 = new Button();
        button1.setGraphic(icon1);
        button1.setStyle("-fx-background-color:#ffffff");

        Button button2 = new Button();
        button2.setGraphic(icon2);
        button2.setStyle("-fx-background-color:#ffffff");

        ap.getChildren().addAll(button1, button2);

        AnchorPane.setRightAnchor(button1, 5.0);
        AnchorPane.setTopAnchor(button1, 5.0);

        AnchorPane.setRightAnchor(button2, 40.0);
        AnchorPane.setTopAnchor(button2, 5.0);

        bp.setBottom(ap);

        dp.getChildren().add(bp);


        Stage s = new Stage();
        s.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/haimian.png")).toExternalForm()));
        s.setResizable(false);
        Scene sc = new Scene(dp);
        s.setScene(sc);
        //设置所有者
        s.initOwner(curStage);
        s.initModality(Modality.WINDOW_MODAL);
        s.setX(MouseInfo.getPointerInfo().getLocation().x);
        s.setY(MouseInfo.getPointerInfo().getLocation().y);
        s.show();

//        button1.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                addFriendWindown();
//            }
//        });
//
        button2.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                exitLogin(s);
            }
        });

        button1.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button1.setCursor(Cursor.HAND);
            }
        });
        button2.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button2.setCursor(Cursor.HAND);
            }
        });
    }

    void exitLogin(Stage s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("提示");
        alert.setHeaderText("确定要退出登录吗?");

        Stage alertS = (Stage) alert.getDialogPane().getScene().getWindow();
        alertS.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/haimian.png")).toExternalForm()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (s != null) {
                s.close();
            }
            System.out.println("您已退出登录");
            curStage.setScene(Client.SignScene);
        }
        alert.close();
    }
}