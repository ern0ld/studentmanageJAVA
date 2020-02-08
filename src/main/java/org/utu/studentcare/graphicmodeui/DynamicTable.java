package org.utu.studentcare.graphicmodeui;
import java.sql.Connection;
import java.sql.ResultSet;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.utu.studentcare.db.SQLConnection;
import org.utu.studentcare.db.*;
import org.utu.studentcare.db.orm.Teacher;

public class DynamicTable {

    private ObservableList<ObservableList> data;
    private TableView tableview;
    SQLiteConnection connection;
    String statement;

//Luodaan tableview-näkymä SQLiteconnection ja hakulausekkeen perusteella
    //Tähän olisi voinut liittää vielä toiminnallisuuden käyttäjän tableviewistä valitsemille soluille,
// mutta alkoi ideat ja aika loppua kesken työtä tehdessä
    public void buildData(SQLiteConnection connection, String statement){
        tableview = new TableView();
        this.connection = connection ;
        System.out.println(connection.toString());
        data = FXCollections.observableArrayList();
        try{
            SQLiteConnection c = connection;


            //ResultSet johon tallennetaan hakutulos
            ResultSet rs = c.find("select * from " + statement);


            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                final int j = i;
                //Käydään hakutulos läpi kolumni kerrallaan
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableview.getColumns().addAll(col);
            }

           //Lisätään data observablelistiin
            while(rs.next()){
                //Käydään rivit läpi
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    //Käydään kolumnit läpi
                    row.add(rs.getString(i));
                }
                data.add(row);

            }

            //Lisätään observablelist tableviewiin
            tableview.setItems(data);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
        Platform.runLater(()-> {
            //luodaan uusi ikkuna
        Scene scene = new Scene(tableview,600,600);
        Stage stage = new Stage();
        stage.setTitle(statement.toUpperCase());

        stage.setScene(scene);
        stage.show();
        });
    }


}