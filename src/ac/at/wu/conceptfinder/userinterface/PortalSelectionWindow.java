package ac.at.wu.conceptfinder.userinterface;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;


/*
 * Displays 
 */
public class PortalSelectionWindow implements Initializable {
	
	public PortalSelectionWindow(){
		m_Callback = null;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//Allow multiple portals to be selected at once
		m_Portals.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		//Call the parent window back with the selection result
		m_LoadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if(m_Callback == null) return;
				m_Callback.portalResultsReady(m_Portals.getSelectionModel().getSelectedItems());
				closeWindow();
		    }
		});
	}
	
	/*
	 * Fill the listView with available portals
	 */
	public void fillPortals(List<String> portals){
		m_Portals.setItems(FXCollections.observableList(portals));
	}
	
	public void registerResultListener(CategorizerCallback callback){
		m_Callback = callback;
	}

	private void closeWindow(){
		Stage stage = (Stage) m_LoadBtn.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private ListView<String> m_Portals;
	
	@FXML
	private Button m_LoadBtn;
	
	private CategorizerCallback m_Callback;
}
