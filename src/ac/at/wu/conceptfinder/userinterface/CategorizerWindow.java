package ac.at.wu.conceptfinder.userinterface;

import java.net.URL;
import java.util.ResourceBundle;

import ac.at.wu.conceptfinder.dataset.Dataset;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

public class CategorizerWindow implements Initializable {
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Dataset ds1 = new Dataset("test1");
		ds1.addCategory(BabelDomain.WARFARE_AND_DEFENSE);
		ds1.addKeyword("key1");
		ds1.addKeyword("key2");
		ds1.setPortal("testport");
		ds1.setTitle("Titletest");
				
		m_data.add(ds1);
		
		//Setting up the columns
		m_LinkColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
		   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
			       return new SimpleStringProperty(arg.getValue().ID().value());
		   }
		});
		m_PortalColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
				       return new SimpleStringProperty(arg.getValue().Portal());
			   }
		});
		m_TitleColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
				       return new SimpleStringProperty(arg.getValue().Title());
			   }
		});
		m_KeywordsColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
				       String keywords = "";
				       
				       for(String keyword : arg.getValue().Keywords()){
				    	   keywords += "\u25B8 " + keyword + System.getProperty("line.separator");
				       }
				       keywords = keywords.substring(0, keywords.length()-2);
				       
				       return new SimpleStringProperty(keywords);
			   }
		});
		m_CategoriesColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
			       String categories = "";
			       
			       for(BabelDomain category : arg.getValue().Categories()){
			    	   categories +=  "\u25B8 "  + category + System.getProperty("line.separator");
			       }
			       categories = categories.substring(0, categories.length()-2);
			       
			       return new SimpleStringProperty(categories);
			   }
		});

			
	
		//Ctrl + C copys dataset id to clipboard
		m_DatasetTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent t) {
		    	if(t.isControlDown() && t.getCode() == KeyCode.C){
			    	Dataset selectedDataset = m_DatasetTable.getSelectionModel().getSelectedItem();
			    	if(selectedDataset == null) return;

			        final Clipboard clipboard = Clipboard.getSystemClipboard();
			        final ClipboardContent content = new ClipboardContent();
		            content.putString(selectedDataset.ID().value());
			        clipboard.setContent(content);
			    }
		    }
		});

		
		//Populate table
		m_DatasetTable.setItems(m_data);
	}
	@FXML
	private BorderPane m_MainPane;

	@FXML
	private TableView<Dataset> m_DatasetTable;
	
	@FXML
	private TableColumn<Dataset, String> m_LinkColumn;
	
	@FXML
	private TableColumn<Dataset, String> m_PortalColumn;
	
	@FXML
	private TableColumn<Dataset, String> m_TitleColumn;
	
	@FXML
	private TableColumn<Dataset, String> m_KeywordsColumn;
	
	@FXML
	private TableColumn<Dataset, String> m_CategoriesColumn;
	
	@FXML
	private TableColumn<Dataset, String> m_ScoreColumn;

	private final ObservableList<Dataset> m_data = FXCollections.observableArrayList();
	
}
