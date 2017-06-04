package ac.at.wu.conceptfinder.application;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import ac.at.wu.conceptfinder.application.CategorizerCallback;
import ac.at.wu.conceptfinder.application.Globals;
import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.dataset.ConceptFeatures;
import ac.at.wu.conceptfinder.dataset.Configuration;
import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptId;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;

public class CategorizerWindow implements Initializable, CategorizerCallback {

	/*
	 * Table cells for concepts are bold if they represent a concept found in the keywords
	 * Also if relevance score and coherence score are exactly 0 the the text color
	 * is set to blue to indicate that this concept was found by the MCS heuristic
	 * The tooltip of a cell displays its contents.
	 * 
	 */
	
	private enum CellType{
		nonEditable, category, confidence, weight;
	}
	
	private class ConceptTableCell extends TableCell<Concept, String>{
		private CellType m_type;
		
		public ConceptTableCell(CellType type){
			super();
			m_type = type;
		}
			   
		@Override
		protected void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty); 
	        this.setFont(Font.getDefault());
	        this.setTextFill(Color.BLACK);
	        this.setBackground(Background.EMPTY);
	        
	        Concept concept = (Concept) this.getTableRow().getItem();
	        if(concept != null){
	        	if(concept.Mark().startsWith("1")){
	        		Font stdFont = Font.getDefault();
	    	        this.setFont(Font.font(stdFont.getFamily(), FontWeight.BOLD, stdFont.getSize()));	        		
	        	}
	        	if(concept.Scores().RelevanceScore() == 0 && concept.Scores().CoherenceScore() == 0)
	        		this.setTextFill(Color.BLUE);
	        	//Use categpry, confidence and weight from categorizer, since only there the edited values are saved
	        	ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(concept.ID());
	        	//Color edited concepts
	        	if(features.getEdited())
	        		this.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgb(220,220,220)"), CornerRadii.EMPTY, Insets.EMPTY)));
	        	
		        if(m_type == CellType.category){
		        	if(features != null)
		        		if(features.Category() != null)
		        			item = features.Category().toString();
		        }
		        if(m_type == CellType.confidence){
		        	if(features != null)
		        		item = Float.toString(m_Categorizer.ConceptIDsToFeatures().get(concept.ID()).CatConf());
		        }
		        if(m_type == CellType.weight){
		        	if(features != null)
		        		item = Float.toString(m_Categorizer.ConceptIDsToFeatures().get(concept.ID()).Weight());
		        }
	        }
	        this.setText(item);
	        Tooltip tooltip = new Tooltip(item);
	        hackTooltipStartTiming(tooltip);
	        this.setTooltip(tooltip);
	   }
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	
		//Set layout for UI
		m_DatasetTable.prefWidthProperty().bind(m_MainPane.widthProperty().divide(40).multiply(23));
		m_DatasetTable.prefHeightProperty().bind(m_MainPane.heightProperty().subtract(m_TopHBox.heightProperty()));
		m_ConceptTable.prefWidthProperty().bind(m_MainPane.widthProperty().divide(40).multiply(17));
		m_ConceptTable.prefHeightProperty().bind(m_MainPane.heightProperty().subtract(m_TopHBox.heightProperty()));
		m_PortalList.prefWidthProperty().bind(m_MainPane.widthProperty().divide(4));
		m_menuBar.prefWidthProperty().bind(m_MainPane.widthProperty());
		//Setting up the menu bar
		m_saveMenu.setOnAction(new EventHandler<ActionEvent>() {
	        public void handle(ActionEvent e) {
				//Set the configuration of the categorizer to the input fields.
				//If input is invalid do nothing.
				if(!setConfiguration()) return;
	        	
	            FileChooser fileChooser = new FileChooser();
	            fileChooser.setTitle("Save Settings");
	            fileChooser.setInitialFileName("Settings");
	            fileChooser.getExtensionFilters().add(new ExtensionFilter("Categorizer Settings", "*.csf"));
	            File file = fileChooser.showSaveDialog(new Stage());
	            if(file == null) return;
	            try {
					m_Categorizer.saveSettings(file);
				} catch (IOException e1) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Cannot save file");
					alert.showAndWait();
				}

	        }
	    });
		m_loadMenu.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
	            FileChooser fileChooser = new FileChooser();
	            fileChooser.setTitle("Load Settings");
	            fileChooser.getExtensionFilters().add(new ExtensionFilter("Categorizer Settings", "*.csf"));
	            File file = fileChooser.showOpenDialog(new Stage());
	            if(file == null) return;
	            if(!file.canRead()) return;
	            try {
					m_Categorizer.loadSettings(file);
				} catch (IOException e1) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Cannot load file");
					alert.showAndWait();
				}
	            refreshUI();
	        }
	    });
		//The export results as csv menu item first lets the user select one or more portals 
		//and then runs categorization on each portal saving the resulting category frequencies to a csv file
		CategorizerWindow thisWindow = this;
		m_ExportCSV.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					try {
						//Set the configuration of the categorizer to the input fields.
						//If input is invalid do nothing.
						if(!setConfiguration()) return;
						//Let the user select any number of portals
						SelectPortalWindow w = new SelectPortalWindow();
						List<String> selectedPortals = w.selectPortals(m_Database.getAllPortals());
						//Let the user chose a destination file
						FileChooser fileChooser = new FileChooser();
				        fileChooser.setTitle("Export category frequencies");
				        fileChooser.setInitialFileName("category frequencies");
				        fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV File", "*.csv"));
				        File file = fileChooser.showSaveDialog(new Stage());
				        if(file == null) return;
				        //Run categorization on the selected portals and save the results to the selected file
				        ExportCsvWindow exportWindow = new ExportCsvWindow(selectedPortals);
				        exportWindow.display(m_Database, m_Categorizer, file);
				        

					} catch (StorageException e1){
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("Cannot connect to Database");
						alert.showAndWait();
					} catch (IOException e1) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("Cannot write to this file");
						alert.showAndWait();
					}
				    }
		});
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
				   return getTextWrappingCell();
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
			       
			       for(Map.Entry<BabelDomain, Float> category : Globals.entriesSortedByValues(arg.getValue().Categories())){
			    	   categories =  "\u25B8 "  + category.getKey() + System.getProperty("line.separator") + categories;
			       }
			       if(categories != "") categories = categories.substring(0, categories.length()-2);
			       
			       return new SimpleStringProperty(categories);
			   }
		});
		m_ScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Dataset, String> arg) {
			       String categories = "";
			       
			       for(Map.Entry<BabelDomain, Float> category : Globals.entriesSortedByValues(arg.getValue().Categories())){
			    	   categories =  "\u25B8 "  + category.getValue() + System.getProperty("line.separator") + categories;
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
					   return new ConceptTableCell(CellType.nonEditable);
			   }
		});
		m_ConceptCatColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   //Concepts may or may not have categories
				   String category = arg.getValue().Category() != null ? arg.getValue().Category().toString() : "";  
				   return new SimpleStringProperty(category);
			   }
		});
		m_ConceptCatColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell(CellType.category);
			   }
		});
		m_ConceptCatConfColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   return new SimpleStringProperty(String.valueOf(arg.getValue().CatConfidence()));
			   }
		});
		m_ConceptCatConfColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell(CellType.confidence);
			   }
		});
		m_ConceptRelScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   //Remove digist after 2. decimal place
				   float relScore = arg.getValue().Scores().RelevanceScore();
				   relScore = Math.round(relScore*100)/100.0f;
				   return new SimpleStringProperty(String.valueOf(relScore));
			   }
		});
		m_ConceptRelScoreColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell(CellType.nonEditable);
			   }
		});
		m_ConceptCohScoreColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   //Remove digist after 2. decimal place
				   float cohScore = arg.getValue().Scores().CoherenceScore();
				   cohScore = Math.round(cohScore*100)/100.0f;
				   return new SimpleStringProperty(String.valueOf(cohScore));
			   }
		});
		m_ConceptCohScoreColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell(CellType.nonEditable);
			   }
		});
		m_ConceptWeightColumn.setCellValueFactory(new Callback<CellDataFeatures<Concept, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<Concept, String> arg) {
				   //Remove digist after 2. decimal place
				   float weight = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue().ID()).Weight();
				   weight = Math.round(weight*100)/100.0f;
				   return new SimpleStringProperty(String.valueOf(weight));
			   }
		});
		m_ConceptWeightColumn.setCellFactory(new Callback<TableColumn<Concept, String>, TableCell<Concept, String>>() {
			   public TableCell<Concept, String> call(TableColumn<Concept, String> arg) {
					   return new ConceptTableCell(CellType.weight);
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
		//Ctrl + C in Concepts table copies url to babelnet synset to clipboard
		m_ConceptTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent t) {
		    	if(t.isControlDown() && t.getCode() == KeyCode.C){
			    	Concept selectedConcept = m_ConceptTable.getSelectionModel().getSelectedItem();
			    	if(selectedConcept == null) return;

			        final Clipboard clipboard = Clipboard.getSystemClipboard();
			        final ClipboardContent content = new ClipboardContent();
		            content.putString("http://babelnet.org/synset?word=" + selectedConcept.ID().value());
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
		m_LoadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					SelectPortalWindow w = new SelectPortalWindow();
					loadPortals(w.selectPortals(m_Database.getAllPortals()));
				} catch (StorageException e1){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Cannot connect to Database");
					alert.showAndWait();
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
				m_data.removeAll(m_Categorizer.unloadPortal(m_PortalList.getItems().get(selectedPortalIndex)));
				m_DatasetTable.refresh();
				//Clear the concept tableview
				m_Conceptsdata.clear();
				//Remove the portal from the listview
				m_PortalList.getItems().remove(selectedPortalIndex);
				//Label for number of datasets
				m_numDatasetsLabel.setText(m_Categorizer.Datasets().size() + " Datasets loaded.");
		        //Refresh the statistics window
				m_StatisticsWindow.refresh();
				//Update the filter choicebox
				updateFilterChoiceBox();
				}
			
		});
		//Setup the statistics window
		//Load the new window
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Statistics.fxml"));
		Parent root1;
		try {
			root1 = (Parent) loader.load();
		} catch (IOException e2) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Cannot access statistics.fxml resource!");
			alert.showAndWait();
			return;
		}
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Statistics");
        stage.setScene(new Scene(root1));  
        //Keep the statistics window
        m_StatisticsWindow = loader.<StatisticsWindow>getController();
        //Register the callback with the statistics window
        m_StatisticsWindow.registerResultListener(thisWindow);
		
		//The statistics button brings up the statistics window for the current categorization
		m_StatsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
		        //Populate UI of statistics window
		        m_StatisticsWindow.refresh();   
			    stage.show();
		    }
		});
		
		//The categorize button performs the categorization of all active datasets using the settings entered in the textfields
		m_CategorizeBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//Set the configuration of the categorizer to the input fields.
				//If input is invalid do nothing.
				if(!setConfiguration()) return;
				//Clear the concept tableview
				m_Conceptsdata.clear();
				//Run the categorization algorithm
				m_Categorizer.categorize();
				//Refresh the dataset table
				m_DatasetTable.refresh();
				//Refresh statistics window
				m_StatisticsWindow.refresh();
				//Update the filter choicebox
				updateFilterChoiceBox();
			}
		});
		
		//When a filter is selected in the choicebox the tableview is update accordingly
		m_FilterChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
		      @Override
		      public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
		    	if(newVal == null){
			    	m_numDisplayedLabel.setText(m_data.size() + " Datasets displayed");
			    	return;
			    }
		    	m_data.clear();
		      	
		      	if(newVal.equals("All")){
	      			m_data.addAll(m_Categorizer.Datasets());	
		      	}else{
			      	//Only display datasets that belong to the selected category
			      	for(Dataset dataset : m_Categorizer.Datasets())
			      		if(dataset.Categories().containsKey(BabelDomain.valueOf(newVal)))
			      			m_data.add(dataset);
			    }
		      	m_numDisplayedLabel.setText(m_data.size() + " Datasets displayed");
		      }
		});
		
		//Populate tables and choicebox
		m_DatasetTable.setItems(m_data);
		m_ConceptTable.setItems(m_Conceptsdata);
		m_FilterChoice.setItems(m_FilterChoices);
	}
	
	private void loadPortals(List<String> list) {
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
		//Reset label for number of datasets
		m_numDatasetsLabel.setText(m_Categorizer.Datasets().size() + " Datasets loaded.");
		//Refresh statistics window
		m_StatisticsWindow.refresh();
		//update filter choicebox
		updateFilterChoiceBox();
	}
	
	@Override
	public void categorize(){
		m_CategorizeBtn.fire();
	}
	
	/*
	 * Connect this window to the database.
	 */
	public void setDatabase(Database db){
		m_Database = db;
		m_Categorizer = new Categorizer(m_Database);
		m_StatisticsWindow.setCategorizer(m_Categorizer);
	}
	
	/*
	 * Refreshes the UI
	 */
	private void refreshUI(){
		//update text fields
		Configuration conf = m_Categorizer.Configuration();
		m_MCSTf.setText(String.valueOf(conf.getMCSScore()));
		m_RelScoreTf.setText(String.valueOf(conf.getRelevanceWeight()));
		m_CohScoreTf.setText(String.valueOf(conf.getCoherenceWeight()));
		m_KeyTf.setText(String.valueOf(conf.getKeywordsWeight()));
		m_CatConfTf.setText(String.valueOf(conf.getCategoryConfidenceWeight()));
		m_RepeatTf.setText(String.valueOf(conf.getRepeatedConceptWeight()));
		m_MinCatsTf.setText(String.valueOf(conf.getMinOutputCategories()));
		m_MaxCatsTf.setText(String.valueOf(conf.getMaxOutputCategories()));
		m_MinCatScoreTf.setText(String.valueOf(conf.getMinScore()));
		//refresh tables
		m_DatasetTable.refresh();
		m_ConceptTable.refresh();
		//refresh statistics window
		m_StatisticsWindow.refresh();
		//refresh filter choicebox
		updateFilterChoiceBox();
	}
	
	/*
	 * Set the configuration of the Categorizer with the settings of the input fields.
	 * Checks the input fields and returns false if an error occured. 
	 */
	private boolean setConfiguration(){
		if(!checkInputFields()) return false;
		
		Configuration conf = m_Categorizer.Configuration();
		conf.setMCSScore(Float.parseFloat(m_MCSTf.getText()));
		conf.setRelevanceWeight(Float.parseFloat(m_RelScoreTf.getText()));
		conf.setCoherenceWeight(Float.parseFloat(m_CohScoreTf.getText()));
		conf.setKeywordsWeight(Float.parseFloat(m_KeyTf.getText()));
		conf.setCategoryConfidenceWeight(Float.parseFloat(m_CatConfTf.getText()));
		conf.setRepeatedConceptWeight(Float.parseFloat(m_RepeatTf.getText()));
		conf.setMaxOutputCategories(Integer.parseInt(m_MaxCatsTf.getText()));
		conf.setMinOutputCategories(Integer.parseInt(m_MinCatsTf.getText()));
		conf.setMinScore(Float.parseFloat(m_MinCatScoreTf.getText()));
		return true;
	}
	
	/*
	 * checks if all text fields have been filled correctly
	 * alerts the user otherwise
	 * @return true if all input is valid, false otherwise
	 */
	private boolean checkInputFields(){
		String allertMsg = "Invalid input: " + System.getProperty("line.separator");
		
		//Check input for MCS text field
		try{
			Float testValue = Float.parseFloat(m_MCSTf.getText());
			if(testValue < 0 || testValue > 1)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a number between 0 and 1 as a MCS score." + System.getProperty("line.separator");
		}
		//Check input for relevance score weight and coherence score weight text fields
		try{
			Float relScoreW = Float.parseFloat(m_RelScoreTf.getText());
			Float cohScoreW = Float.parseFloat(m_CohScoreTf.getText());
			if(relScoreW < 0 || relScoreW > 1)
				throw new NumberFormatException();
			if(cohScoreW < 0 || cohScoreW > 1)
				throw new NumberFormatException();
			if((relScoreW + cohScoreW) != 1)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a number between 0 and 1 as a weight for relevance and coherence score." + System.getProperty("line.separator");
			allertMsg += "Sum of the weights for relevance and coherence score must be 1." + System.getProperty("line.separator");
		}
		//Check input for MCS text field
		try{
			Float testValue = Float.parseFloat(m_KeyTf.getText());
			if(testValue < 0)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number as multiplier for keyword concepts." + System.getProperty("line.separator");
		}
		//Check input for category confidence weight
		try{
			Float testValue = Float.parseFloat(m_CatConfTf.getText());
			if(testValue < 0)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number as a weight for the weight of the category confidence of concepts." + System.getProperty("line.separator");
		}
		//Check input for weight of repeated concepts
		try{
			Float testValue = Float.parseFloat(m_RepeatTf.getText());
			if(testValue < 0)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number as a weight for repeated concepts." + System.getProperty("line.separator");
		}
		//Check input for number of output categories
		try{
			Integer testValue = Integer.parseInt(m_MaxCatsTf.getText());
			if(testValue < 1)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number for the maximum number of output categories." + System.getProperty("line.separator");
		}
		try{
			Integer testValue = Integer.parseInt(m_MinCatsTf.getText());
			if(testValue < 1)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number for the minimum number of output categories." + System.getProperty("line.separator");
		}
		try{
			Float testValue = Float.parseFloat(m_MinCatScoreTf.getText());
			if(testValue < 0.0f || testValue > 1.0f)
				throw new NumberFormatException();
		}catch (NumberFormatException e){
			allertMsg += "Please enter a positive number between 0 and 1 as minimum category score." + System.getProperty("line.separator");
		}
		if(allertMsg.length() > 18){
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(allertMsg);
			alert.showAndWait();
			return false;
		}
		return true;
	}
	
	/*
	 * Creates a cell that wraps the text inside it
	 */
	private TableCell getTextWrappingCell(){
		TableCell cell = new TableCell<>();
        Text text = new Text();
        text.setStyle("-fx-fill: -fx-text-background-color;");
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
	}
	
	/*
	 * Updates the filter choice box to the currently available categories plus an "All" option
	 */
	private void updateFilterChoiceBox(){
		m_FilterChoices.clear();
		m_FilterChoices.add("All");
		for(BabelDomain cat : m_Categorizer.CategoriesToFrequency().keySet())
			m_FilterChoices.add(cat.toString());
		//Select "All" filter as a default
		m_FilterChoice.getSelectionModel().select(0);
	}

	private void hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);

	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
	    } catch (Exception e) {
	        
	    }
	}
		
	@FXML
	private BorderPane m_MainPane;
	@FXML
	private HBox m_TopHBox;
	@FXML
	private MenuBar m_menuBar;
	@FXML
	private MenuItem m_saveMenu;
	@FXML
	private MenuItem m_loadMenu;
	@FXML
	private MenuItem m_ExportCSV;
	@FXML
	private TextField m_MCSTf;
	@FXML
	private TextField m_RelScoreTf;
	@FXML
	private TextField m_CohScoreTf;
	@FXML
	private TextField m_KeyTf;
	@FXML
	private TextField m_CatConfTf;
	@FXML
	private TextField m_RepeatTf;
	@FXML
	private TextField m_MinCatsTf;
	@FXML
	private TextField m_MaxCatsTf;
	@FXML
	private TextField m_MinCatScoreTf;
	@FXML
	private Button m_CategorizeBtn;
	@FXML
	private Button m_StatsButton;
	@FXML
	private Button m_LoadBtn;
	@FXML
	private Button m_UnloadBtn;
	@FXML
	private ListView<String> m_PortalList;
	@FXML
	private Label m_numDatasetsLabel;
	
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
	private Label m_numDisplayedLabel;
	@FXML
	private ChoiceBox<String> m_FilterChoice;
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
	@FXML
	private TableColumn<Concept, String> m_ConceptWeightColumn;

	private final ObservableList<Dataset> m_data = FXCollections.observableArrayList();
	private final ObservableList<Concept> m_Conceptsdata = FXCollections.observableArrayList();
	private final ObservableList<String> m_FilterChoices = FXCollections.observableArrayList();
	
	private StatisticsWindow m_StatisticsWindow;
	
	private Database m_Database;
	private Categorizer m_Categorizer;
	
}
