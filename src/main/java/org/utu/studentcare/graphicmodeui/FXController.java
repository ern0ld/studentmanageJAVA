package org.utu.studentcare.graphicmodeui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.db.SQLConnection;
import org.utu.studentcare.db.SQLiteConnection;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
//Hallinnoi allMenu.fxml-lomakkeen toiminnallisuuksia, eli lähinnä debug-tilassa tapahtuvia toimintoja
public class FXController extends Controller {
    @FXML
    Button startTeachBtn, assessBtn, evaluateBtn, initializeDbBtn, logOutBtn, StartTeachBtn2, startTeachBtn21;
    @FXML
    TextField userTxt, passwordTxt;
    @FXML
    public static ProgressBar eteneminen;
    static String userID;
    static String password;
    static ObjectProperty<TextField> loginForm = new SimpleObjectProperty<>();

    @FXML
    private void signIn(ActionEvent e) {
        if (!userTxt.getText().isEmpty() && !passwordTxt.getText().isEmpty()) {
            userID = userTxt.getText();
            password = passwordTxt.getText();
        }


    }

    public static String getUserID() {
        return userID;
    }

    public static String getPassword() {
        return password;
    }

    @FXML
    public void close(ActionEvent actionEvent) {
        Controller.setAppState(AppState.MainMenu);
        // userTxt.clear();

    }

    @FXML //tyhjentää login-lomakkeeseen syötetyt tiedot mikäli on jo kirjauduttu sisään
    public void initialize()  {
        DBApp.logState.addListener((a, b, c) -> {
            try {

                if (c != LoginState.NotLogged) {
                    userTxt.clear();
                    passwordTxt.clear();
                }
            } catch (NullPointerException npe) {

            }


        });


    }
@FXML
    public void loadStudentData(ActionEvent actionEvent) {
        if (DBApp.logState.getValue() == LoginState.NotLogged) {
            Controller.setAppState(AppState.Student);
            Controller.setAppState(AppState.login);
        } else {
            Controller.setAppState(AppState.Student);
        }
    }
    @FXML
    public void loadSecretaryData(ActionEvent actionEvent) {
        if (DBApp.logState.getValue() == LoginState.NotLogged) {
            Controller.setAppState(AppState.Secretary);
            Controller.setAppState(AppState.login);
        } else {
            Controller.setAppState(AppState.Secretary);
        }
    }

    @FXML
    public void loadTeacherData(ActionEvent actionEvent) {
        if (DBApp.logState.getValue() == LoginState.NotLogged) {
            Controller.setAppState(AppState.Teacher);
            Controller.setAppState(AppState.login);
        } else {
            Controller.setAppState(AppState.Teacher);
        }


    }
    //Allaolevissa käsitellään näppäimien tapahtumat ja luodaan tietokannasta haetuista tiedoista näkymä
    @FXML
    public void writeRegister(ActionEvent actionEvent) throws SQLException {
        dynamicTable.buildData(Controller.creates(), "coursegrades");
    }
    @FXML
    public void joinCourses(ActionEvent actionEvent) throws SQLException {
        dynamicTable.buildData(Controller.creates(), "courses");
    }
    @FXML
    public void coursesAttended(ActionEvent actionEvent) throws SQLException{
        dynamicTable.buildData(Controller.creates(), "courses");
    }
    @FXML
    public void startTeach(ActionEvent actionEvent) throws SQLException {
        //SQLConnection connection = SQLConnection.createConnection(false ? "slow" + dbPath : dbPath, true)) {
            dynamicTable.buildData(Controller.creates(), "courseinstances");

    }
    @FXML
    public void assess(ActionEvent actionEvent) throws SQLException {
        dynamicTable.buildData(Controller.creates(), "courses" );

    }
    public static SQLConnection create() throws SQLException {
        Path path = Paths.get("value4life.db");
        String dbPath = path.toString();

        boolean emptyDB = !Files.exists(path);

        SQLConnection connection = SQLConnection.createConnection(false ? "slow" + dbPath : dbPath, true);

        return connection;

    }
}
