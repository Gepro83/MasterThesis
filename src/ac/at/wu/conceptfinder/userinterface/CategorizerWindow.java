package ac.at.wu.conceptfinder.userinterface;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class CategorizerWindow implements Initializable, ResultCallback {

	//Table cells for concepts are bold if they represent a concept found in the keywords
	//Also if relevance score and coherence score are exactly 0 the the text color
	//is set to blue to indicate that this concept was found by the MCS heuristic
	private final class ConceptTableCell extends TableCell<Concept, String>{
			   
		@Override
		protected void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty); 
	        this.setFont(Font.getDefault());
	        this.setTextFill(Color.BLACK);
	        
	        Concept concept = (Concept) this.getTableRow().getItem();
	        if(concept != null){
	        	if(concept.Mark().startsWith("1")){
	        		Font stdFont = Font.getDefault();
	    	        this.setFont(Font.font(stdFont.getFamily(), FontWeight.BOLD, stdFont.getSize()));	        		
	        	}
	        	if(concept.Scores().RelevanceScore() == 0 && concept.Scores().CoherenceScore() == 0)
	        		this.setTextFill(Color.BLUE);
	        }
	        this.setText(item);
	   }
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	
		//Set layout for UI
		m_DatasetTable.prefWidthProperty().bind(m_MainPane.widthProperty().divide(2));
		m_ConceptTable.prefWidthProperty().bind(m_MainPane.widthProperty().divide(2));
		m_PortalList.prefWidthProperty().bind(m_MainPane.widthProperty().divide(4));
		m_PortalList.prefHeightProperty().bind(m_MainPane.heightProperty().divide(5));
		
		//Setting up the columns for the Dataset table
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
				   return new ReadOnlyStringWrapper(arg.getValue().Title());
			   }
		});
		m_TitleColumn.setCellFactory(new Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>>() {
			   public TableCell<Dataset, String> call(TableColumn<Dataset, String> arg) {
				   TableCell<Dataset, String> cell = new TableCell<>();
		           Text text = new Text();
		           text.setStyle("-fx-fill: -fx-text-background-color;");
		           cell.setGraphic(text);
		           cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
		           text.wrappingWidthProperty().bind(cell.widthProperty());
		           text.textProperty().bind(cell.itemProperty());
		           return cell ;
			   }
		});
		m_KeywordsColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
				       String keywords = "";
				       
				       for(String keyword : arg.getValue().Keywords()){
				    	   keywords += "\u25B8 " + keyword + System.getProperty("line.separator");
				       }
				       if(keywords != "") keywords = keywords.substring(0, keywords.length()-2);
				       
				       return new SimpleStringProperty(keywords);
			   }
		});
		m_CategoriesColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
			       String categories = "";
			       
			       for(BabelDomain category : arg.getValue().Categories().keySet()){
			    	   categories +=  "\u25B8 "  + category + System.getProperty("line.separator");
			       }
			       if(categories != "") categories = categories.substring(0, categories.length()-2);
			       
			       return new SimpleStringProperty(categories);
			   }
		});
		m_ScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
			       String categories = "";
			       
			       for(BabelDomain category : arg.getValue().Categories().keySet()){
			    	   categories +=  "\u25B8 "  + arg.getValue().Categories().get(category) + System.getProperty("line.separator");
			       }
			       if(categories != "") categories = categories.substring(0, categories.length()-2);
			       
			       return new SimpleStringProperty(categories);
			   }
		});
		
		//Setting up the columns for the Concept table
		m_ConceptNameColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
				   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
					       return new SimpleStringProperty(arg.getValue().Name());
				   }
		});
		m_ConceptNameColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell();
			   }
		});
		m_ConceptCatColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   return new SimpleStringProperty(arg.getValue().Category().toString());
			   }
		});
		m_ConceptCatColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell();
			   }
		});
		m_ConceptCatConfColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   return new SimpleStringProperty(String.valueOf(arg.getValue().CatConfidence()));
			   }
		});
		m_ConceptCatConfColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell();
			   }
		});
		m_ConceptRelScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   return new SimpleStringProperty(String.valueOf(arg.getValue().Scores().RelevanceScore()));
			   }
		});
		m_ConceptRelScoreColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell();
			   }
		});
		m_ConceptCohScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   return new SimpleStringProperty(String.valueOf(arg.getValue().Scores().CoherenceScore()));
			   }
		});
		m_ConceptCohScoreColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell();
			   }
		});

		
		//Ctrl + C in Dataset table copies dataset id to clipboard
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
		
		m_DatasetTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Dataset>() {
		    @Override
		    public void changed(ObservableValue<? extends Dataset> observableValue, Dataset oldValue, Dataset newValue) {
		        //Check whether item is selected
		        if(m_DatasetTable.getSelectionModel().getSelectedItem() != null){
		        	//Update the Concept table with the concepts of the selected dataset
		        	m_Conceptsdata.clear();
		        	//First add keyword concepts
		        	for(Concept c : newValue.Concepts())
		        		if(c.Mark().startsWith("1")) m_Conceptsdata.add(c);
		        	//Then add non-keyword concepts
		        	for(Concept c : newValue.Concepts())
		        		if(c.Mark().startsWith("0")) m_Conceptsdata.add(c);
		        }
		      }
		});

		//The load button opens a new window which lets the user select from all available portals
		CategorizerWindow thisWindow = this;
		m_LoadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					//Load the new window
					FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("LoadPortals.fxml"));
					Parent root1 = (Parent) loader.load();
			        Stage stage = new Stage();
			        stage.initModality(Modality.NONE);
			        stage.initStyle(StageStyle.DECORATED);
			        stage.setTitle("Load Datasets");
			        stage.setScene(new Scene(root1));  
			        //Tell the selection window which portals exist
			        PortalSelectionWindow selectPortal = loader.<PortalSelectionWindow>getController();
			        selectPortal.fillPortals(m_Database.getAllPortals());
			        //Allow the window to callback with the results
			        selectPortal.registerResultListener(thisWindow);
			        
				    stage.show();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (StorageException e1){
					e1.printStackTrace();
				}
		    }
		});
		
		//The unload button removes the selected portal from the list and removes all datasets belonging to the corresponding portal
		m_UnloadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				int selectedPortalIndex = m_PortalList.getSelectionModel().getSelectedIndex();
				//Only do something if a portal is selected
				if(selectedPortalIndex < 0) return;
				//Remove the portal from the categorizer and the tableview
				m_DatasetTable.getItems().removeAll(
						m_Categorizer.unloadPortal(m_PortalList.getItems().get(selectedPortalIndex))
				);
				//Clear the concept tableview
				m_Conceptsdata.clear();
				//Remove the portal from the listview
				m_PortalList.getItems().remove(selectedPortalIndex);
				}
		});
		
		//Populate tables
		m_DatasetTable.setItems(m_data);
		m_ConceptTable.setItems(m_Conceptsdata);
	}
	
	@Override
	public void resultsReady(List<String> list) {
		//Load all portals in the list of selected portals
		List<String> currentPortals = m_PortalList.getItems();
		for(String portal : list){
			if(!currentPortals.contains(portal)){
				//Load the corresponding datasets from the database into the categorizer and into the tableview
				try {
					m_Categorizer.loadPortal(portal);
					m_data.addAll(m_Categorizer.Datasets());
				} catch (StorageException e) {
					Alert error = new Alert(AlertType.ERROR, "Cannot connect to database!");
					error.showAndWait();
					return;
				}
				//Add the portal to the listview
				currentPortals.add(portal);
			}
		}
	}
	
	/*
	 * Connect this window to the database.
	 */
	public void setDatabase(Database db){
		m_Database = db;
		m_Categorizer = new Categorizer(m_Database);
	}
	
	
	@FXML
	private BorderPane m_MainPane;

	@FXML
	private Button m_LoadBtn;
	
	@FXML
	private Button m_UnloadBtn;
	
	@FXML
	private ListView<String> m_PortalList;
	
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
	
	@FXML
	private TableView<Concept> m_ConceptTable;
	@FXML
	private TableColumn<Concept, String> m_ConceptNameColumn;
	@FXML
	private TableColumn<Concept, String> m_ConceptCatColumn;
	@FXML
	private TableColumn<Concept, String> m_ConceptCatConfColumn;
	@FXML
	private TableColumn<Concept, String> m_ConceptRelScoreColumn;
	@FXML
	private TableColumn<Concept, String> m_ConceptCohScoreColumn;
	

	private final ObservableList<Dataset> m_data = FXCollections.observableArrayList();
	private final ObservableList<Concept> m_Conceptsdata = FXCollections.observableArrayList();
	private Database m_Database;
	private Categorizer m_Categorizer;
	
}
