package com.example.shooterlab2;
import java.util.ArrayList;

import static java.lang.Math.pow;

public class Model {
    public static class Builder {
        private static final Model model = new Model(new MyDB());

        public static Model build() {
            return model;
        }
    }

    public static final int PLAYERS_MAX = 4;
    public static final int SCORE_LIMIT = 10;
    public static final int RADIUS_1 = 36;
    public static final int RADIUS_2 = 18;
    public static final int ARROW_SPEED = 8;
    public static final int ARROW_X_START = 150;

    // Координаты мишеней
    public final Point bigTargetCenter = new Point(501, GameController.GAME_AREA_HEIGHT / 2);
    public final Point smallTargetCenter = new Point(616, GameController.GAME_AREA_HEIGHT / 2);

    // Информация о каждом из игроков (включая координаты их стрел и состояния)
    public int currentPlayersCount = 0;
    public boolean isPaused = true;
    public final PlayerData[] players = new PlayerData[PLAYERS_MAX];

    public String winnerName = null;

    double speedBig = 1.5, speedSmall = -(speedBig * 2);

    // Список наблюдателей за изменениями данных модели
    private final transient ArrayList<IObserver> observers = new ArrayList<>();

    // Класс для работы с базой данных
    private transient MyDB db;

    public ArrayList<RatingRec> records;

    public Model() {
    }

    public Model(MyDB db) {
        this.db = db;
        records = db.getRatingsTable();
    }

    public void addObserver(IObserver o) {
        observers.add(o);
    }

    // Метод, уведомляющий наблюдателей об изменениях в модели
    private void notifyObserversAboutUpdate() {
        for (IObserver o : observers) {
            o.refresh();
        }
    }

    public synchronized int regNewPlayer(String name) {
        if (currentPlayersCount == PLAYERS_MAX || !isPaused) {
            return -1;
        }

        for (int i = 0; i < PLAYERS_MAX; i++) {
            if (players[i] == null) {
                players[i] = new PlayerData(name, 0, 0);
                currentPlayersCount++;
                resetGame(false);
                notifyObserversAboutUpdate();
                return i;
            }
        }
        return -1;
    }

    public synchronized void setPlayerReady(int playerId, boolean isReady) {
        players[playerId].isReady = isReady;

        for (int i = 0; i < PLAYERS_MAX; i++) {
            if (players[i] != null && !players[i].isReady) {
                isPaused = true;
                notifyObserversAboutUpdate();
                return;
            }
        }
        isPaused = false;
        notifyObserversAboutUpdate();
    }

    private synchronized void resetGame(boolean clearStatAndReady) {
        winnerName = null;

        if (clearStatAndReady) {
            isPaused = true;
        }

        //Восстановление скоростей
        speedBig = 1.5;
        speedSmall = -(speedBig * 2);

        // Восстановление положения мишеней
        bigTargetCenter.y = GameController.GAME_AREA_HEIGHT / 2;
        smallTargetCenter.y = GameController.GAME_AREA_HEIGHT / 2;

        // Пересчёт положений стрел
        ArrayList<PlayerData> nonNullPlayers = new ArrayList<>();
        for (int j = 0; j < PLAYERS_MAX; j++)
            if (players[j] != null) {
                players[j].isArrowFlying = false;
                players[j].arrowEndPos.x = ARROW_X_START;
                if (clearStatAndReady) {
                    players[j].isReady = false;
                    players[j].score = 0;
                    players[j].shots = 0;
                }
                nonNullPlayers.add(players[j]);
            }
        // смещение стрел (если их несколько)
        double dist = 100;
        double h = (nonNullPlayers.size() - 1) * dist;
        for (int j = 0; j < nonNullPlayers.size(); j++)
            nonNullPlayers.get(j).arrowEndPos.y = j * dist + (GameController.GAME_AREA_HEIGHT - h) / 2;
    }

    private void updateWinnerInfoInDB(String winnerName) {
        int idx = -1;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).name.equals(winnerName)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            db.insertOrUpdateRec(new RatingRec(winnerName, 1));
        } else {
            db.insertOrUpdateRec(new RatingRec(winnerName, records.get(idx).winsCount + 1));
        }
        records = db.getRatingsTable();
    }

    public void shoot(int playerId) {
        if (!isPaused && !players[playerId].isArrowFlying) {
            players[playerId].isArrowFlying = true;
            players[playerId].shots += 1;
            notifyObserversAboutUpdate();
        }
    }

    public boolean gameUpdate() {
        // Передвижение мишеней
        bigTargetCenter.y += speedBig;
        smallTargetCenter.y += speedSmall;

        double bigX = bigTargetCenter.x;
        double bigY = bigTargetCenter.y;
        double smallX = smallTargetCenter.x;
        double smallY = smallTargetCenter.y;

        // Логика изменения направления движения стрелы при достижении границ поля
        if (bigY <= Model.RADIUS_1
                || bigY >= GameController.GAME_AREA_HEIGHT - Model.RADIUS_1) {
            speedBig *= (-1);
        }
        if (smallY <= Model.RADIUS_2
                || smallY >= GameController.GAME_AREA_HEIGHT - Model.RADIUS_2) {
            speedSmall *= (-1);
        }

        for (PlayerData player : players) {
            if (player != null) {
                if (player.score >= SCORE_LIMIT) {
                    synchronized (this) {
                        winnerName = player.name;
                        updateWinnerInfoInDB(winnerName);
                        notifyObserversAboutUpdate();
                        resetGame(true);
                        notifyObserversAboutUpdate();
                    }
                    return false;
                }

                if (player.isArrowFlying) {
                    player.setArrowEndPos(
                            player.arrowEndPos.x + ARROW_SPEED,
                            player.arrowEndPos.y);

                    double arrowEndX = player.arrowEndPos.x;
                    double arrowEndY = player.arrowEndPos.y;

                    // Проверка на попадание стрелы в мишень 1
                    if (pow((arrowEndX - bigX), 2) + pow((arrowEndY - bigY), 2)
                            <= pow(Model.RADIUS_1, 2)) {
                        player.isArrowFlying = false;
                        player.arrowEndPos.x = ARROW_X_START;
                        player.score += 1;
                    }

                    // Проверка на попадание стрелы в мишень 2
                    else if (pow((arrowEndX - smallX), 2) + pow((arrowEndY - smallY), 2)
                            <= pow(Model.RADIUS_2, 2)) {
                        player.isArrowFlying = false;
                        player.arrowEndPos.x = ARROW_X_START;
                        player.score += 2;
                    }

                    // Проверка на пролёт мимо
                    else if (arrowEndX >= GameController.GAME_AREA_WIDTH) {
                        player.isArrowFlying = false;
                        player.arrowEndPos.x = ARROW_X_START;
                    }
                }
            }
        }

        notifyObserversAboutUpdate();
        return true;
    }
}
