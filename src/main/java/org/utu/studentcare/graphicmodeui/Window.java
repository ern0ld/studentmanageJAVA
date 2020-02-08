package org.utu.studentcare.graphicmodeui;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.utu.studentcare.applogic.DBApp;
import org.utu.studentcare.db.SQLConnection;
import javafx.util.*;

//Myös harjoitustyö 3:sta varastettu, luodaan ikkunat lomakedatan perusteella ja asetetaan niille tilamuuttuja
public class Window<C extends Controller> extends ResourceLoader<Parent, C>  {
    public  String title;
    public String uusi;

    ObjectProperty<AppState> objectAppState;


    public Window(String title, String[] contents, int i) {
       super("");
        this.title = title;

    }
    public Window(String title, String contentPath, AppState activeStateForThis,ObjectProperty<AppState> appState ) {
        super(contentPath);
        uusi = contentPath;
        this.title = title;
        new Stage() {{
            //Kuunnellaan ikkunoiden tilamuuttujaa ja näytetään uusi ikkuna mikäli tarve vaattii
            Controller.appState.addListener((a, b, c) -> {
                if (c == AppState.Exit) System.exit(1);
                if (DBApp.isDebugMode() && c == AppState.login) {
                    System.out.println("Debugmode päällä");
                    Controller.setAppState(AppState.Debug);
                }
                //Mikäli kirjautuneena on opiskelija, ei päästetä häntä Hallinnon tai opettajan näkymiin
                if (DBApp.logState.getValue() == LoginState.StudenLogged && c == AppState.Teacher || DBApp.logState.getValue() == LoginState.StudenLogged && c == AppState.Secretary) {
                    //System.out.println("Sinulla ei ole oikeuksia");
                    Controller.setAppState(AppState.MainMenu);

                } else if (c == activeStateForThis) show();
                else hide();
            });
            setTitle(title);
            setScene(new Scene(root));
        }};
        Controller.setAppState(activeStateForThis);
    }
    public String getContentpath(){
        return uusi;

    }
    public String getTitle(){
        return title;
    }
}