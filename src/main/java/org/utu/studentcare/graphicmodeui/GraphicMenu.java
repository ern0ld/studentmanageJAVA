package org.utu.studentcare.graphicmodeui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.utu.studentcare.applogic.DBApp;

//Harjoitustyö 3:sta
public class GraphicMenu extends VBox {
    VBox vBox = new VBox();
    public GraphicMenu(ObjectProperty<AppState> appState, String[] labels, AppState... states) {
        addSpace();

        Platform.runLater(() -> {

                    System.out.println(DBApp.logState.getValue().toString());
                    DBApp.logState.addListener((a, b, c) -> {

                        for (int i = 0; i < labels.length; i++) {
                            StackPane node = generateMenuNode(labels[i]);
                            System.out.println(c.toString());
                            final AppState state = states[i];
                            if (c == LoginState.NotLogged) {

                                node.setVisible(false);
                            } else {
                                node.setVisible(true);
                                node.setOnMouseClicked(e -> Controller.setAppState((state)));
                                vBox.getChildren().add(node);

                            }
                        }
                    });




        addSpace();
        setSpacing(32);

        vBox.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));
        });
    }

    private void addSpace() {
        vBox.getChildren().add(new Separator(Orientation.VERTICAL) {{ setHeight(80); setOpacity(0); }});
    }

    private StackPane generateMenuNode(String text) {
        return new StackPane(
                new Rectangle(400, 50) {{ setFill(Color.AQUAMARINE); }},
                new Label(text) {{ setFont(new Font("Times New Roman", 32)); }}
        );
    }
}

