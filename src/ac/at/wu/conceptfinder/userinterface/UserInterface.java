package ac.at.wu.conceptfinder.userinterface;

import java.util.Set;

import ac.at.wu.conceptfinder.dataset.Dataset;

/*
 * An interface for a userinterface
 * the userinterfaces uses events to signal when a certain command has been
 * issued by the user
 */
public interface UserInterface {
	//Starts the user interface, returns true if successfull otherwise false
	public boolean start();
	
	//Stops the user interface, returns true if successfull otherwise false
	public boolean stop();
	
	//Add an EventListener for command events raised by this UserInterface  
	public void addCommandListener(CommandListener cListener);
	
	//Displays a set of Datasets together with a message
	public void show(Set<Dataset> datasets, String message);
	
	//Displays a message
	public void show(String message);
	
}
