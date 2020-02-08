package org.utu.studentcare.textmodeui;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.utu.studentcare.db.SQLConnection;
import org.utu.studentcare.graphicmodeui.AppState;

import java.util.function.Function;

/**
 * Liittyy tekstikäyttöliittymään.
 *
 * Oheisluokka, EI tarvitse ymmärtää, muokata eikä käyttää omassa koodissa työn tekemiseksi!
 * Voi silti olla tarpeen tekstiversion toiminnan käsittämiseksi
 */
public class Menu extends VBox {
    public String name;
    public SQLConnection.SQLFunction<MenuSession, Component> content;
    public String apiSignature;

    public Menu(String name, Component content) {
        this(name, "", s -> content, 0);
    }

    public Menu(String name, String apiSignature, SQLConnection.SQLFunction<MenuSession, Component> content, int i) {
        this.name = name;
        this.apiSignature = apiSignature;
        this.content = content;
    }

    public Menu(String name, String apiSignature, SQLConnection.SQLFunction<MenuSession, Function<DOM.ChainingComponentContainer, Component>> generator) {
        this(name, apiSignature, s -> generator.apply(s).apply(new DOM().new ChainingComponentContainer()), 0);
    }
    public Menu(ObjectProperty<AppState> appState, String[] labels, AppState... states) {
        addSpace();

        for(int i=0; i<labels.length; i++) {
            StackPane node = generateMenuNode(labels[i]);
            final AppState state = states[i];
            node.setOnMouseClicked(e -> appState.setValue(state));
            getChildren().add(node);
        }

        addSpace();
        setSpacing(32);

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void addSpace() {
        getChildren().add(new Separator(Orientation.VERTICAL) {{ setHeight(80); setOpacity(0); }});
    }

    private StackPane generateMenuNode(String text) {
        return new StackPane(
                new Rectangle(400, 50) {{ setFill(Color.BURLYWOOD); }},
                new Label(text) {{ setFont(new Font("Comic Sans Ms", 32)); }}
        );
    }
}