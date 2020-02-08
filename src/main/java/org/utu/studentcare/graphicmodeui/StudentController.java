package org.utu.studentcare.graphicmodeui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.db.SQLiteConnection;
import org.utu.studentcare.javafx.FXConsole;

import java.sql.SQLException;

public class StudentController extends Controller{

@FXML
    public void joinCourses(ActionEvent actionEvent) throws SQLException {
    //Valitaan kursseista vain ne joissa tunnisteena DTEK
        dynamicTable.buildData(Controller.creates(), "courseinstances WHERE instanceid LIKE '%DTEK%'");
    }

    @Override
    public void close() {
    //System.out.println("tässä kirjautumistila " + DBApp.logState.getValue().toString());
        Controller.appState.setValue(AppState.MainMenu);
    }
    @FXML
    public void coursesAttended(ActionEvent actionEvent) throws SQLException{
        //DynamicTable dynamicTable = new DynamicTable();
        //Valitaan kursseista käyttöliittymät
        super.dynamicTable.buildData(Controller.creates(), "courses where shortname = 'DTEK1049'");
    }
@FXML
    public void logOut(ActionEvent actionEvent) {
    DBApp.logState.setValue(LoginState.NotLogged);
    Controller.setAppState(AppState.MainMenu);


}
}
