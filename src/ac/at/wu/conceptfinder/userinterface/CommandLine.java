package ac.at.wu.conceptfinder.userinterface;

import java.util.Scanner;
import java.util.Set;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.stringanalysis.Concept;

/*
 * A simple command line interface for concept finder
 */
public class CommandLine implements UserInterface {
	
	public CommandLine(){
		m_reader = new Scanner(System.in);
	}

	@Override
	public boolean start() {
		print("Conceptfinder started! Please input command: ");
		
		String input = m_reader.next();
		m_reader.close();

		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addCommandListener(CommandListener cListener) {
		m_listener = cListener;
	}

	@Override
	public void show(Set<Dataset> datasets, String message) {
		print(message);
		
		for(Dataset dataset : datasets){
			print("-------------------dataset-------------------");
			print("Dataset ID: " + dataset.ID().value());
			print("Description: " + dataset.Description());
			print("Title: " + dataset.Title());
			print("Keywords: " + concatStringSet(dataset.Keywords()));
			print("-------------------concepts-------------------");
			
			for(Concept concept : dataset.Concepts()){
				print("Concept ID: " + concept.ID());
			}
			
			
		}
	}

	@Override
	public void show(String message) {
		System.out.println(message);
	}

	private void print(String message){
		System.out.println(message);
	}
	
	private String concatStringSet(Set<String> set){
		String ret = "";
		
		for(String s : set)
			ret += s;
		
		return ret;
	}
	
	private Scanner m_reader;
	private CommandListener m_listener;
}
