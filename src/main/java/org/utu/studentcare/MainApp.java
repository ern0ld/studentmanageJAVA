package org.utu.studentcare;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.graphicmodeui.*;
import org.utu.studentcare.javafx.FXConsole;
import org.utu.studentcare.graphicmodeui.AppState;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class MainApp extends Application {
    ObjectProperty<AppState> appState = new SimpleObjectProperty<>();


    Window<Controller> debugWindow = new Window<>(
            "Debug",
            "/org/utu/studentcare/db/allMenu.fxml",
            AppState.Debug, Controller.appState);
    Window<Controller> loginWindow = new Window<>(
            "Login",
            "/org/utu/studentcare/db/login.fxml",
            AppState.login, Controller.appState);
    Window<TeacherController> teacherWindow = new Window<>(
            "Teacher",
            "/org/utu/studentcare/db/teacherMenu.fxml",
            AppState.Teacher, Controller.appState);
    Window<AdminController> adminWindow = new Window<>(
            "Secretary",
            "/org/utu/studentcare/db/adminMenu.fxml",
            AppState.Secretary, Controller.appState);
    Window<FXController> studentWindow = new Window<>(
            "Student",
            "/org/utu/studentcare/db/student.fxml",
            AppState.Student, Controller.appState);
    Window<Controller> menuWindow = new Window<>(
            "Main",
            "/org/utu/studentcare/db/mainMenu.fxml",
            AppState.MainMenu, Controller.appState);
    /*Window<Controller> slowWindow = new Window<>(
            "SlowMode",
            "/org/utu/studentcare/db/progress.fxml",
            AppState.Busy, Controller.appState);*/


    // The JavaFX runtime does the following, in order, whenever an application is launched:

    // 1. Starts the JavaFX runtime, if not already started (see Platform.startup(Runnable) for more information)
    // 2. Constructs an instance of the specified Application class
    // 3. Calls the init() method
    // 4. Calls the start(javafx.stage.Stage) method
    // 5. Waits for the application to finish, which happens when either of the following occur:
    //   a) the application calls Platform.exit()
    //   b) the last window has been closed and the implicitExit attribute on Platform is true
    // 6. Calls the stop() method

    @Override
    public void init() { /* ei toiminnallisuutta tässä */ }

    @Override
    public void stop() { /* ei toiminnallisuutta tässä */ }

    /**
     * Creates a new text mode user interface for the application.
     * @param stage Main window
     *
     * TODO: tämä kaikki halutaan korvata graafisella käyttöliittymällä!
     */
    private void startTextModeUI(Stage stage) throws AppLogicException, SQLException {
        FXConsole console = new FXConsole(true, true, new TextArea());

        stage.setWidth(1200);
        stage.setHeight(700);
        stage.show();
        stage.setScene(new Scene(console));

        DBApp.init("value4life.db", s -> { Platform.runLater(stage::close); console.close(); s.accept(null); Platform.exit(); }, console.commands);

    }

    /**
     * Creates a new graphical user interface for the application.
     * @param stage Main window
     */
    private void startGraphicalUI(Stage stage) throws Exception{
        // TODO: toteuta!

        GraphAppLogic graphAppLogic = new GraphAppLogic(teacherWindow, adminWindow,debugWindow, studentWindow,loginWindow, menuWindow);
            FXConsole console = new FXConsole(true, true, new TextArea());

            try {

                DBApp.init("value4life.db", s -> {
                    Platform.runLater(stage::close);
                    console.close();
                    s.accept(null);
                    Platform.exit();
                }, console.commands);
            } catch (AppLogicException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            catch (NullPointerException npe) {
                System.out.println("Nullpointer");
            }

    }




    @Override
    public void start(Stage stage) throws Exception {
        // TODO: vaihda kun teet graafista käyttöliittymää
        boolean textMode =  false;

        if (textMode)
            startTextModeUI(stage);
        else{
            startGraphicalUI(stage);
            //FXMainController.currentStage(stage);

    }



}
    private Parent niceMainWindow(String app) {
        return new BorderPane(new GraphicMenu(Controller.appState,
                new String[]{"Opettaja", "Hallinto", "Opiskelija", "Lopetus"},
                AppState.Teacher,
                AppState.Secretary,
                AppState.Student,
                AppState.MainMenu,
                AppState.login,
                AppState.Debug,
                AppState.Exit
        )) {{
        }};
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
