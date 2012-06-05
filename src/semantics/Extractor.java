package semantics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Extractor {
	public String extract(HttpServletRequest request) {	 
		String endpoint= request.getParameter("endpoint");
		String query= request.getParameter("query");
		QueryExecution qe= QueryExecutionFactory.sparqlService(endpoint, query);						
		return this.getExtent(qe.execConstruct());
	}	
	public String save(HttpServletRequest request) {
		String result= "ok";
		String endpoint= request.getParameter("endpoint");
		String query= request.getParameter("query");
		String filename= request.getParameter("filename");
		String rdfpath= request.getServletContext().getRealPath("resources/rdf/"+filename);
		String querypath= request.getServletContext().getRealPath("resources/queries.json");		
		try {
			saveRdf(endpoint, query, querypath, rdfpath);
			saveQuery(query, filename, querypath);			
		} catch (Exception e) { result= e.toString(); }
		return result;
	}	
	public String delete(HttpServletRequest request) {		
		String result= "ok";
		String filename= request.getParameter("filename");
		String rdfpath= request.getServletContext().getRealPath("resources/rdf/"+filename);
		String querypath= request.getServletContext().getRealPath("resources/queries.json");		
		try {
			deleteQuery(filename, querypath);
			deleteRdf(rdfpath);			
			return result;
		} catch (Exception e) { result= e.toString(); } 
		return result;
	}	
	public String getSavedConstructs(HttpServletRequest request) 
			throws FileNotFoundException, IOException {
		String result;
		String querypath= request.getServletContext().getRealPath("resources/queries.json");
		File qf= new File(querypath);		
		JSONObject constructs= new JSONObject();
		try {
			if (qf.length() != 0) { 
				constructs= (JSONObject) new JSONParser().parse(new FileReader(querypath)); 
			}
			result= constructs.toJSONString();
		} catch (ParseException e) { result= e.toString(); }
		return result;
	}
	@SuppressWarnings("unchecked")
	private String getExtent(Model model) {
		String query= "select ?s ?p ?o { ?s ?p ?o }";
		ResultSet rs= QueryExecutionFactory.create(query, model).execSelect();		
		JSONArray answers= new JSONArray();
		while (rs.hasNext()) {
			QuerySolution qs= rs.next();
			JSONArray answer= new JSONArray();			
			answer.add(qs.get("s").toString());
			answer.add(qs.get("p").toString());
			answer.add(qs.get("o").toString());
			answers.add(answer);
		}
		JSONArray fields= new JSONArray();
		fields.add("s");
		fields.add("p");
		fields.add("o");
		JSONObject result= new JSONObject();
		result.put("fields", fields);
		result.put("answers", answers);
		return result.toJSONString();
	} 
	private void deleteQuery(String filename, String querypath) 
			throws FileNotFoundException, IOException, ParseException {
		File qf= new File(querypath);		
		if (qf.length() != 0) {
			JSONObject queries= (JSONObject) new JSONParser().parse(new FileReader(querypath));			
			queries.remove(filename);			
			FileWriter out= new FileWriter(querypath);
			out.write(queries.toJSONString());			
			out.flush();			
			out.close();
		}			
	}  
	private void deleteRdf(String rdfpath) 
			throws IOException {
		File delFile= new File(rdfpath);		
		if (delFile.exists()) { delFile.delete(); }
	}
	@SuppressWarnings("unchecked")
	private void saveQuery(String query, String filename, String querypath) 
			throws IOException, ParseException, FileNotFoundException{
		File qf= new File(querypath);		
		JSONObject queries= new JSONObject();
		if (qf.length() != 0) { 
			queries= (JSONObject) new JSONParser().parse(new FileReader(querypath)); 
		}
		queries.put(filename, query);
		FileWriter queriesFile= new FileWriter(querypath);
		queriesFile.write(queries.toJSONString());		
		queriesFile.flush();			
		queriesFile.close();
	}
	private void saveRdf(String endpoint, String query, String querypath, String rdfpath) 
			throws IOException {
		QueryExecution qe= QueryExecutionFactory.sparqlService(endpoint, query);
		Model m= qe.execConstruct();
		File rdfFile;
		FileOutputStream rdfOut;		
		rdfFile= new File(rdfpath);
		rdfOut= new FileOutputStream(rdfFile);
		m.write(rdfOut);
		rdfOut.flush();
		rdfOut.close();		
	}
}
