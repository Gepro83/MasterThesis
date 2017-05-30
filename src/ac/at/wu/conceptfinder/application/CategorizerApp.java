package ac.at.wu.conceptfinder.application;


import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.BabelConceptCreator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CategorizerApp extends Application {
	private static Database m_database;
	
    public static void main(String[] args) throws StorageException {
    	m_database = new Database("jdbc:postgresql://localhost:5432/Categorizer Eval", "postgres", "postgrespass", new BabelConceptCreator());
        launch(args);
     }

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("GUI.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root, 1350, 900);
    
        primaryStage.setTitle("Categorizer");
        primaryStage.setScene(scene);
        
        
        CategorizerWindow window = (CategorizerWindow) fxmlLoader.getController();

      
        window.setDatabase(m_database);
        
        primaryStage.show();
	}
	

}
