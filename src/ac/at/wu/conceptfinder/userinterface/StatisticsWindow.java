package ac.at.wu.conceptfinder.userinterface;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import ac.at.wu.conceptfinder.application.Globals;
import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class StatisticsWindow implements Initializable {

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//Populate tables
		m_CategoriesTable.setItems(m_Categoriesdata);
		
		//Set columns for categories table
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
					   //Round to 3 digits for readabilty and convert to a % representation
					   frequency = Float.toString(
							   	Math.round(m_Categorizer.CategoriesToFrequency().get(category)*1000)/10.0f
							   	) + " %";
				   }
			       return new SimpleStringProperty(frequency);
			   }
		});
	}
	
		/*
	 * Set the categorizer object this window displays statistics for.
	 */
	
	public void setCategorizer(Categorizer cat){ 
		m_Categorizer = cat; 
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

		m_Categoriesdata.clear();
		//Set the data for the categories table.
		//Table entries are sorted by frequency of the category
		for(Map.Entry<BabelDomain, Float> categoryToFrequency : Globals.entriesSortedByValues(m_Categorizer.CategoriesToFrequency()))
			m_Categoriesdata.add(0, categoryToFrequency.getKey());
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
	private TableColumn<ConceptID, String> m_ConceptCountColumn;
	@FXML
	private TableColumn<ConceptID, String> m_AvgRelColumn;
	@FXML
	private TableColumn<ConceptID, String> m_AvgCohColumn;
	@FXML
	private TableColumn<ConceptID, String> m_WeightColumn;

	private final ObservableList<BabelDomain> m_Categoriesdata = FXCollections.observableArrayList();

	private Categorizer m_Categorizer;

}
