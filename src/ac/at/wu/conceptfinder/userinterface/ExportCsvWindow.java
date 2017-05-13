package ac.at.wu.conceptfinder.userinterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.at.wu.conceptfinder.dataset.Categorizer;
import ac.at.wu.conceptfinder.dataset.ConceptFeatures;
import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;

/*
 * Displays a window with a progress bar and runs the categorization algorithm with a given configuration
 * on a given number of portals and saves the result to a target csv file
 */
public class ExportCsvWindow {
	
	public ExportCsvWindow(List<String> portals){
		m_selectedPortals = portals;
	}
	
	//Displays the window and performs categorization
	public void display(Database database, Categorizer categorizer, File destination) throws IOException, StorageException{
		//Setup up a new blocking window 
		final Stage window = new Stage();
		window.initStyle(StageStyle.UTILITY);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setResizable(false);
		HBox hBox = new HBox();
		VBox vBox = new VBox();
		window.setTitle("Exporting results");
		//Setup progressbar and indicator and add it to the ui
		m_progressBar = new ProgressBar(0);
		m_progressBar.setPrefHeight(30);
		m_progressBar.setPrefWidth(240);
		m_indicator = new ProgressIndicator(0);
		hBox.getChildren().addAll(m_progressBar, m_indicator);
		hBox.setSpacing(10);
		//hBox.setPadding(new Insets(10, 10, 10, 10));
		hBox.setAlignment(Pos.CENTER);
		
		Button cancelBtn = new Button("Cancel");
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);
		vBox.setPadding(new Insets(10, 10, 10, 10));
		vBox.getChildren().addAll(hBox, cancelBtn);
		
		Scene scene = new Scene(vBox, 300, 120);
		
		window.setScene(scene);
		window.show();
		
		Service<Void> service = new Service<Void>(){
			
			@Override
			protected Task<Void> createTask() {
				return new Task<Void>(){
					@Override
					protected Void call() throws Exception {
						double progress = 0;
						updateProgress(progress, 1);
						//Since the current selection of portals/datasets must not be altered 
				        //create a new categorizer with the same configuration
				        Categorizer localCat = new Categorizer(database, categorizer.Configuration());
				        //Also add all edited concepts to this new categorizer
				        for(Map.Entry<ConceptID, ConceptFeatures> idWithFeatures : categorizer.ConceptIDsToFeatures().entrySet())
				        	if(idWithFeatures.getValue().getEdited())
				        		localCat.addConceptWithFeatures(idWithFeatures.getKey(), idWithFeatures.getValue());
				        //Run the categorization on every portal on the list and save the results to a list
				        //A result is a mapping of categories to frequencies for each portal
				        Map<String, Map<BabelDomain, Float>> results = new HashMap<String, Map<BabelDomain, Float>>();
				        for(String portal : m_selectedPortals){
				        	
				        	if (isCancelled()) return null;
				        	localCat.loadPortal(portal);
				        	localCat.categorize();
				        	results.put(portal, new HashMap<BabelDomain, Float>(localCat.CategoriesToFrequency()));
				        	localCat.unloadPortal(portal);
				        	//The progressbar is increased for every categorized portal and once for writing the results to the file
				        	progress += 1 / ((float) m_selectedPortals.size() + 1);
				        	System.out.println(progress);
				        	updateProgress(progress, 1);
				        }
				        //Write the results the the previously selected file in the csv format - use a colon as a separator
				        FileWriter fWriter = new FileWriter(destination);
				        //As a header name the columns of the file -  first column is the name of the portal, 
				        //followed by a column for each category
				        fWriter.write("\"portal\"");
				        for(BabelDomain cat : BabelDomain.values())
				        	fWriter.write(",\"" + cat + "\"");
				        //Go through the results and add a line for each one to the csv
				        for(Map.Entry<String, Map<BabelDomain, Float>> result : results.entrySet()){
				        	if (isCancelled()) return null;
				        	//Start a new line
				        	fWriter.write(System.lineSeparator());
				        	//First column is the name of the portal
				        	fWriter.write("\"" + result.getKey() + "\"");
				        	//Add a frequency for every category
				        	for(BabelDomain category : BabelDomain.values()){
				        		Float frequency = result.getValue().get(category);
				        		//If this category does not appear in the portals it is 0
				        		if(frequency == null) frequency = 0.0f;
				        		//Write the frequency to the file
				        		fWriter.write("," + String.valueOf(frequency));
				        	}
				        }
				        fWriter.close();
				        updateProgress(1, 1);
				        succeeded();
						return null;
					}
				};
			}
		};
		m_progressBar.progressProperty().bind(service.progressProperty());
		m_indicator.progressProperty().bind(service.progressProperty());
		window.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                service.cancel();
            }

        });
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
	            service.cancel();
	            window.close();
	        }
	    });
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				cancelBtn.setText("Ok");				
			}
		});
			
		service.start();
		
	}
	
	
		
	
	
	private List<String> m_selectedPortals;
	private ProgressBar m_progressBar;
	private ProgressIndicator m_indicator;
}
