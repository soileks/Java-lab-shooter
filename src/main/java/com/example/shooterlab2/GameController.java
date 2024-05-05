package com.example.shooterlab2;


import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController {
    @FXML
    private TextField inputPlayerName;
    @FXML
    private Button btnConnect;
    @FXML
    private Button btnReady;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnShoot;
    @FXML
    private Button btnRating;

    @FXML
    private Circle targetBigCircle;
    @FXML
    private Circle targetSmallCircle;
    @FXML
    private HBox arrow1;
    @FXML
    private HBox arrow2;
    @FXML
    private HBox arrow3;
    @FXML
    private HBox arrow4;
    @FXML
    private HBox playerTriangle1;
    @FXML
    private HBox playerTriangle2;
    @FXML
    private HBox playerTriangle3;
    @FXML
    private HBox playerTriangle4;

    private HBox[] playerTriangles;
    private HBox[] playerArrows;

    @FXML
    private Text labelGameInfo;
    @FXML
    private Text labelPause;

    public static final double GAME_AREA_WIDTH = 668;
    public static final double GAME_AREA_HEIGHT = 465;

    Gson gson = new Gson();
    Socket socketAtClient;
    InetAddress ip = null;

    DataInputStream dis;
    DataOutputStream dos;

    private Thread t;

    private boolean isThreadRunning = false;

    @FXML
    private void initialize() {
        playerTriangles = new HBox[]{playerTriangle1, playerTriangle2,
                playerTriangle3, playerTriangle4};
        playerArrows = new HBox[]{arrow1, arrow2, arrow3, arrow4};
    }

    public static void setArrowEndX(HBox arrow, double x) {
        arrow.setLayoutX(x - arrow.getBoundsInParent().getWidth());
    }

    public static void setArrowEndY(HBox arrow, double y) {
        arrow.setLayoutY(y - arrow.getBoundsInParent().getHeight() / 2);
    }

    public static void placePlayerTriangle(HBox playerTriangle, double y) {
        playerTriangle.setLayoutY(y - playerTriangle.getLayoutBounds().getHeight() / 2);
    }

    public static void showAlertInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщение");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonTypeOne = new ButtonType("Понятно");
        alert.getButtonTypes().setAll(buttonTypeOne);
        alert.show();
    }

    private void updateGame(Model model) {
        RatingController.setRecords(model.records);

        if (model.winnerName != null) {
            Platform.runLater(() -> showAlertInfo("Победитель: " + model.winnerName));
            return;
        }

        Platform.runLater(() -> {
            targetBigCircle.setLayoutY(model.bigTargetCenter.y);
            targetSmallCircle.setLayoutY(model.smallTargetCenter.y);

            int counter = 0;

            StringBuilder gameInfoText = new StringBuilder();

            for (PlayerData player : model.players) {
                if (player != null) {
                    playerArrows[counter].setVisible(player.isArrowFlying);
                    playerTriangles[counter].setVisible(true);
                    setArrowEndX(playerArrows[counter], player.arrowEndPos.x);
                    setArrowEndY(playerArrows[counter], player.arrowEndPos.y);
                    placePlayerTriangle(playerTriangles[counter], player.arrowEndPos.y);

                    int winnerScore = 0;
                    for (RatingRec record : model.records) {
                        if (record.name.equals(player.name)) {
                            winnerScore = record.winsCount;
                            break;
                        }
                    }

                    gameInfoText.append(player.name).append(":\n")
                            .append("Очков: ").append(player.score).append("\n")
                            .append("Выстрелов: ").append(player.shots).append("\n")
                            .append("Побед: ").append(winnerScore).append("\n\n");

                } else {
                    playerArrows[counter].setVisible(false);
                    playerTriangles[counter].setVisible(false);
                }
                counter++;
            }

            labelGameInfo.setText(gameInfoText.toString());
            labelPause.setVisible(model.isPaused);
        });
    }

    @FXML
    protected void onConnectClick() {
        if (t != null) return;

        if (inputPlayerName.getText().trim().isEmpty()) {
            showAlertInfo("Ник игрока не должен быть пустым.");
            return;
        }

        try {
            ip = InetAddress.getLocalHost();
            socketAtClient = new Socket(ip, mainServer.DEFAULT_SERVER_PORT);
            OutputStream os = socketAtClient.getOutputStream();
            dos = new DataOutputStream(os);
            InputStream is = socketAtClient.getInputStream();
            dis = new DataInputStream(is);

            t = new Thread(() -> {
                // Отправка серверу приветствия (для сообщения имени)
                try {
                    MSG hello = new MSG(MsgAction.CL_HELLO,
                            inputPlayerName.getText().trim());
                    dos.writeUTF(gson.toJson(hello));
                } catch (IOException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                }

                inputPlayerName.setDisable(true);
                btnConnect.setDisable(true);
                btnReady.setDisable(false);
                btnPause.setDisable(false);
                btnShoot.setDisable(false);
                btnRating.setDisable(false);

                isThreadRunning = true;
                // Бесконечное получение сообщений
                // (текущего состояния модели) от сервера
                // и обновление UI
                while (isThreadRunning) {
                    String s;
                    try {
                        s = dis.readUTF();
                        ServerMsg msg = gson.fromJson(s, ServerMsg.class);

                        // Обновление положений мишеней и стрел, а также
                        // информации об игре (счёта, числа выстрелов)
                        updateGame(msg.model);
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                    }
                }
            });
            t.start();
        } catch (IOException e) {
            showAlertInfo("Не удалось подключиться.\nУбедитесь, что сервер запущен, и попробуйте снова.");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    protected void onReadyClick() {
        if (dos != null) {
            MSG msg = new MSG(MsgAction.CL_READY);
            try {
                dos.writeUTF(gson.toJson(msg, MSG.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onPauseClick() {
        if (dos != null) {
            MSG msg = new MSG(MsgAction.CL_PAUSE);
            try {
                dos.writeUTF(gson.toJson(msg, MSG.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onShootClick() {
        if (dos != null) {
            MSG msg = new MSG(MsgAction.CL_SHOOT);
            try {
                dos.writeUTF(gson.toJson(msg, MSG.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onShowRatingClick() throws IOException {
        if (dos != null) {
            RatingController.showRatingsScreen();
        }
    }

    public void shutDown() {
        isThreadRunning = false;
        if (t != null)
            t.interrupt();
    }
}
