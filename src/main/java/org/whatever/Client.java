package org.whatever;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class Client extends Application {
    private final Properties properties = new Properties();
    private TableView resultsBox;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
        properties.load(inputStream);

        Pane objectsBox = addObjectsBox();
        Pane connectionsBox = addConnectionsBox();
        Pane queryBox = addQueryBox();
        resultsBox = addResultsBox();

        VBox leftSide = new VBox(10, objectsBox, connectionsBox);
        leftSide.setAlignment(Pos.TOP_LEFT);
        VBox rightSide = new VBox(10, queryBox, resultsBox);
        rightSide.setAlignment(Pos.TOP_RIGHT);

        HBox root = new HBox(10, leftSide, rightSide);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 600, 600);

        primaryStage.setTitle(properties.get("application.name").toString());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Pane addObjectsBox() {
        VBox vBox = new VBox();

        Text heading = new Text("Connections");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        vBox.getChildren().add(heading);

        ListView<String> list = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList (
                "table1", "table2", "table3");
        list.setItems(items);
        vBox.getChildren().add(list);

        return vBox;
    }

    public Pane addConnectionsBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #cccccc;");

        Button buttonCurrent = new Button("Add Connection");
        buttonCurrent.setPrefSize(100, 20);

        hbox.getChildren().addAll(buttonCurrent);

        return hbox;
    }

    public Pane addQueryBox() {
        VBox vBox = new VBox();
        TextArea textArea = new TextArea("enter sql here");

        textArea.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                executeQuery(textArea.getText());
            }
        });

        Button executeButton = new Button("Execute");
        executeButton.setPrefSize(100, 20);

        executeButton.setOnAction(event -> {
            executeQuery(textArea.getText());
        });

        vBox.getChildren().addAll(textArea, executeButton);
        return vBox;
    }

    public TableView addResultsBox() {
        TableView<Result> table = new TableView<>();
        table.setEditable(false); // ?
        // FIXME iterate over me using jdbc db result metadata
        TableColumn col1 = new TableColumn<>("col1");
        TableColumn col2 = new TableColumn<>("col2");
        TableColumn col3 = new TableColumn<>("col3");
        col1.setMinWidth(100);
        col2.setMinWidth(100);
        col3.setMinWidth(100);

        col1.setCellValueFactory(new PropertyValueFactory<>("col1"));
        col2.setCellValueFactory(new PropertyValueFactory<>("col2"));
        col3.setCellValueFactory(new PropertyValueFactory<>("col3"));
        table.getColumns().addAll(col1, col2, col3);

        ObservableList<Result> resultsData =
                FXCollections.observableArrayList(
                        Result.builder().col1("1").col2("2").col3("3").build(),
                        Result.builder().col1("x").col2("y").col3("z").build()
                );

        table.setItems(resultsData);
        return table;
    }

    private void executeQuery(String value) {
        try {
            connectAndExecuteQuery(properties.get("connectionUrl"), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void connectAndExecuteQuery(Object url, String sql) throws Exception {
        Connection connection = DriverManager.getConnection(url.toString());

        ArrayList<Result> results = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1 ; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            System.out.println("col name=" + columnName);
        }

        while(resultSet.next()) {
            results.add(Result.builder()
                    .col1(getColumn(resultSet, 1))
                    .col2(getColumn(resultSet, 2))
                    .col3(getColumn(resultSet, 3))
                    .build());
        }

        ObservableList<Result> resultsData = FXCollections.observableArrayList(results);
        resultsBox.setItems(resultsData);
        connection.close();
    }

    private Object getColumn(ResultSet resultSet, int index) throws Exception {
        return resultSet.getObject(index);
    }
}
