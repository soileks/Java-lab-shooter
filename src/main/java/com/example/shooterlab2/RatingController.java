package com.example.shooterlab2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class RatingController {

    private static ArrayList<RatingRec> records;

    public static void setRecords(ArrayList<RatingRec> rds) {
        records = rds;
    }

    @FXML
    private TableView<RatingRec> tableView;

    public static void showRatingsScreen() throws IOException {
        Parent root = new FXMLLoader(mainClient.class.getResource("RatingTable.fxml")).load();
        Stage stage = new Stage();
        stage.setTitle("Рейтинговая таблица");
        stage.setScene(new Scene(root, 500, 500));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    private void initialize() {
        tableView.setPlaceholder(new Label("Ещё нет данных об игроках."));

        TableColumn<RatingRec, String> column1 =
                new TableColumn<>("Имя в игре");
        column1.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        TableColumn<RatingRec, String> column2 =
                new TableColumn<>("Число побед");
        column2.setCellValueFactory(
                new PropertyValueFactory<>("winsCount"));

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        tableView.getItems().addAll(records);
    }
}
