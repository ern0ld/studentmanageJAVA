package org.utu.studentcare.graphicmodeui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.utu.studentcare.db.SQLConnection;
import org.utu.studentcare.db.SQLiteConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

//Controller-luokka, käsittelee ikkunoiden tilamuuttujia
public abstract class Controller  {

    public static ObjectProperty<AppState> appState = new SimpleObjectProperty<>(AppState.MainMenu);
    final DynamicTable dynamicTable = new DynamicTable();


    /*public void setAppState(ObjectProperty<AppState> appState) {
        this.appState = appState;
    }
*/
    public static ObjectProperty<AppState> getAppState(){
        return appState;
    }
//Asetetaan ikkunoiden tilamuuttujia
    public static void setAppState(AppState appState) {
        Platform.runLater(() -> {
        Controller.appState.set(appState);
    });}

    public void close() {
        appState.setValue(AppState.MainMenu);
    }

    public void closeProgram() {
        appState.setValue(AppState.Exit);
    }
    public void initialize()  {
    }

    public static SQLiteConnection creates() throws SQLException {
        Path path = Paths.get("value4life.db");
        String dbPath = path.toString();

        boolean emptyDB = !Files.exists(path);

        SQLConnection conn = SQLConnection.createConnection(false ? "slow" + dbPath : dbPath, true);

        return (SQLiteConnection)conn;

    }
}