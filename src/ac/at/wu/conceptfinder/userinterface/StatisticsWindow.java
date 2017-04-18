package ac.at.wu.conceptfinder.userinterface;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ac.at.wu.conceptfinder.application.Globals;
import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.dataset.Configuration;
import ac.at.wu.conceptfinder.dataset.Categorizer.ConceptFeatures;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
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
		m_ConceptFreqColumn.setEditable(false);
		m_ConceptNameColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
				   String name = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   name = features.Name();
			       return new SimpleStringProperty(name);
			   }
		});
		m_ConceptFreqColumn.setEditable(false);
		m_ConceptFreqColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
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
		m_ConceptCatColumn.setEditable(false);
		m_ConceptCatColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
				   String cat = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   if(features.Category() != null)
						   cat = features.Category().toString();
			       return new SimpleStringProperty(cat);
			   }
		});
		m_ConceptCatConfColumn.setEditable(false);
		m_ConceptCatConfColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
				   String conf = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   conf = Float.toString(features.CatConf());
			       return new SimpleStringProperty(conf);
			   }
		});
		m_AvgRelColumn.setEditable(false);
		m_AvgRelColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
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
		m_AvgCohColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
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
	    m_WeightColumn.setCellFactory(new Callback<TableColumn<ConceptID, String>, TableCell<ConceptID, String>>() {
            public TableCell<ConceptID, String> call(TableColumn<ConceptID, String> p) {
                return new EditingCell();
             }
        });
	    m_WeightColumn.setOnEditCommit(
	            new EventHandler<CellEditEvent<ConceptID, String>>() {
	                @Override
	                public void handle(CellEditEvent<ConceptID, String> t) {
	                	float newWeight;
	                	try{
	                		newWeight = Float.parseFloat(t.getNewValue());
	                	}catch (NumberFormatException e){
	                		Alert alert = new Alert(AlertType.ERROR);
	                		alert.setContentText("Please enter a number!");
	                		alert.showAndWait();
	                		m_ConceptsTable.refresh();
	                		return;
	                	}
	                    m_Categorizer.ConceptIDsToFeatures().get(
	                    		((ConceptID) t.getTableView().getItems().get(
	                        t.getTablePosition().getRow())
	                        ))
	                    .setWeight(newWeight);
	                }
	             }
	        );   
		m_WeightColumn.setCellValueFactory(new Callback<CellDataFeatures<ConceptID, String>, ObservableValue<String>>() {
			   public ObservableValue<String> call(CellDataFeatures<ConceptID, String> arg) {
				   String weight = "";
				   ConceptFeatures features = m_Categorizer.ConceptIDsToFeatures().get(arg.getValue());
				   if(features != null)
					   weight = Float.toString(features.Weight());
			       return new SimpleStringProperty(weight);
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
	
	public void setCategorizer(Categorizer cat){ 
		m_Categorizer = cat; 
	}
	
	public void registerResultListener(CategorizerCallback callback){
		m_Callback = callback;
	}
	
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
		HashMap<ConceptID, Float> conceptIDsToFrequencies = new HashMap<ConceptID, Float>();
		for(Map.Entry<ConceptID, ConceptFeatures> conceptIdToFeature : m_Categorizer.ConceptIDsToFeatures().entrySet())
			conceptIDsToFrequencies.put(conceptIdToFeature.getKey(), conceptIdToFeature.getValue().Frequency());
		
		//Add the conceptIDs in the appropriate order
		for(Map.Entry<ConceptID, Float> conceptIdToFrequency : Globals.entriesSortedByValues(conceptIDsToFrequencies))
			m_ConceptsData.add(0, conceptIdToFrequency.getKey());
	}

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
	private TableView<ConceptID> m_ConceptsTable;
	@FXML
	private TableColumn<ConceptID, String> m_ConceptNameColumn;
	@FXML
	private TableColumn<ConceptID, String> m_ConceptFreqColumn;
	@FXML
	private TableColumn<ConceptID, String> m_ConceptCatColumn;
	@FXML
	private TableColumn<ConceptID, String> m_ConceptCatConfColumn;
	@FXML
	private TableColumn<ConceptID, String> m_AvgRelColumn;
	@FXML
	private TableColumn<ConceptID, String> m_AvgCohColumn;
	@FXML
	private TableColumn<ConceptID, String> m_WeightColumn;

	private final ObservableList<BabelDomain> m_CategoriesData = FXCollections.observableArrayList();
	private final ObservableList<ConceptID> m_ConceptsData = FXCollections.observableArrayList();

	private Categorizer m_Categorizer;
	private CategorizerCallback m_Callback;
	
	class EditingCell extends TableCell<ConceptID, String> {
		 
        private TextField textField;
 
        public EditingCell() {
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

}
