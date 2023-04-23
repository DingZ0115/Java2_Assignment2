package com.example.assignment2_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.MouseInfo;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    @FXML
    ImageView upload;
    @FXML
    ImageView portrait;
    @FXML
    Label showMyName;
    @FXML
    Label showMyAccount;
    @FXML
    TextField textSearch;
    //    @FXML
//    ImageView emoj;
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
    String curChat; //当前通信的账户
    HashMap<String, String[]> onlineUserMap = new HashMap<>();

    HashMap<String, String[]> groupMap = new HashMap<>();
    String speakingPerson; //群聊里面发言的人，在用户发言和用户收到信息都要更新

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        inputArea.setFont(new Font(14));
//        msgSCP.setVvalue(1.0);
        msgSCP.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if (updateFlag) {
                msgSCP.setVvalue(msgVBox.getHeight());
//                    updateFlag = false;
//                }
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
        upload.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                upload.setCursor(Cursor.HAND);
            }
        });
    }

    public void sendMsg() {
        try {
            String ss = inputArea.getText();
            Message message;
            if (onlineUserMap.containsKey(curChat) || showMyAccount.getText().equals(curChat)) {
                message = new Message(new Date(), showMyAccount.getText(), curChat, ss, "chat");
            } else {
                //群聊中curChat存的是群聊号，要给除自己外所有人发消息
                String[] groupUsers = groupMap.get(curChat);//groupUsers[0]为群聊号，groupUsers[1]为发起人
                //所有需要发送的人
                String result = Arrays.stream(groupUsers)
                        .filter(user -> !user.equals(showMyAccount.getText()))
                        .collect(Collectors.joining("%"));

                //群号+自己的昵称
                message = new Message(new Date(), curChat + "%" + showMyName.getText() + "%" + showMyAccount.getText(),
                        result, ss, "groupChat");
                speakingPerson = showMyAccount.getText();
            }
            String mm = message.serialize();
            writer.println(mm);
            showSendMsg(message.getData(), false, message.getTimestamp());
            inputArea.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        curChat = host[0];
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
        setAUserPage(newUserInfo, false);
        //弹窗提示一个用户来了
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提醒");
        alert.setHeaderText("用户上线提醒");
        alert.setContentText("用户" + newUserInfo[1] + "上线了");
        alert.showAndWait();
    }

    public void exitOnlineUserMap(Message exitMsg) {
        String[] info = exitMsg.getData().split("\\|");
        onlineUserMap.remove(info[0]);//从在线列表删除下线用户
        if (!groupMap.containsKey(curChat)) { //好友退出时，用户处于私聊状态
            showFriendName.setText(showMyName.getText());
            msgVBox.getChildren().clear();
            curChat = showMyAccount.getText();
        }
        //更新好友列表
        // 获取VBox中的所有子节点列表
        ObservableList<Node> children = friendVBox.getChildren();

        // 遍历子节点列表，查找要删除的AnchorPane节点
        for (Node node : children) {
            if (node instanceof AnchorPane) {
                AnchorPane anchorPane = (AnchorPane) node;
                if (info[0].equals(anchorPane.getId())) {
                    // 找到了要删除的AnchorPane节点，从VBox中移除它
                    friendVBox.getChildren().remove(anchorPane);
                    break;
                }
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提醒");
        alert.setHeaderText("用户下线提醒");
        alert.setContentText("用户" + info[1] + "下线了");
        alert.showAndWait();
    }

    public void showSendMsg(String data, boolean fileOrNot, Date time) {
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo1.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        Label l1 = new Label();
        Label l2 = new Label();

        if (fileOrNot) {
            l1.setText(time + " " + showMyName.getText());
            l2.setText("\uD83D\uDCC4 " + data);
        } else {
            l1.setText(time + " " + showMyName.getText());
            l2.setText(data);
        }
        l1.setFont(new Font(13));
        l1.setTextAlignment(TextAlignment.RIGHT);
        l1.setMaxWidth(400);
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
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo2.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        Label l1 = new Label();
        Label l2 = new Label();
        //如果发送方不是上一个发送方，要把对话框清空
        if (msg.getMethod().equals("chat")) {
            if (!msg.sendBy.equals(curChat)) {
                msgVBox.getChildren().clear();
                showFriendName.setText(onlineUserMap.get(msg.sendBy)[1]);
            }
            if (msg.sendBy.equals(showMyAccount.getText())) {
                l1.setText(showMyName.getText() + " " + msg.getTimestamp());
            } else {
                l1.setText(onlineUserMap.get(msg.sendBy)[1] + " " + msg.getTimestamp());
            }
            curChat = msg.sendBy;
        } else {
            String[] number_user = msg.getSendBy().split("%");
            if (!number_user[0].equals(curChat)) {
                // TODO: 不能瞎clear，因为群聊发多少条，curChat都不变
                if (!number_user[2].equals(speakingPerson)) {
                    msgVBox.getChildren().clear();
                    showFriendName.setText(number_user[0]);
                }
            }
            l1.setText(number_user[1] + " " + msg.getTimestamp());
            curChat = number_user[0];
        }
        l2.setText(msg.data);
        l1.setFont(new Font(13));
        l1.setMaxWidth(400);
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

        Set<String> keySet = onlineUserMap.keySet();  // 获取所有的key
        ArrayList<String> selectedUser = new ArrayList<>();
        selectedUser.add(showMyAccount.getText()); //把本人添加到第一个
        for (String t : keySet) {
            CheckBox cb = new CheckBox(onlineUserMap.get(t)[1]);
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
                if (selectedUser.size() < 3) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("警告");
                    alert.setHeaderText("群聊人数警告");
                    alert.setContentText("群聊人数不可以小于3人");
                    alert.showAndWait();
                } else {
                    confirmCreateGroup(selectedUser);
                    stage.close();
                }
            }
        });
    }

    public void mouseClickExit(MouseEvent mouseEvent) {
        exitLogin();
    }

    public void confirmCreateGroup(ArrayList<String> selectedUser) {
        //这里面存的是account
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedUser.size() - 1; i++) {
            sb.append(selectedUser.get(i) + "%");
        }
        sb.append(selectedUser.get(selectedUser.size() - 1));
        //创建一个群聊，名称为xxx,xxx,xxx（3）
        try {
            Message message = new Message(new Date(), showMyAccount.getText(), "0", sb.toString(), "createGroup");
            String mm = message.serialize();
            writer.println(mm);
        } catch (IOException e) {
            e.printStackTrace();
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
            try {
                Message message = new Message(new Date(), "0", "0", showMyAccount.getText(), "exit");
                String mm = message.serialize();
                writer.println(mm);
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alert.close();
    }

    //头像点击事件
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
        setAUserPage(personalValue, false);
        for (Map.Entry<String, String[]> entry : onlineUserMap.entrySet()) {
            if (!entry.getKey().equals(showMyAccount.getText())) {
                setAUserPage(entry.getValue(), false);
            }
        }
    }

    public void setAUserPage(String[] value, boolean groupOrNot) {
        String[] imgs = {"photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg"};
        int count = (int) (Math.random() * 100);
        AnchorPane an = new AnchorPane();
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().
                getResource("/Image/" + imgs[count % 4])).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        //给这个组件设置唯一标识
        an.setId(value[0]);
        an.setPrefHeight(60);
        Label l1;
        Label l2;
        if (groupOrNot) {
            l1 = new Label(value[0]);
            StringBuilder sb = new StringBuilder();
            sb.append("群聊成员：");
            for (int i = 1; i < value.length; i++) {
                sb.append(value[i]).append(" ");
            }
            l2 = new Label(sb.toString());
            Tooltip tooltip = new Tooltip(sb.toString());
            l2.setTooltip(tooltip);
        } else {
            l1 = new Label(value[1]);
            l2 = new Label(value[2]);
        }
        l1.setFont(new Font(15));
        l1.setMaxWidth(160);
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
                if (!value[0].equals(curChat)) {
                    curChat = value[0];
                    System.out.println("curChat " + curChat);
                    msgVBox.getChildren().clear();//清空聊天框的内容
                }
            }
        });
    }

    public void responseCreateGroup(Message msg) {
        //提醒包括本人在内的所有人创建了群聊，更新这些人的群聊列表
        String[] groupInfo = msg.getData().split("%");
        String groupNumber = groupInfo[0];
        groupMap.put(groupNumber, groupInfo);
        setAUserPage(groupInfo, true);
        String hostUser = msg.getSendBy().split("\\|")[1];
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提醒");
        alert.setHeaderText("群组提醒");
        alert.setContentText("您已加入由" + hostUser + "创建的群聊" + groupNumber);
        alert.showAndWait();
    }

    public void uploadClickAction(MouseEvent mouseEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("选择文件");
        File file = fc.showOpenDialog(curStage);
        if (file != null) {
            File dir = new File(file.getParent());
            if (dir.isDirectory()) {
                fc.setInitialDirectory(dir);
            }
            if (!groupMap.containsKey(curChat)) {
                showSendMsg(file.getName(), true, new Date());
                sendFile(file);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setHeaderText("群聊警告");
                alert.setContentText("不支持在群聊中传输文件");
                alert.showAndWait();
            }
        }

    }

    public void sendFile(File file) {
        try {
            Message message = new Message(new Date(), showMyAccount.getText(),
                    curChat, serialize(file), "PrivateSendFile");
            String mm = message.serialize();
            writer.println(mm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRecvFile(String fileName, String sendBy, Date time) {
        AnchorPane an = new AnchorPane();
        an.setPrefWidth(590);
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/photo2.jpg")).toExternalForm()));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        Label l1 = new Label();
        Label l2 = new Label();
        if (!sendBy.equals(curChat)) {
            msgVBox.getChildren().clear();
            showFriendName.setText(onlineUserMap.get(sendBy)[1]);
        }
        if (sendBy.equals(showMyAccount.getText())) {
            l1.setText(showMyName.getText() + " " + time);
        } else {
            l1.setText(onlineUserMap.get(sendBy)[1] + " " + time);
        }
        curChat = sendBy;
        l2.setText("\uD83D\uDCC4 " + fileName);

        l1.setFont(new Font(13));
        l1.setMaxWidth(400);
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

    public void saveFile(Message msg) throws IOException, ClassNotFoundException {
        File fileOrigin = (File) deserializeFile(msg.getData());
        File fileSave = new File("F:/java2File/" + fileOrigin.getName());
        //显示传过来的文件
        Platform.runLater(() -> {
            showRecvFile(fileOrigin.getName(), msg.getSendBy(), msg.getTimestamp());
        });
        fileSave.delete();
        fileSave.createNewFile();
        byte[] bs = new byte[1024];
        FileInputStream originStream = new FileInputStream(fileOrigin);
        FileOutputStream saveStream = new FileOutputStream(fileSave);
        int len;
        while ((len = originStream.read(bs)) != -1) {
            saveStream.write(bs, 0, len);
        }
        originStream.close();
        saveStream.close();
    }

    public String serialize(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(file);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Object deserializeFile(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }
}