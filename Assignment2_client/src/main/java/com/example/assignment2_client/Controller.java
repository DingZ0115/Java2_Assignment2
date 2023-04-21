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
    @FXML
    ImageView portrait;
    @FXML
    Label showMyName;
    @FXML
    Label showMyAccount;
    @FXML
    TextField textSearch;
    @FXML
    ImageView emoj;
    @FXML
    Button btnSend;
    @FXML
    Label showFriendName;
    @FXML
    ScrollPane friendSCP;
    @FXML
    VBox friendVBox;
    @FXML
    VBox msgVBox;
    @FXML
    ScrollPane msgSCP;
    @FXML
    TextArea inputArea;
    @FXML
    ImageView create;
    @FXML
    ImageView exit;
    PrintWriter writer;
    Stage curStage;

    String personal_signature;

    boolean updateFlag = false;

    String curPrivateChatUser;


    HashMap<String, String[]> onlineUserMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        portrait.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                portrait.setCursor(Cursor.HAND);
            }
        });
        create.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                create.setCursor(Cursor.HAND);
            }
        });
        exit.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                exit.setCursor(Cursor.HAND);
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
        //按回车发送
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
            Message message = new Message(new Date(), showMyAccount.getText(), curPrivateChatUser, ss, "chat");
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

    public void setInfo(String info) {
        String[] onlineUsers = info.split("%");
        String[] host = onlineUsers[0].split("\\|");
        personal_signature = host[2];
        curPrivateChatUser = host[0];
        showMyName.setText(host[1]);
        showMyAccount.setText(host[0]);
        showFriendName.setText(host[1]);
        for (int i = 1; i < onlineUsers.length; i++) {
            String[] user = onlineUsers[i].split("\\|");
            onlineUserMap.put(user[0], user);
        }
        setOnlineUsersPage();
    }

    public void updateOnlineUserMap(Message newUser) {
        String[] newUserInfo = newUser.data.substring(9).split("\\|");
        onlineUserMap.put(newUserInfo[0], newUserInfo);
        setAUserPage(newUserInfo);
        //弹窗提示一个用户来了
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提醒");
        alert.setHeaderText("好友上线提醒");
        alert.setContentText("您的好友" + newUserInfo[1] + "上线了");
        alert.showAndWait();
    }

    public void showSendMsg(String msg) {
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo1.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);

        Label l1 = new Label(showMyName.getText());
        l1.setFont(new Font(13));
        l1.setTextAlignment(TextAlignment.RIGHT);
        l1.setMaxWidth(400);

        Label l2 = new Label(msg);
        l2.setFont(new Font(18));
        l2.setStyle("-fx-background-color: #ffffff;-fx-background-radius: 8;");
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

    public void showRecvMsg(Message msg) {
        //如果发送方不是上一个发送方，要把对话框清空
        if (!msg.sendBy.equals(curPrivateChatUser)) {
            curPrivateChatUser = msg.sendBy;
            showFriendName.setText(onlineUserMap.get(msg.sendBy)[1]);
            msgVBox.getChildren().clear();
        }
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo2.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        Label l1;
        if (msg.sendBy.equals(curPrivateChatUser)) {
            l1 = new Label(showMyName.getText());
        } else {
            l1 = new Label(onlineUserMap.get(msg.sendBy)[1]);
        }
        l1.setFont(new Font(13));
        l1.setMaxWidth(400);
        Label l2 = new Label(msg.data);
        l2.setFont(new Font(18));
        l2.setStyle("-fx-background-color: #55a3ec;-fx-background-radius: 8;");
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

    public void mouseClickCreate(MouseEvent mouseEvent) {
        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/bigstar.png")).toExternalForm()));
        stage.setTitle("请选择群聊好友");
        stage.setWidth(280);
        stage.setHeight(400);
        stage.initOwner(curStage);
        stage.initModality(Modality.WINDOW_MODAL);

        ScrollPane scrollPane = new ScrollPane();// 使用一个滚动板面
        VBox box = new VBox(); // 滚动板面里放行垂直布局， VBox里放多个复选框

        String[] allTable = {"1111", "2222", "3333"};
        ArrayList<String> selectedUser = new ArrayList<>();
        for (String t : allTable) {
            CheckBox cb = new CheckBox(t);
            cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov,
                                    Boolean old_val, Boolean new_val) {
                    if (cb.isSelected()) {
                        selectedUser.add(t);
                    } else {
                        selectedUser.remove(t);
                    }
                }
            });
            box.getChildren().add(cb);
            box.setMargin(cb, new Insets(10, 10, 0, 10));// 设置间距
        }
        Button button = new Button("确认创建群聊");
        button.setStyle("-fx-background-color:#ffffff");

        box.getChildren().add(button);
        box.setMargin(button, new Insets(10, 10, 0, 10));
        scrollPane.setContent(box);
        Scene scene = new Scene(scrollPane);

        stage.setScene(scene);
        stage.show();

        button.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setCursor(Cursor.HAND);
            }
        });
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                confirmCreateGroup(selectedUser);
                stage.close();
            }
        });
    }

    public void mouseClickExit(MouseEvent mouseEvent) {
        exitLogin();
    }

    public void confirmCreateGroup(ArrayList<String> selectedUser) {
        for (String s : selectedUser) {
            System.out.println(s);
        }
    }

    public void exitLogin() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("提示");
        alert.setHeaderText("确定要退出登录吗?");

        Stage alertS = (Stage) alert.getDialogPane().getScene().getWindow();
        alertS.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/haimian.png")).toExternalForm()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println("您已退出登录");
            curStage.setScene(Client.SignScene);
        }
        alert.close();
    }

    @FXML
    public void emojClickAction(MouseEvent event) {

    }

    //头像点击事件
    @FXML
    public void mouseClickMyImage(MouseEvent event) {
        DialogPane dp = new DialogPane();

        dp.setPrefWidth(200);
        dp.setPrefHeight(230);

        BorderPane bp = new BorderPane();

        ImageView myInfo = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/account.png")).toExternalForm()));
        myInfo.setFitWidth(200);
        myInfo.setFitHeight(200);
        bp.setCenter(myInfo);

        Label l1 = new Label(personal_signature);
        l1.setFont(new Font(15));
        l1.setMaxWidth(400);

        AnchorPane ap = new AnchorPane();
        ap.setPrefWidth(200);
        ap.setPrefHeight(50);
        ap.setStyle("-fx-background-color:#ffffff");
        ap.getChildren().addAll(l1);
        AnchorPane.setLeftAnchor(l1, 10.0);
        AnchorPane.setTopAnchor(l1, 5.0);

        bp.setBottom(ap);
        dp.getChildren().add(bp);
        Stage s = new Stage();
        s.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/bigstar.png")).toExternalForm()));
        s.setResizable(false);
        Scene sc = new Scene(dp);
        s.setScene(sc);
        //设置所有者
        s.initOwner(curStage);
        s.initModality(Modality.WINDOW_MODAL);
        s.setX(MouseInfo.getPointerInfo().getLocation().x);
        s.setY(MouseInfo.getPointerInfo().getLocation().y);
        s.show();
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void setCurStage(Stage curStage) {
        this.curStage = curStage;
    }

    public void setOnlineUsersPage() {
        //根据map生成在线用户页面
        //把自己放最上面，然后是别人
        String[] personalValue = {showMyAccount.getText(), showMyName.getText(), personal_signature};
        setAUserPage(personalValue);
        for (Map.Entry<String, String[]> entry : onlineUserMap.entrySet()) {
            if (!entry.getKey().equals(showMyAccount.getText())) {
                String[] value = entry.getValue();
                for (int i = 0; i < value.length; i++) {
                    System.out.println(value[i]);
                }
                setAUserPage(entry.getValue());
            }
        }
    }

    public void setAUserPage(String[] value) {
        String[] imgs = {"photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg"};
        int count = (int) (Math.random() * 100);
        AnchorPane an = new AnchorPane();
        an.setPrefHeight(60);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Image/" + imgs[count % 4])).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);

        Label l1 = new Label(value[1]);
        l1.setFont(new Font(15));
        l1.setMaxWidth(160);
        Label l2 = new Label(value[2]);
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
                if (!value[0].equals(curPrivateChatUser)) {
                    curPrivateChatUser = value[0];
                    msgVBox.getChildren().clear();//清空聊天框的内容
                }
            }
        });
    }
}