package com.example.assignment2_client;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Client extends Application {
    Socket s;
    OutputStream out;
    InputStream in;
    BufferedReader br;
    PrintWriter writer;
    Stage ShowStage = new Stage();
    static GridPane SignPane = new GridPane();// 登录界面的
    static Scene SignScene = new Scene(SignPane, 600, 400);
    GridPane RegisterPane = new GridPane();// 注册界面的
    Scene RegisterScene = new Scene(RegisterPane, 500, 600);
    Scene centerScene;
    static Message replyMsg;
    static volatile boolean receiveMsgOrNot = false;

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/IndexView.fxml"));
    Controller cl;

    public static void main(String[] args) {// ***启动
        launch(args);
    }

    public void start(Stage primaryStage) throws IOException {// UI主轴
        s = new Socket("127.0.0.1", 9999);
        out = s.getOutputStream();
        in = s.getInputStream();
        br = new BufferedReader(new InputStreamReader(in));
        writer = new PrintWriter(out, true);
        centerScene = new Scene(fxmlLoader.load());
        cl = fxmlLoader.getController();
        cl.setWriter(writer);
        ThreadReader reader = new ThreadReader(s, cl);
        reader.start();

        Sign();
        Register();
        ShowStage.setScene(SignScene);
        ShowStage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("/Image/sandy.png")).toExternalForm()));
        ShowStage.initStyle(StageStyle.DECORATED);
        ShowStage.show();
    }

    public void Register() {// ***注册UI
        Label RegisterTitle = new Label("   注册");
        RegisterTitle.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 40));
        Label RegisterAccountLabel = new Label("账号:");
        RegisterAccountLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));
        Label RegisterPasswordLabel = new Label("密码:");
        RegisterPasswordLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));
        Label RegisterIDLabel = new Label("用户名:");
        RegisterIDLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));
        Label RegisterSignatureLabel = new Label("个性签名:");
        RegisterSignatureLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));

        Button RegisterOkButton = new Button("注册");
        RegisterOkButton.setTranslateX(220);
        RegisterOkButton.setTranslateY(450);
        Button RegisterReButton = new Button("返回");
        RegisterReButton.setTranslateX(420);
        RegisterReButton.setTranslateY(550);

        TextField RegisterAccountTextField = new TextField();
        TextField RegisterPasswordTextField = new TextField();
        TextField RegisterIDTextField = new TextField();
        TextField RegisterSignatureTextField = new TextField();

        RegisterPane.setHgap(20);
        RegisterPane.setVgap(20);
        RegisterPane.setBackground(new Background(new BackgroundImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/Register.jpg"))),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));

        RegisterPane.setPadding(new Insets(0, 10, 10, 10));
        RegisterPane.add(RegisterTitle, 3, 2);
        RegisterPane.add(RegisterAccountLabel, 2, 5);
        RegisterPane.add(RegisterPasswordLabel, 2, 7);
        RegisterPane.add(RegisterIDLabel, 2, 9);
        RegisterPane.add(RegisterSignatureLabel, 2, 11);
        RegisterPane.add(RegisterAccountTextField, 3, 5);
        RegisterPane.add(RegisterPasswordTextField, 3, 7);
        RegisterPane.add(RegisterIDTextField, 3, 9);
        RegisterPane.add(RegisterSignatureTextField, 3, 11);

        RegisterPane.getChildren().addAll(RegisterOkButton, RegisterReButton);
        RegisterReButton.setOnAction(e -> ShowStage.setScene(SignScene));
        RegisterOkButton.setOnAction(e -> {
            try {
                String xx = RegisterIDTextField.getText() + "%" + RegisterAccountTextField.getText()
                        + "%" + RegisterPasswordTextField.getText() + "%" + RegisterSignatureTextField.getText();
                Message message = new Message(new Date(), "0", "0", xx, "signUp");
                String mm = message.serialize();
                writer.println(mm);
                while (!receiveMsgOrNot) {
                    Thread.onSpinWait();
                }
                receiveMsgOrNot = false;
                if (replyMsg.data.equals("responseSignUp-Yes")) {
                    //弹出一个注册成功，去登录页面登录
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("恭喜");
                    alert.setHeaderText("注册成功");
                    alert.setContentText("您已经成功注册账号" + RegisterAccountTextField.getText());
                    alert.showAndWait();
                    ShowStage.setScene(SignScene);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("警告");
                    alert.setHeaderText("注册失败");
                    alert.setContentText("您的账号可能已经存在，请更换其他账号进行注册");
                    alert.showAndWait();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void Sign() {// ***登录UI
        Label SignTitle = new Label("    登录");
        SignTitle.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 40));
        SignTitle.setStyle("-fx-text-fill: white;");
        Label SignAccountLabel = new Label("账号:");
        SignAccountLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));
        SignAccountLabel.setStyle("-fx-text-fill: white;");
        Label SignPasswordLabel = new Label("密码:");
        SignPasswordLabel.setFont(Font.font("T", FontWeight.LIGHT, FontPosture.ITALIC, 20));
        SignPasswordLabel.setStyle("-fx-text-fill: white;");
        TextField SignAccountTextField = new TextField();
        TextField SignPasswordTextField = new TextField();
        Button SignOkButton = new Button("登录");
        Button SignRegisterButton = new Button("注册");
        SignOkButton.setTranslateX(250);
        SignOkButton.setTranslateY(250);
        SignRegisterButton.setTranslateX(420);
        SignRegisterButton.setTranslateY(320);
        SignPane.setHgap(20);
        SignPane.setVgap(20);

        SignPane.setPadding(new Insets(0, 10, 10, 10));

        SignPane.add(SignTitle, 4, 2);
        SignPane.add(SignAccountLabel, 3, 3);
        SignPane.add(SignAccountTextField, 4, 3);
        SignPane.add(SignPasswordLabel, 3, 4);
        SignPane.add(SignPasswordTextField, 4, 4);
        SignPane.getChildren().addAll(SignOkButton, SignRegisterButton);
        SignPane.setBackground(new Background(new BackgroundImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/Sign.jpg"))),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));

        SignOkButton.setOnAction(e -> { // 转跳主程序界面
            try {
                String xx = SignAccountTextField.getText() + "%" + SignPasswordTextField.getText();
                Message message = new Message(new Date(), "0", "0", xx, "signIn");
                String mm = message.serialize();
                writer.println(mm);

                while (!receiveMsgOrNot) {
                    Thread.onSpinWait();
                }
                receiveMsgOrNot = false;
                if (replyMsg.data.contains("responseSignIn-Yes")) {
                    ShowStage.setScene(centerScene);
                    ShowStage.setTitle("Chatting Client");
                    ShowStage.show();
                    cl.setInfo(replyMsg.data.substring(19));
                    cl.setCurStage(ShowStage);
                    SignAccountTextField.clear();
                    SignPasswordTextField.clear();
                } else if (replyMsg.data.equals("Already login")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("警告");
                    alert.setHeaderText("您已登录");
                    alert.setContentText("您已登录，无需重复登录");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("警告");
                    alert.setHeaderText("账号或密码错误");
                    alert.setContentText("您的账号或密码错误，无法进行登录");
                    alert.showAndWait();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        SignRegisterButton.setOnAction(e -> {// 转跳注册界面
            ShowStage.setScene(RegisterScene);
        });
    }

    public static void setReplyMsg(Message rm) {
        replyMsg = rm;
    }

    public static void setReceiveMsgOrNot(boolean rm) {
        receiveMsgOrNot = rm;
    }


}

