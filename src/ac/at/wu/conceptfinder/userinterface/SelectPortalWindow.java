package ac.at.wu.conceptfinder.userinterface;


import java.util.List;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SelectPortalWindow {
	
	public List<String> selectPortals(List<String> possiblePortals){
		//Setup up a new blocking window
		final Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		BorderPane bP = new BorderPane();
		window.setTitle("Select Portals");
		
		final ListView<String> m_portalList = new ListView<String>();
		m_portalList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ObservableList<String> m_portalItems = FXCollections.observableArrayList(possiblePortals);
		m_portalList.setItems(m_portalItems);
		
		final Button selectBtn = new Button("Select");
		selectBtn.setOnAction(e -> {
			window.close();
		});

		VBox vbox = new VBox(m_portalList, selectBtn);
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll();
		
		bP.setCenter(vbox);
				
		Scene scene = new Scene(bP, 250, 400);
		
		window.setScene(scene);
		window.showAndWait();
		return m_portalList.getSelectionModel().getSelectedItems();
	}

		
}
