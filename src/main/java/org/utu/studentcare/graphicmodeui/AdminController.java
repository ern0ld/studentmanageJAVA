package org.utu.studentcare.graphicmodeui;

import javafx.fxml.FXML;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.db.DBCleaner;

import javafx.event.ActionEvent;
import org.utu.studentcare.db.SQLiteConnection;

import java.sql.SQLException;
//Hallinnoi adminmenu-lomakkeen toimintoja
public class AdminController extends Controller {


    @FXML
    public void cleanDB(ActionEvent actionEvent) throws AppLogicException, SQLException {
        new DBCleaner(FXController.create()).wipeTables().populateTables();

    }
    @FXML
    public void close(ActionEvent actionEvent) {
        Controller.setAppState(AppState.MainMenu);
    }

    @FXML //Kirjautuu ulos ja asettaa logstateksi notlogged, sulkee ikkunan
    public void logOut(ActionEvent actionEvent) {
        DBApp.logState.setValue(LoginState.NotLogged);
        close(actionEvent);
    }
 //Haetaan tietokannasta ja luodaan näkymä tuloksista
    public void writeRegister(ActionEvent actionEvent) throws SQLException {
        //Valitaan kaikki arvioidut kurssit
        dynamicTable.buildData(Controller.creates(), "coursegrades");
    }
    //Haetaan tietokannasta ja luodaan näkymä tuloksista
    public void joinCourses(ActionEvent actionEvent) throws SQLException {
        //Valitaan kaikki kurssit
        dynamicTable.buildData(Controller.creates(), "courses");
    }
    //Haetaan tietokannasta ja luodaan näkymä tuloksista
    public void attending(ActionEvent actionEvent) throws SQLException{
        //Valitaan kursseista käyttöliittymät
        dynamicTable.buildData(Controller.creates(), "courses where shortname = 'DTEK1049'");
    }
}
