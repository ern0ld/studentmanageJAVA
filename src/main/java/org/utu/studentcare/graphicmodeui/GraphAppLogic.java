package org.utu.studentcare.graphicmodeui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.sqlite.core.DB;
import org.utu.studentcare.applogic.AppLogicException;
import org.utu.studentcare.applogic.DBApp;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
//Käsitellään ikkunoita
public class GraphAppLogic {

    private final Map<String, Window> windowMap = new HashMap<>();
    private String activeWindowName;
    private String contentPath;
    private Parent root;
    public GraphAppLogic(Window... windows)throws SQLException, AppLogicException, NullPointerException {

        for (Window window : windows){
            this.activeWindowName = window.getTitle();
            windowMap.put(activeWindowName, window);
            //System.out.println(activeWindowName);

            }
        if(DBApp.isDebugMode()) {
            init("/org/utu/studentcare/db/allMenu.fxml", "Debug", AppState.Debug);

        }
        else if(!DBApp.isDebugMode()) {
            init("/org/utu/studentcare/db/mainMenu.fxml", "Main", AppState.MainMenu);
        }
        else {
            init(windowMap.get(Controller.getAppState().getValue().toString()).getContentpath(), windowMap.get(Controller.getAppState().getValue().toString()).getTitle(), Controller.getAppState().getValue());

        }
        }






    public void init(String contentPath, String title, AppState appState) throws SQLException, AppLogicException {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(contentPath));

                root = loader.load();
               // Stage stage = new Stage();
               // stage.setTitle(title);
                //stage.setScene(new Scene(root));
                //stage.show();
                //Controller.appState.addListener((a, b, c) -> {
                  //  System.out.println("täälläkin tapahtuu jotain, toinen kuuntelija" + appState.toString());
                    //if (c == appState) stage.show();
                   // else stage.hide();

                /*new Stage() {{
                    System.out.println("Luodaan uusi stage");
                    Controller.appState.addListener((a, b, c) -> {
                        if (c == appState) show(); else hide();
                    });
                    setTitle(title);
                    setScene(new Scene(root));
                }};*/

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (NullPointerException npe) {
                System.out.println("Null");
            }


        } );
          /*a  stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("DEBUG: " + contentPath + " ladattu.");
        }
        catch (Exception e) {

        }*/
    }
    private Supplier<Integer> linkIndexer() {
        return new Supplier<>() {
            int i = 1;

            @Override
            public Integer get() {
                return i++;
            }
        };
    }

}


