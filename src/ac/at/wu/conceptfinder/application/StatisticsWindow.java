package ac.at.wu.conceptfinder.application;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.dataset.ConceptFeatures;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.ConceptId;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.util.Callback;

public class StatisticsWindow implements Initializable {

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//Populate tables
		m_CategoriesTable.setItems(m_CategoriesData);
		m_ConceptsTable.setItems(m_ConceptsData);
		
		//Set dimensions of tables
		m_CategoriesTable.prefWidthProperty().bind(m_BaseVBox.widthProperty().divide(24).multiply(8));
		m_CategoriesTable.prefHeightProperty().bind(m_BaseVBox.heightProperty().subtract(m_TitleLabel.heightProperty().add(m_TopGrid.heightProperty())));
		m_ConceptsTable.prefWidthProperty().bind(m_BaseVBox.widthProperty().divide(24).multiply(16));
		m_ConceptsTable.prefHeightProperty().bind(m_BaseVBox.heightProperty().subtract(m_TitleLabel.heightProperty().add(m_TopGrid.heightProperty())));
		
		
		//Setup columns for categories table
		m_CategoryColumn.setCellValueFactory(new Callback<CellDataFeatures<BabelDomain, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<BabelDomain, String> arg) {
			       return new SimpleStringProperty(arg.getValue().toString());
			   }
		});
		m_CatFreqColumn.setCellValueFactory(new Callback<CellDataFeatures<BabelDomain, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<BabelDomain, String> arg) {
				   String frequency = "";
				   BabelDomain category = arg.getValue();
				   if(m_Categorizer != null){
					   //Round to 3 digits for readability and convert to a % representation
					   frequency = Float.toString(
							   	Math.round(m_Categorizer.CategoriesToFrequency().get(category)*1000)/10.0f
							   	) + " %";
				   }
			       return new SimpleStringProperty(frequency);
			   }
		});
		
		//Setup columns for concepts table
		m_ConceptsTable.setEditable(true);
		m_ConceptNameColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String name = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   name = features.Name();
			       return new SimpleStringProperty(name);
			   }
		});
		m_ConceptFreqColumn.setEditable(false);
		m_ConceptFreqColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String freq = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				 //Round to 3 digits for readability and convert to a % representation
				   if(features != null)
					   freq = Float.toString(
							   Math.round(features.Frequency() * 1000)/10.0f
							   ) + " %";
			       return new SimpleStringProperty(freq);
			   }
		});
		//Categories and category confidence can be edited by the user
		m_ConceptCatColumn.setEditable(true);
		m_ConceptCatColumn.setCellFactory(new Callback<TableColumn<ConceptId, String>, TableCell<ConceptId, String>>() {
            public TableCell<ConceptId, String> call(TableColumn<ConceptId, String> p) {
                return new ChoiceEditingCell();
             }
        });
		m_ConceptCatColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String cat = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   if(features.Category() != null)
						   cat = features.Category().toString();
			       return new SimpleStringProperty(cat);
			   }
		});
		m_ConceptCatConfColumn.setEditable(true);
		m_ConceptCatConfColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String conf = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   conf = Float.toString(features.CatConf());
			       return new SimpleStringProperty(conf);
			   }
		});
		m_ConceptCatConfColumn.setCellFactory(new Callback<TableColumn<ConceptId, String>, TableCell<ConceptId, String>>() {
            public TableCell<ConceptId, String> call(TableColumn<ConceptId, String> p) {
                return new TextEditingCell();
             }
        });
		m_ConceptCatConfColumn.setOnEditCommit(
	            new EventHandler<CellEditEvent<ConceptId, String>>() {
	                @Override
	                public void handle(CellEditEvent<ConceptId, String> t) {
	                	float newConf;
	                	try{
	                		newConf = Float.parseFloat(t.getNewValue());
	                	}catch (NumberFormatException e){
	                		Alert alert = new Alert(AlertType.ERROR);
	                		alert.setContentText("Please enter a number!");
	                		alert.showAndWait();
	                		m_ConceptsTable.refresh();
	                		return;
	                	}
	                	ConceptFeatures conceptFeatures = m_Categorizer.ConceptIDsToFeatures().get(
	                    		((ConceptId) t.getTableView().getItems().get(
	                        t.getTablePosition().getRow())
	                        ));
	                    conceptFeatures.setCatConf(newConf);
	                    conceptFeatures.setEdited(true);
	                }
	             }
	        );   
		m_AvgRelColumn.setEditable(false);
		m_AvgRelColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String score = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				 //Round to 3 digits for readability 
				   if(features != null)
					   score = Float.toString(
							   Math.round(features.AvgRelScore() * 1000)/1000.0f
							   );
			       return new SimpleStringProperty(score);
			   }
		});
		m_AvgCohColumn.setEditable(false);
		m_AvgCohColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String score = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				 //Round to 3 digits for readability 
				   if(features != null)
					   score = Float.toString(
							   Math.round(features.AvgCohScore() * 1000)/1000.0f
							   );
			       return new SimpleStringProperty(score);
			   }
		});
		//The weight column is an editable cell
		m_WeightColumn.setEditable(true);	             
	    m_WeightColumn.setCellFactory(new Callback<TableColumn<ConceptId, String>, TableCell<ConceptId, String>>() {
            public TableCell<ConceptId, String> call(TableColumn<ConceptId, String> p) {
                return new TextEditingCell();
             }
        });
	    m_WeightColumn.setOnEditCommit(
	            new EventHandler<CellEditEvent<ConceptId, String>>() {
	                @Override
	                public void handle(CellEditEvent<ConceptId, String> t) {
	                	float newWeight;
	                	try{
	                		newWeight = Float.parseFloat(t.getNewValue());
	                		if(newWeight < 0 || newWeight > 1)
	                			throw new NumberFormatException();
	                	}catch (NumberFormatException e){
	                		Alert alert = new Alert(AlertType.ERROR);
	                		alert.setContentText("Please enter a number between 0 and 1!");
	                		alert.showAndWait();
	                		m_ConceptsTable.refresh();
	                		return;
	                	}
	                	ConceptFeatures conceptFeatures = m_Categorizer.ConceptIDsToFeatures().get(
	                    		((ConceptId) t.getTableView().getItems().get(
	        	                        t.getTablePosition().getRow())
	        	                        )); 
	                    conceptFeatures.setWeight(newWeight);
	                    conceptFeatures.setEdited(true);
	                    m_ConceptsTable.refresh();
	                }
	             }
	        );   
		m_WeightColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptId, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptId, String> arg) {
				   String weight = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   weight = Float.toString(features.Weight());
			       return new SimpleStringProperty(weight);
			   }
		});
		//Create a context menu for reseting category and confidence to default
		final ContextMenu cm = new ContextMenu();
		MenuItem cmReset = new MenuItem("Reset to default");
		cmReset.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		    	ConceptId selectedID = m_ConceptsTable.getSelectionModel().getSelectedItem();
		    	ConceptFeatures cellFeatures = m_Categorizer.ConceptIDsToFeatures().get(selectedID);
		    	try {
					Map.Entry<BabelDomain, Float> stdValues = m_Categorizer.getDefaultCategoryWithConf(selectedID).entrySet().iterator().next();
					cellFeatures.setCategory(stdValues.getKey());
					cellFeatures.setCatConf(stdValues.getValue());
					cellFeatures.setWeight(1.0f);
					cellFeatures.setEdited(false);
					m_ConceptsTable.refresh();
				} catch (StorageException e1) {
					Alert alert = new Alert(AlertType.ERROR);
            		alert.setContentText("Cannot access database!");
            		alert.showAndWait();
				}
		    }
		});
		cm.getItems().add(cmReset);
		//Attach the context menus to the secondary mousebutton of the concepts table
		m_ConceptsTable.addEventHandler(MouseEvent.MOUSE_CLICKED,
			    new EventHandler<MouseEvent>() {
			        @Override public void handle(MouseEvent e) {
			            if (e.getButton() == MouseButton.SECONDARY)  
			            	cm.show(m_ConceptsTable, e.getScreenX(), e.getScreenY());
			        }
			});
		//Ctrl + C in Concepts table copies url to babelnet synset to clipboard
		m_ConceptsTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent t) {
			   	if(t.isControlDown() && t.getCode() == KeyCode.C){
			    	ConceptId selectedID = m_ConceptsTable.getSelectionModel().getSelectedItem();
			    	if(selectedID == null) return;

			    	final Clipboard clipboard = Clipboard.getSystemClipboard();
			        final ClipboardContent content = new ClipboardContent();
		            content.putString("http://babelnet.org/synset?word=" + selectedID.value());
			        clipboard.setContent(content);
			    }
		    }
		});
		//The apply weights button triggers a categorizationvin the main window
		m_ApplyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				m_Callback.categorize();
			}
		});
	}
	
	/*
	 * Set the categorizer object this window displays statistics for.
	 */
	
	public void setCategorizer(Categorizer cat){ m_Categorizer = cat; }
	
	public void registerResultListener(CategorizerCallback callback){ m_Callback = callback; }
	
	/*
	 * (Re)populates the UI elements.
	 *	Categorizer should be set before calling this routine. 
     */
	public void refresh(){
		if(m_Categorizer == null) return;
		//Set the text of the labels appropriately
		m_TitleLabel.setText("Statistics for " + m_Categorizer.Datasets().size() + " datasets:");
		//Round to 4 digits for readability
		m_avgConceptsLabel.setText(Float.toString(Math.round(m_Categorizer.AverageConceptCount()*1000)/1000.0f));
		m_numDistConceptsLabel.setText(Integer.toString(m_Categorizer.DistinctConceptsCount()));
		m_DistCatsLabel.setText(Integer.toString(m_Categorizer.CategoriesToFrequency().size()));

		m_CategoriesData.clear();
		//Set the data for the categories table.
		//Table entries are sorted by frequency of the category
		for(Map.Entry<BabelDomain, Float> categoryToFrequency : Globals.entriesSortedByValues(m_Categorizer.CategoriesToFrequency()))
			m_CategoriesData.add(0, categoryToFrequency.getKey());
		//Set the data for the concepts table
		m_ConceptsData.clear();
		//Table entries are sorted by frequency of the concepts in the datasets
		//Create a map of conceptIDs to the frequencies of the corresponding concepts
		HashMap<ConceptId, Float> conceptIDsToFrequencies = new HashMap<ConceptId, Float>();
		for(Map.Entry<ConceptId, ConceptFeatures> conceptIdToFeature : m_Categorizer.ConceptIDsToFeatures().entrySet())
			conceptIDsToFrequencies.put(conceptIdToFeature.getKey(), conceptIdToFeature.getValue().Frequency());
		
		//Add the conceptIDs in the appropriate order
		for(Map.Entry<ConceptId, Float> conceptIdToFrequency : Globals.entriesSortedByValues(conceptIDsToFrequencies))
			m_ConceptsData.add(0, conceptIdToFrequency.getKey());
		//refresh tables
		m_CategoriesTable.refresh();
		m_ConceptsTable.refresh();
	}

	@FXML
	private VBox m_BaseVBox;
	@FXML
	private GridPane m_TopGrid; 
	@FXML
	private Label m_TitleLabel;
	@FXML
	private Label m_avgConceptsLabel;
	@FXML
	private Label m_numDistConceptsLabel;
	@FXML
	private Label m_DistCatsLabel;
	@FXML
	private Button m_ApplyButton;
	
	@FXML
	private TableView<BabelDomain> m_CategoriesTable;
	@FXML
	private TableColumn<BabelDomain, String> m_CategoryColumn;
	@FXML
	private TableColumn<BabelDomain, String> m_CatFreqColumn;

	@FXML
	private TableView<ConceptId> m_ConceptsTable;
	@FXML
	private TableColumn<ConceptId, String> m_ConceptNameColumn;
	@FXML
	private TableColumn<ConceptId, String> m_ConceptFreqColumn;
	@FXML
	private TableColumn<ConceptId, String> m_ConceptCatColumn;
	@FXML
	private TableColumn<ConceptId, String> m_ConceptCatConfColumn;
	@FXML
	private TableColumn<ConceptId, String> m_AvgRelColumn;
	@FXML
	private TableColumn<ConceptId, String> m_AvgCohColumn;
	@FXML
	private TableColumn<ConceptId, String> m_WeightColumn;

	private final ObservableList<BabelDomain> m_CategoriesData = FXCollections.observableArrayList();
	private final ObservableList<ConceptId> m_ConceptsData = FXCollections.observableArrayList();

	private Categorizer m_Categorizer;
	private CategorizerCallback m_Callback;
	
	class TextEditingCell extends TableCell<ConceptId, String> {
		 
        private TextField textField;
 
        public TextEditingCell() {
        }
 
        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            setText((String) getItem());
            setGraphic(null);
        }
        
        @Override
        public void commitEdit(String value) {
            super.commitEdit(value);
            setGraphic(null);
        }
 
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }
 
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.setOnKeyPressed(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
	
	class ChoiceEditingCell extends TableCell<ConceptId, String> {
		 
        private ChoiceBox<String> m_ChoiceBox = new ChoiceBox<String>();

        public ChoiceEditingCell() {

        	for(BabelDomain cat : BabelDomain.values())
        		m_ChoiceBox.getItems().add(cat.toString());
        	m_ChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
  		      @Override
  		      public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
  			      	if(newVal == null) return;
  			      	ConceptFeatures cellFeatures = m_Categorizer.ConceptIDsToFeatures().get((ConceptId) getTableRow().getItem());
  			      	cellFeatures.setCategory(BabelDomain.valueOf(newVal));
  			      	cellFeatures.setEdited(true);
  			      	m_ConceptsTable.refresh();
  		      }
  			});
        }
        
        @Override
        public void commitEdit(String value) {
            super.commitEdit(value);
            setGraphic(null);
        }
 
        @Override
        public void startEdit() {
            super.startEdit();
            String value = getItem();
            if (value != null) {
                setGraphic(m_ChoiceBox);
                setText(null);
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(null);
        }
 
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            ConceptFeatures cellFeatures = m_Categorizer.ConceptIDsToFeatures().get((ConceptId) getTableRow().getItem());
            if(cellFeatures != null)
            	if(cellFeatures.getEdited()){
            		getTableRow().setStyle("-fx-background-color: grey;");
            	}else{
            		getTableRow().setStyle("");
            	}
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item);
            }
        }
    }
}
