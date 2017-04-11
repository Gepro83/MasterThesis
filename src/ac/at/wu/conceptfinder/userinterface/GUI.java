package ac.at.wu.conceptfinder.userinterface;

import java.util.ArrayList;
import java.util.HashSet;

import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.BabelConceptCreator;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class GUI extends Application {
	private static Database m_database;
	
    public static void main(String[] args) throws StorageException {
    	m_database = new Database("jdbc:postgresql://localhost:5432/Test", "Georg", "georg", new BabelConceptCreator());
        launch(args);
     }

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("GUI.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root, 1024, 768);
    
        primaryStage.setTitle("FXML Welcome");
        primaryStage.setScene(scene);
        
        
        CategorizerWindow window = (CategorizerWindow) fxmlLoader.getController();

      
        window.setDatabase(m_database);
        
        primaryStage.show();
	}
	

}
