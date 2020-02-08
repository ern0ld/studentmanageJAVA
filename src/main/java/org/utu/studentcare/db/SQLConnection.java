package org.utu.studentcare.db;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.graphicmodeui.AppState;
import org.utu.studentcare.graphicmodeui.Controller;
import org.utu.studentcare.graphicmodeui.DynamicTable;
import org.utu.studentcare.graphicmodeui.FXController;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * SQL interface for all SQL backends.
 *
 * TODO: Oheisluokka, ei tarvitse muokata, mutta pitää ymmärtää ja käyttää omassa koodissa kuten esimerkissäkin!
 */
public interface SQLConnection extends AutoCloseable {
    // suora yhteys SQL-backendille
    //Connection connection();

    /** Suorittaa vapaamuotoisen SQL-käskyn */
    boolean performCustomStatement(String statement) throws SQLException, AppLogicException;

    /** Asettaa tietokannan viivästettyyn commit-tilaan jos paljon dataa kirjattavana */
    void setDelayedCommit(boolean status) throws SQLException;

    /** Etsii ensimmäisen datan esiintymän tietokannassa annetuilla ehdoilla */
    <T> Optional<T> findFirst(SQLFunction<ResultSet, T> mapping, String statement, Object... data) throws SQLException, AppLogicException;

    /** Etsii kaikki datan esiintymät tietokannassa annetuilla ehdoilla */
    <T> List<T> findAll(SQLFunction<ResultSet, T> mapping, String statement, Object... data) throws SQLException, AppLogicException;

    /** Lisää dataa tietokantaan */
    int insert(String statement, Object... data) throws SQLException, AppLogicException;

    /** Päivittää datan tietokannassa */
    int update(String statement, Object... data) throws SQLException, AppLogicException;

    /** Laskee datan rivien määrän tietokannassa */
    int count(String statement, Object... data) throws SQLException, AppLogicException;

    /** Poistaa dataa tietokannasta */
    int delete(String statement, Object... data) throws SQLException, AppLogicException;

    /** Palauttaa nykyhetken päiväyksen SQL-moottorin mukaan */
    String now() throws SQLException, AppLogicException;

    @FunctionalInterface
    interface SQLFunction<T, R> {
        R apply(T t) throws SQLException, AppLogicException;
    }

    /**
     *
     * @throws SQLException
     */

    /**
     * Luo SQL-yhteyden.
     *
     * @param path tietostopolku (liitä alkuun "slow" jos haluat tahallista hidastusta kyselyihin)
     * @param debugMode tulostetaanko konsoliin kyselyiden teksti
     * @param delay hidastuksen määrä (ms)
     * @return Yhteysolio
     * @throws SQLException
     */
    static SQLConnection createConnection(String path, boolean debugMode, int delay) throws SQLException {
        if ("dummy".equals(path)) {
            return new DummyConnection();
        }
        if (path.startsWith("slow")) {
            return new SQLiteConnection(path.substring(4), debugMode) {
                @Override
                protected PreparedStatement prepare(String type, String statement, Object... data) throws SQLException, AppLogicException {
                    try {
                        if (Controller.appState.getValue() == AppState.Debug) {
                            progress(delay);
                        }
                        if (!type.equals("insert")) Thread.sleep(delay);
                        Controller.appState.addListener((a,b,c) -> {
                            try {
                                if (c == AppState.MainMenu || c == AppState.login) Thread.sleep(10);
                                 else progress(delay);
                            }
                            catch (InterruptedException ie) {
                            }
                      });




                    } catch (InterruptedException e) {
                    }
                    return super.prepare(type, statement, data);
                }
            };}
        else
            //SQLiteConnection sqLiteConnection = new SQLiteConnection(path, debugMode);

            return new SQLiteConnection(path, debugMode);

    }

    /**
     * Luo SQL-yhteyden.
     *
     * @param path tietostopolku (liitä alkuun "slow" jos haluat tahallista hidastusta kyselyihin 400ms)
     * @param debugMode tulostetaanko konsoliin kyselyiden teksti
     * @return Yhteysolio
     * @throws SQLException
     */
    static SQLConnection createConnection(String path, boolean debugMode) throws SQLException {
        return createConnection(path, debugMode, 400);
    }


static void progress(int delay){
    Platform.runLater(() -> {
        Task<Integer> task = new Task<Integer>() {
            @Override
            public Integer call() {
                int i = 0;
                for (i = 0; i < delay; i++) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        break;
                    }
                    updateProgress(i + 1, 10);
                }

                return i;
            }

        };
        ProgressBar updProg = new ProgressBar();
        updProg.progressProperty().bind(task.progressProperty());

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        Stage stage = new Stage();
        StackPane layout = new StackPane();
        layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 100;");
        layout.getChildren().add(updProg);

        stage.setScene(new Scene(layout));
        stage.setTitle("Käsitellään tietokantaa");
        stage.show();
        task.setOnSucceeded(e -> stage.hide());


    });
}
}