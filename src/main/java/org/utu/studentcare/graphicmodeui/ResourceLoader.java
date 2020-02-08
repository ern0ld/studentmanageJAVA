package org.utu.studentcare.graphicmodeui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

//Viikkoharjoitus 3:sta varastettu luokka, käsitellään lomakkeita
public class ResourceLoader<N extends Parent, C> {
    protected final N root;
    protected final C controller;

    ResourceLoader(String contentPath) {
        N root_ = null;
        C controller_ = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(contentPath));
            root_ = loader.load();
            controller_ = loader.getController();
            System.out.println("DEBUG: " + contentPath + " ladattu.");
        } catch (Exception e) {

            System.out.println("Syy: " + e.getMessage());
            System.exit(1);
        }
        root = root_;
        controller = controller_;
    }
}