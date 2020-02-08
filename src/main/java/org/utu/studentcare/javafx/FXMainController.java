package org.utu.studentcare.javafx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.db.orm.Student;
import org.utu.studentcare.db.orm.Teacher;
import org.utu.studentcare.javafx.FXConsole;

import java.sql.SQLException;

public class FXMainController {
    Stage stage;
    @FXML
    private Button teacherBtn;

    public static void currentStage(Stage stage){
        stage = stage;
    }
    @FXML
    private void loadTeacherData(ActionEvent event) throws AppLogicException, SQLException {
        FXConsole console = new FXConsole(true, true, new TextArea());

        DBApp.init("value4life.db", s -> {

            console.close();
            s.accept(null);
            Platform.exit();
        }, console.commands);
    }
}
