package org.utu.studentcare.graphicmodeui;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import org.utu.studentcare.applogic.DBApp;

import org.utu.studentcare.db.SQLiteConnection;


import java.sql.SQLException;

//Käsitellään teacherMenu-lomakkeen tapahtumia
public class TeacherController extends Controller  {
    //private final DynamicTable dynamicTable = new DynamicTable();
   @FXML
   Button startTeachBtn;
    ObjectProperty<Button> objectProperty = new SimpleObjectProperty<>();
    public void joinCourses(ActionEvent actionEvent) throws SQLException {

        dynamicTable.buildData(Controller.creates(), "courseinstances");
    }
@FXML
    public void startTeachAction(ActionEvent actionEvent) throws SQLException {
        dynamicTable.buildData(Controller.creates(), "courseinstances");



    }
    @FXML
    public void assess(ActionEvent actionEvent) throws SQLException {
        dynamicTable.buildData(Controller.creates(), "courses where shortname = 'DTEK1049' ");

    }
    @FXML
    public void close(ActionEvent actionEvent) {
        Controller.setAppState(AppState.MainMenu);

    }
    public void initialize(){

    }
@FXML
    public void signOut(ActionEvent actionEvent) {
        DBApp.logState.setValue(LoginState.NotLogged);
        Controller.setAppState(AppState.MainMenu);
    }
@FXML
    public void attending(ActionEvent actionEvent) throws SQLException {

        dynamicTable.buildData((SQLiteConnection)FXController.create(), "courses where shortname = 'DTEK1049' ");
    }
}

