package ac.at.wu.conceptfinder.storage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.postgresql.util.PSQLException;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.DatasetFormat;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.dataset.Distribution;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptCreator;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import ac.at.wu.conceptfinder.stringanalysis.ConceptScores;
import ac.at.wu.conceptfinder.stringanalysis.InvalidConceptIDException;
import ac.at.wu.conceptfinder.stringanalysis.Language;
import ac.at.wu.conceptfinder.stringanalysis.LanguageDetector;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * Manages connection to a postgre database
 */
public class Database {
	
	public Database(String host, String username, String password, ConceptCreator conceptCreator) throws StorageException{
		m_username = username;
		m_password = password;
		m_host = host;
		m_conceptCreator = conceptCreator;
		//Load the mapping for active conceptids to categories
		m_conceptIdToCategory = loadActiveDomains();
		try{
			CreateDatasetTables();
		}catch (SQLException e){
			e.printStackTrace();
			throw new StorageException("Some SQLException happened in Databse class for host: " + host, StorageError.SQLError);
		}
	}
	
	/*
	 * outdated
	 */
	private void CreateDatasetTables() throws SQLException, StorageException{
		
		
		Connection connection = getConnection();
				
		if(!tableExists(connection, "dataset")){
			Statement create = connection.createStatement();
			
			String qry = "CREATE TABLE dataset("
					+ "id varchar(255) NOT NULL PRIMARY KEY,"
					+ "description varchar(16383),"
					+ "title varchar(4095),"
					+ "keywords varchar(4095),"
					+ "language varchar(5),"
					+ "issued timestamptz,"
					+ "modified timestamptz,"
					+ "portal varchar(255),"
					+ "categories varchar(127))";
			
			create.executeUpdate(qry);
			create.close();
		}
		
		if(!tableExists(connection, "concept")){
			Statement create = connection.createStatement();
			
			String qry = "CREATE TABLE concept("
					+ "datasetID varchar(255) REFERENCES dataset(id) ON DELETE CASCADE,"
					+ "conceptID varchar(255),"
					+ "name varchar(255),"
					+ "disambiguationscore float,"
					+ "relevancescore float,"
					+ "coherencescore float,"
					+ "totalscore float,"
					+ "mark varchar(50))";
			
			create.executeUpdate(qry);
			create.close();
		}
		
		if(!tableExists(connection, "distribution")){
			Statement create = connection.createStatement();
			
			String qry = "CREATE TABLE distribution("
					+ "distributionID varchar(255) NOT NULL PRIMARY KEY,"
					+ "datasetID varchar(255) REFERENCES dataset(id) ON DELETE CASCADE,"
					+ "description varchar(16384),"
					+ "title varchar(4095),"
					+ "bytesize float,"
					+ "format varchar(511),"
					+ "accessurl varchar(2083),"
					+ "issued timestamptz,"
					+ "modified timestamptz)";
			
			create.executeUpdate(qry);
			create.close();
		}
		
		connection.close();
	}
	
	/*
	 * checks if table exists
	 */
	private boolean tableExists(Connection connection, String tableName) throws SQLException{
		DatabaseMetaData meta = connection.getMetaData();
		
		try(ResultSet res = meta.getTables(null, null, tableName, new String[] {"TABLE"})){
			while(res.next()) {
				String tName = res.getString("TABLE_NAME");
				if(tName != null && tName.equals(tableName))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * saves a dataset to the database under a given portalname
	 * if a dataset with the same DatasetID allready exists an exception is thrown
	 * use updateDataset instead
	 * DatasetIDs are saved as strings with maximum 255 characters
	 */
	public void createDatasets(Set<Dataset> datasets) throws StorageException {
		Connection connection = getConnection();

		try{
			connection.setAutoCommit(false);
			
			for(Dataset dataset : datasets){
				saveDataset(dataset, connection);
				saveConcepts(dataset.ID().value(), dataset.Concepts(), connection);
				//saveDistributions(dataset.ID().value(), dataset.Distributions(), connection);
			}
			
		}catch (SQLException e){
			String state = e.getSQLState();
			if(state.substring(0, 1).equals("08")){
				throw new StorageException("cannot connect to " + m_host, StorageError.cannotConnect);
			}else{
				e.printStackTrace();
				throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
			}
		} finally {
			try {
				connection.commit();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
			}
		}		
	}
		
	private void saveDataset(Dataset dataset, Connection connection) throws StorageException, SQLException{
		PreparedStatement insert;
		
		try {
			insert = connection.prepareStatement("INSERT INTO dataset VALUES(?,?,?,?,?,?,?,?)");
			
			insert.setString(1, dataset.ID().value());
			insert.setString(2, dataset.Description().length() < 16383 ? dataset.Description() : dataset.Description().substring(0, 16382));
			insert.setString(3, dataset.Title().length() < 4095 ? dataset.Title() : dataset.Title().substring(0, 4094));
			insert.setString(4, concatArray(dataset.Keywords().toArray(new String[dataset.Keywords().size()])));
			insert.setString(5, String.format(dataset.Language().name()));
			insert.setTimestamp(6, (dataset.Issued() == null) ? null : new Timestamp(dataset.Issued().getTime()));
			insert.setTimestamp(7, (dataset.Modified() == null) ? null : new Timestamp(dataset.Modified().getTime()));
			insert.setString(8, dataset.Portal());
			
			insert.executeUpdate();
			insert.close();
					
		} catch (PSQLException e) {
			if(e.getSQLState().equals("23505"))
				throw new StorageException("dataset with ID: " + dataset.ID().value() + " allready in database", StorageError.allreadyExists);
			throw e;
		}  
	}
	
	private void saveConcepts(String ID, List<Concept> concepts, Connection connection) throws SQLException{
		PreparedStatement insert = connection.prepareStatement("INSERT INTO concept VALUES(?,?,?,?,?,?,?,?)");
		
		for(Concept concept : concepts){
			insert.setString(1, ID);
			insert.setString(2, concept.ID().value());
			insert.setString(3, concept.Name());
			insert.setFloat(4, concept.Scores().DisambiguationScore());
			insert.setFloat(5, concept.Scores().RelevanceScore());
			insert.setFloat(6, concept.Scores().CoherenceScore());
			insert.setFloat(7, concept.Scores().TotalScore());
			insert.setString(8, concept.Mark());
				
			insert.executeUpdate();
		}
		insert.close();
	}
	
	private void saveDistributions(String datasetID, List<Distribution> distributions, Connection connection) throws SQLException{
		PreparedStatement insert = connection.prepareStatement("INSERT INTO distribution VALUES(?,?,?,?,?,?,?,?,?)");;
		
		for(Distribution dist : distributions){
			insert.setString(1, dist.ID().value());
			insert.setString(2, datasetID);
			insert.setString(3, dist.Description().length() < 16383 ? dist.Description() : dist.Description().substring(0, 16382));
			insert.setString(4, dist.Title().length() < 4095 ? dist.Title() : dist.Title().substring(0, 4094));
			insert.setFloat(5, dist.Bytesize());
			insert.setString(6, (dist.Format() == null) ? "" : concatArray(dist.Format().allSynonyms()));
			insert.setString(7, (dist.AccessURL() == null) ? "" : dist.AccessURL().toExternalForm());
			insert.setTimestamp(8, (dist.Issued() == null) ? null : new Timestamp(dist.Issued().getTime()));
			insert.setTimestamp(9, (dist.Modified() == null) ? null : new Timestamp(dist.Modified().getTime()));
			
			insert.executeUpdate();
		}
		insert.close();
	}
	
	private String concatArray(String[] words){
		String ret = "";
		if(words.length == 0) return ret;
		
		for(String s : words)
			ret += s + "#";

		return ret.substring(0, ret.length() - 1);
	}
		
	/*
	 * updates and/or creates new datasets under a given portal in the database
	 * portal information will be updated if it differs
	 * takes longer than createDatasets()
	 * @param updateConcepts signals if concepts need to be updated
	 */
	public void saveDatasets(Set<Dataset> datasets, boolean updateConcepts) throws StorageException{
		//Establish a connection to the database
		Connection connection = getConnection();
		
		try {
			//Create a prepared select query that will be used to check 
			//if a specific dataset (identified by its ID) exists in the database
			PreparedStatement select = connection.prepareStatement("SELECT exists(SELECT id FROM dataset WHERE id = ?) as exist");
			//Keep a list of datasets that have been found in the database
			HashSet<Dataset> existing = new HashSet<Dataset>();
			//Keep another list of datasets that are new
			HashSet<Dataset> newData = new HashSet<Dataset>();
			//go through the list of provided datasets
			for(Dataset dataset : datasets){
				//add the ID of the current dataset to the select query
				select.setString(1, dataset.ID().value());
				
				//submit the prepared query to the database and store the results
				ResultSet result = select.executeQuery();
				result.next();
				
				//if the dataset exists
				if(result.getBoolean(1)){
					//add it to the list of existing datasets
					existing.add(dataset);
				}else{
					//otherwise add them to the list of new datasets
					newData.add(dataset);
				}
				result.close();
			}
			//update existing datasets
			updateDatasets(existing, updateConcepts);
			
			//create new datasets
			createDatasets(newData);
			
			//Close connection
			select.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
	}
	
	/*
	 * updates existing datasets
	 * does nothing if a dataset does not exist
	 * @param updateConcepts sginals if concepts need to be updated
	 * !CURRENTLY IGNORES DISITRBUTIONS!
	 */
	public void updateDatasets(Set<Dataset> datasets, boolean updateConcepts) throws StorageException{
		Connection connection = getConnection();
		
		try{
			connection.setAutoCommit(false);
		}catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
		
		Connection selectConnection = getConnection();
		Statement select = getStatement(selectConnection);
		
		for(Dataset dataset : datasets){
		
			try{
				//System.out.println("Updating dataset: " + dataset.ID().value());
				PreparedStatement update = connection.prepareStatement("UPDATE dataset SET "
						+ "description = ?, "
						+ "title = ?,"
						+ "keywords = ?,"
						+ "language = ?,"
						+ "issued = ?,"
						+ "modified = ?,"
						+ "portal = ?"
						+ " WHERE id = ?");

				update.setString(1, dataset.Description().length() < 16383 ? dataset.Description() : dataset.Description().substring(0, 16382));
				update.setString(2, dataset.Title().length() < 4095 ? dataset.Title() : dataset.Title().substring(0, 4094));
				update.setString(3, concatArray(dataset.Keywords().toArray(new String[dataset.Keywords().size()])));
				update.setString(4, String.format(dataset.Language().name()));
				update.setTimestamp(5, (dataset.Issued() == null) ? null : new Timestamp(dataset.Issued().getTime()));
				update.setTimestamp(6, (dataset.Modified() == null) ? null : new Timestamp(dataset.Modified().getTime()));
				update.setString(7, dataset.Portal());
				update.setString(8,  dataset.ID().value());
				
				update.executeUpdate();
				update.close();
				//System.out.println("Done!");
				
				if(!updateConcepts) continue;
				
				/*ResultSet resultSet = select.executeQuery("SELECT distributionid FROM distribution WHERE datasetid = '" + dataset.ID().value() + "'");
				HashMap<String, Boolean> distIds = new HashMap<String, Boolean>();
				while(resultSet.next())
					distIds.put(resultSet.getString("distributionid"), false);
				
				ArrayList<Distribution> missingDists = new ArrayList<Distribution>();
				
				for(Distribution dist : dataset.Distributions()){
					boolean found = false;
					
					for(String distId : distIds.keySet()){
						if(distId.equals(dist.ID().value())){
							distIds.replace(distId, false, true);
							found = true;
						}
					}
					if(!found) missingDists.add(dist);
				}
				
				for(String distId : distIds.keySet()){
					if(!distIds.get(distId)){
						update.executeUpdate("DELETE from distribution WHERE distributionid = '" + distId + "'");
					}
				}
			
				saveDistributions(dataset.ID().value(), missingDists, connection);*/

				Statement delete = connection.createStatement();
				delete.executeUpdate("DELETE FROM concept WHERE datasetid ='" + dataset.ID().value() + "'");
				saveConcepts(dataset.ID().value(), dataset.Concepts(), connection);

				
			}catch (SQLException e) {
				e.printStackTrace();
				throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
			}
		}
		
		try{
			connection.commit();
			select.close();
			connection.close();
			selectConnection.close();
		}catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
	}
	
	/*
	 * retrieve a dataset from the database
	 * returns null of it does not exist
	 */
	public Dataset getDataset(RdfId ID) throws StorageException{
		Connection connection = getConnection();
		Statement select = getStatement(connection);
		Dataset dataset = new Dataset(ID);
		String qry = "SELECT * from dataset WHERE id = '" + ID.value() + "'";

		try{
			ResultSet result = select.executeQuery(qry);
			PreparedStatement selectConcepts = connection.prepareStatement("SELECT * from concept WHERE datasetid = ?");
			selectConcepts.setString(1, ID.value());
			
			if(!result.next()) return null;
						
			dataset = fillDataset(result);
			
			result = selectConcepts.executeQuery();
			
			//for every row in the resulting table of concepts
			while(result.next()){
				//Create a concept and fill it with the results
				Concept concept;
				try {
					concept = fillConcept(result);
					//Add the concept to the dataset
					dataset.addConcept(concept);
				} catch (InvalidConceptIDException e) {
					//Ignore concepts with invalid IDs
				}
			}
			//fillDistributions(dataset, connection);
			
			selectConcepts.close();
			select.close();
			result.close();
			connection.close();
		}catch (SQLException e){
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
		return dataset;
	}
	
	/*
	 * Returns all datasets in the database matching the given search mask
	 * !CURRENTLY NOT LOADING DISTRIBUTIONS!
	 * @param mask may be null or an empty mask to retrieve all datasets
	 */
	public Set<Dataset> getDatasets(DatasetSearchMask mask) throws StorageException{
		//Establish a connection to the database
		Connection connection = getConnection();
		
		//turn off autocommit
		try{
			connection.setAutoCommit(false);
		}catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
		
		if(mask == null) mask = new DatasetSearchMask(null, null);
		
		//Prepare the first part of a select query string that selects
		//all datasets that match the search mask
		String qry = "SELECT * from dataset WHERE ";
		//Also prepare a query for the concepts belonging to the datasets
		String conceptQry = "SELECT * from concept WHERE datasetid IN (SELECT id FROM dataset WHERE ";
		
		//If the languages parameter of the mask is not empty
		if(!mask.Languages().isEmpty()){
			//Add open parenthesis to the queries to group language clauses together
			qry += "(";
			conceptQry += "(";
			//Go through the languages
			for(Language lang : mask.Languages()){
				//Add a matching clause for every possible language to the queries followed by an OR
				qry += "language = '" + lang.toString() + "' OR ";
				conceptQry += "language = '" + lang.toString() + "' OR ";
			}
			//Remove the last OR so the syntax is correct
			qry = qry.substring(0, qry.length() - 3);
			conceptQry = conceptQry.substring(0, conceptQry.length() - 3);
			//Close parenthesis
			qry += ")";
			conceptQry += ")";
		}
			
		//If the portals paramater of the mask is not empty
		if(!mask.Portals().isEmpty()){
			//If something has been added to the queries already we need and AND to make sure all
			//matching criteria are met
			if(qry.charAt(qry.length() - 1) == ')'){
				qry += " AND ";
				conceptQry += " AND ";
			}
			//Add open parenthesis to the queries to group language clauses together
			qry += "(";
			conceptQry += "(";
			//Go through the portals
			for(String portal : mask.Portals()){
				//Add a matching clause for every possible portal to the query followed by an OR
				qry += "portal = '" + portal + "' OR ";
				conceptQry += "portal = '" + portal + "' OR ";
			}
			//Remove the last OR so the syntax is correct
			qry = qry.substring(0, qry.length() - 3);
			conceptQry = conceptQry.substring(0, conceptQry.length() - 3);
			//Close parenthesis
			qry += ")";
			conceptQry += ")";
		}

		//If mask was empty (no clauses were added) remove WHERE from the query
		if(qry.endsWith("WHERE ")) qry = "SELECT * from dataset";
		if(conceptQry.endsWith("WHERE ")){ conceptQry = "SELECT * from concept";
		}else{
			//Close the subquery
			conceptQry += ")";
		}
		try{
			//submit the query to the database and store the resulting table
			Statement select = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			select.setFetchSize(1000);
			ResultSet datasetResults = select.executeQuery(qry);
						
			//Create a map of found datasets and their IDs 
			HashMap<RdfId, Dataset> foundDatasets = new HashMap<RdfId, Dataset>();
			
			//for every row in the resulting table of datasets
			while(datasetResults.next()){
				//Create a dataset and fill it with the results
				Dataset dataset = fillDataset(datasetResults);
			
				//add it to the list of found datasets
				foundDatasets.put(dataset.ID(), dataset);
			}
			
			//Submit concept query and store resulting table
			ResultSet conceptResults = select.executeQuery(conceptQry);

			//for every row in the resulting table of concepts
			while(conceptResults.next()){
				//Create a concept and fill it with the results
				Concept concept;
				try {
					concept = fillConcept(conceptResults);
					//Add the concept to the corresponding dataset
					foundDatasets.get(new RdfId(conceptResults.getString("datasetid"))).addConcept(concept);
				} catch (InvalidConceptIDException e) {
					//Ignore concepts with invalid IDs
				}
			}

			//Close connection
			select.close();
			conceptResults.close();
			datasetResults.close();
			connection.close();
			
			//return the list
			return new HashSet<Dataset>(foundDatasets.values());
			
		}catch (SQLException e){
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		} 
	}
	
	/*
	 * updates the mark of concepts of the given datasets in the database
	 * only works for existing concepts and datasets 
	 */
	public void updateConceptMark(Set<Dataset> datasets) throws StorageException{
		//Establish a connection to the database
		Connection connection = getConnection();

		try{
			//turn off autocommit
			connection.setAutoCommit(false);
			
			//Go through all datasets
			for(Dataset dataset : datasets){
				//Prepare an update statement to update each concept
				PreparedStatement update = connection.prepareStatement(
						"UPDATE concept SET "
								+ "mark = ?" 
								+ "WHERE datasetid = ? AND "
								+ "conceptid = ? AND "
								+ "name = ? AND "
								+ "disambiguationscore = ? AND "
								+ "relevancescore = ? AND "
								+ "coherencescore = ? AND "
								+ "totalscore = ?");
				//Go through all concepts of each dataset
				for(Concept concept : dataset.Concepts()){
					//specifiy the fields for each concept					
					update.setString(1, concept.Mark());
					update.setString(2, dataset.ID().value());
					update.setString(3, concept.ID().value());
					update.setString(4, concept.Name());
					update.setFloat(5, concept.Scores().DisambiguationScore());
					update.setFloat(6, concept.Scores().RelevanceScore());
					update.setFloat(7, concept.Scores().CoherenceScore());
					update.setFloat(8, concept.Scores().TotalScore());
					
					update.executeUpdate();
				}
				update.close();
			}
			//after all concepts have been updated commit
			connection.commit();
			connection.close();
			
		}catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
		
	}
	

	/*
	 * Gets the names of all portals available in the database
	 */
	public List<String> getAllPortals() throws StorageException{
		//Establish a connection to the database
		Connection connection = getConnection();
		Statement select = getStatement(connection);
	
		try {
			//Get the list of portals from the database
			ResultSet result = select.executeQuery("SELECT DISTINCT portal FROM dataset");
			//return the list 
			ArrayList<String> portals = new ArrayList<String>();
			while(result.next()){
				portals.add(result.getString("portal"));
			}
			return portals;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		} 		
	}
	
	/*
	 * Copies the contents of the babeldomains file to the database table "BabelDomains"
	 * (takes a few minutes)
	 */
	public void copyBabelDomains() throws StorageException, IOException{
		Connection connection = getConnection();
		
		try{
			connection.setAutoCommit(false);
			Statement insert = getStatement(connection);
			Map<ConceptID, Object[]> domainMap = loadConceptToDomain();
			for(ConceptID ID : domainMap.keySet()){
				insert.executeUpdate("INSERT INTO babeldomains VALUES('" +
						ID.value() + "','" +
						(BabelDomain) domainMap.get(ID)[0] + "'," +
						(float) domainMap.get(ID)[1] + ")");
			}
			connection.commit();
		}catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
	}
	
	//Loads the mapping of synsets to BabelDomains with confidence scores from the resource file
	private HashMap<ConceptID, Object[]> loadConceptToDomain() throws IOException {
		//Load the file where the domains are stored
		try (BufferedReader br = new BufferedReader(new FileReader("resources/babeldomains.txt"))) {
			//Initialise a map to hold 2,68 mio entries
			HashMap<ConceptID, Object[]> conceptToDomain = new HashMap<ConceptID, Object[]>(2680000, 1);
			//Go through the file line by line
			String line;
			while ((line = br.readLine()) != null) {
				//Each line consists of the synset ID the corresponding BabelDomain and a score for confidence
				//separated by a tab
				//Split the line at the tabs in the 3 parts mentioned above
				String[] parts = line.split("\t");
				//Create a ConceptID out of the first part
				ConceptID conceptID = new ConceptID(parts[0]);
				//Create a BabelDomain out of the second part 
				BabelDomain domain = BabelDomain.valueOfName(parts[1]);
				//Create a float out of the third part
				float confidence = Float.parseFloat(parts[2].startsWith("*") ? parts[2].substring(1) : parts[2]);
				//Add an entry to the mapping for concepts to BabelDomains
				conceptToDomain.put(conceptID, new Object[]{domain, confidence});
			}
			return conceptToDomain;
		}
	}
	
	/*
	 * Loads the "activebabeldomains" table in the database to a Map.
	 * This represents the mapping of synsets to categories and confidences.
	 * This map only contains synsets that currently can be found in the datasets.
	 * @return The values of this map are Object arrays which store the category as a
	 * 			BabelDomain object under index 0 and the corresponding confidence
	 * 			as a Float under index 1  
	 */
	private HashMap<ConceptID, Object[]> loadActiveDomains() throws StorageException{
		Connection connection = getConnection();
		Statement select = getStatement(connection);
		
		try {
			ResultSet results = select.executeQuery("SELECT * FROM activebabeldomains");
			HashMap<ConceptID, Object[]> babeldomainsMap = new HashMap<ConceptID, Object[]>(8000);
			while(results.next()){
				ConceptID cid = new ConceptID(results.getString("synsetid"));
				BabelDomain domain = BabelDomain.valueOf(results.getString("domain"));
				float confidence = results.getFloat("confidence");
				babeldomainsMap.put(cid, new Object[]{domain, confidence});
			}
			return babeldomainsMap;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new StorageException("Some SQL error happend when accessing activebabeldomains table ", StorageError.SQLError);
		}
		
	}
	
	/*
	 * fills a dataset with the data that is at the actual position of the cursor
	 * make sure it does not point to empty row!
	 */
	private Dataset fillDataset(ResultSet result) throws SQLException{
		Dataset dataset = new Dataset(new RdfId(result.getString("id")));
		
		dataset.setPortal(result.getString("portal"));
		dataset.setDescription(result.getString("description"));
		dataset.setTitle(result.getString("title"));
		
		for(String keyword : extractWords(result.getString("keywords")))
			dataset.addKeyword(keyword);
		
		dataset.setLanguage(LanguageDetector.stringToEnum(result.getString("language")));
		
		if(result.getTimestamp("issued") != null)
			dataset.setIssued(new Date(result.getTimestamp("issued").getTime()));
		if(result.getTimestamp("modified") != null)
			dataset.setModified(new Date(result.getTimestamp("modified").getTime()));
		
		return dataset;
	}
	
	private Concept fillConcept(ResultSet result) throws SQLException, InvalidConceptIDException{
		ConceptID cid = new ConceptID(result.getString("conceptid"));
		Object[] categoryAndConf = m_conceptIdToCategory.get(cid);
		BabelDomain category = null;
		float confidence = 0;
		
		//If the concept has a category 
		if(categoryAndConf != null){
			category = (BabelDomain) m_conceptIdToCategory.get(cid)[0];
			confidence = (float) m_conceptIdToCategory.get(cid)[1];
		}
		Concept concept = 	m_conceptCreator.createConcept(
						cid, 
						result.getString("name"),
				new ConceptScores(result.getFloat("disambiguationscore"),
						result.getFloat("relevancescore"), 
						result.getFloat("coherencescore"), 
						result.getFloat("totalscore")),
						result.getString("mark"),
						category,
						confidence);
		return concept;
	}
	
	private void fillDistributions(Dataset dataset, Connection connection) throws SQLException, StorageException{
		Statement select = getStatement(connection);
				
		String qry = "SELECT * from distribution WHERE datasetid = '" + dataset.ID().value() + "'";

		try{
			ResultSet result = select.executeQuery(qry);

			while(result.next()){
				Distribution dist = new Distribution(new RdfId(result.getString("distributionID")));
				dist.setDescription(result.getString("description"));
				dist.setTitle(result.getString("title"));
				dist.setBytsize(result.getFloat("bytesize"));
				dist.setFormat(new DatasetFormat(extractWords(result.getString("format"))));
			
				try{
					dist.setAccessURL(new URL(result.getString("accessurl")));
				} catch (MalformedURLException e){
					//leave URL empty
				}
				if(result.getTimestamp("issued") != null)
					dist.setIssued(new Date(result.getTimestamp("issued").getTime()));
				if(result.getTimestamp("modified") != null)
					dist.setModified(new Date(result.getTimestamp("modified").getTime()));

				dataset.addDistribution(dist);
			}
			
			result.close();
			select.close();
		}catch (SQLException e){
			e.printStackTrace();
			throw new StorageException("some SQL error occured with host: " + m_host, StorageError.SQLError);
		}
	}

	/*
	 * creates a DatasetFormat object out of a string of concatinated formats
	 */
	private String[] extractWords(String concatWords){
		if(concatWords == null) return new String[0];
		String[] words = concatWords.split("#");
				
		return words;
	}
	
	/*
	 * deletes a dataset in the database
	 */
	public void deleteDataset(RdfId ID) throws StorageException{
		Connection connection = getConnection();
		Statement delete = getStatement(connection);
		
		String qry = "DELETE FROM dataset WHERE "
				+ "id = '" + ID.value() + "'";
		
		try {
			delete.executeUpdate(qry);
			
			connection.close();
		}catch (SQLException e){
			throw new StorageException("cannot connect to " + m_host, StorageError.cannotConnect);
		}
	}
	
	/*
	 * tries to connect to the database, otherwise throws StorageException
	 */
	private Connection getConnection() throws StorageException{
		Connection connection = null;
		
		try{
			connection = DriverManager.getConnection(m_host, m_username, m_password);
		}catch (SQLException e){
			throw new StorageException("cannot connect to " + m_host, StorageError.cannotConnect);
		}
		return connection;
	}
	
	/*
	 * tries to create a statement, otherwise throws StorageException
	 */
	private Statement getStatement(Connection connection) throws StorageException{
		Statement stmt = null;
		
		try{
			stmt = connection.createStatement();
		}catch (SQLException e){
			throw new StorageException("cannot connect to " + m_host, StorageError.cannotConnect);
		}
		return stmt;
	}
	
	private String m_username;
	private String m_password;
	private String m_host;
	private ConceptCreator m_conceptCreator;
	private HashMap<ConceptID, Object[]> m_conceptIdToCategory;
}
