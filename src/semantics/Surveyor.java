package semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Surveyor {
    private String surveyQuery=    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    						 "select ?class ?pred (count(*) as ?cnt) "+
    						 "{ ?subj a ?class . ?subj ?pred ?obj } " +
    						 "group by ?class ?pred " +
    						 "having (?cnt > 50) " +
							 "order by ?class ?cnt ";    
    private String prefix= "prefix : <http://www.w3.org/2001/sw/DataAccess/tests/result-set#> ";
    
    @SuppressWarnings("unchecked")
	public String doRemoteSurvey(HttpServletRequest request) {
    	String endpoint= request.getParameter("endpoint");
    	QueryExecution qe= QueryExecutionFactory.sparqlService(endpoint, surveyQuery);
    	ResultSet resultSet= qe.execSelect();
		Model resultGraph= ResultSetFormatter.toModel(resultSet);
		JSONObject result= new JSONObject();
		result.put("classCount", getClassCount(resultGraph));
		result.put("propCount", getPropertyCount(resultGraph));
		result.put("classList", getClassList(resultGraph));
		result.put("predicateMap", getPredicateMap(resultGraph));
		return result.toJSONString();
    }
    @SuppressWarnings("unchecked")
	public String doSurvey(Model model) {
		QueryExecution qe= QueryExecutionFactory.create(surveyQuery, model);
		ResultSet resultSet= qe.execSelect();
		Model resultGraph= ResultSetFormatter.toModel(resultSet);
		JSONObject result= new JSONObject();
		result.put("classCount", getClassCount(resultGraph));
		result.put("propCount", getPropertyCount(resultGraph));
		result.put("classList", getClassList(resultGraph));
		result.put("predicateMap", getPredicateMap(resultGraph));
		return result.toJSONString(); 
    }    
    public String doRemoteQuery(HttpServletRequest request) {
    	String endpoint= request.getParameter("endpoint");
    	String query= request.getParameter("query");
    	QueryExecution qe= QueryExecutionFactory.sparqlService(endpoint, query);
		return this.getResults(qe.execSelect());				
    }    
	public String doQuery(Model model, String query) {
    	QueryExecution qe= QueryExecutionFactory.create(query, model);
		return this.getResults(qe.execSelect());				
    } 
	@SuppressWarnings("unchecked")
    public String getResults(ResultSet resultSet) {
    	JSONArray answers= new JSONArray();    	
		List<String> fields= resultSet.getResultVars();		
		while (resultSet.hasNext()) {
			JSONArray answer= new JSONArray();						
			QuerySolution result= resultSet.next();				
			for (String field : fields) {
				try { answer.add(result.get(field).toString()); }
				catch (NullPointerException e) {answer.add("none");}
			} 
			answers.add(answer);
		}
		JSONObject result= new JSONObject();
		result.put("fields", fields);
		result.put("answers", answers);
		return result.toJSONString();
    }
	public int getClassCount(Model resultGraph) {
    	String q= prefix + "select (count(?class) as ?classCount) " +
    					   "{ select distinct ?class { ?b :variable 'class' . ?b :value ?class } }";
    	ResultSet r= QueryExecutionFactory.create(q, resultGraph).execSelect();
    	QuerySolution qs= r.next();
    	return qs.get("classCount").asLiteral().getInt();
    }
	public int getPropertyCount(Model resultGraph) {
    	String q= prefix + "select (count(?property) as ?propCount) " +
    					   "{ select distinct ?property { ?property ^:value/:variable 'pred' } }";
    	ResultSet r= QueryExecutionFactory.create(q, resultGraph).execSelect();
    	QuerySolution qs= r.next();
    	return qs.get("propCount").asLiteral().getInt();
    }
	public ArrayList<String> getClassList(Model resultGraph) {
    	ArrayList<String> result= new ArrayList<String>();
    	String q= prefix + "select distinct ?class { ?class ^:value/:variable 'class' } ";
    	ResultSet r= QueryExecutionFactory.create(q, resultGraph).execSelect();
    	while (r.hasNext()) {
    		result.add(r.next().get("class").asResource().toString());
    	}
    	return result;
    }
	public HashMap<String, ArrayList<ArrayList<Object>>> getPredicateMap(Model resultGraph) {
    	HashMap<String, ArrayList<ArrayList<Object>>> result= new HashMap<String, ArrayList<ArrayList<Object>>>();
    	String q= prefix + "select ?class ?prop ?cnt " +
    					   "{ ?solution :binding ?b1 . ?b1 :variable 'class' . ?b1 :value ?class . " +
    					   "  ?solution :binding ?b2 . ?b2 :variable 'pred' . ?b2 :value ?prop . " +
    					   "  ?solution :binding ?b3 . ?b3 :variable 'cnt' . ?b3 :value ?cnt }";
    	ResultSet r= QueryExecutionFactory.create(q, resultGraph).execSelect();
    	while (r.hasNext()) {
    		QuerySolution qs= r.next();
    		String klass= qs.get("class").asResource().toString();
    		String prop= qs.get("prop").asResource().toString();    		
    		int cnt= qs.getLiteral("cnt").getInt();
    		ArrayList<Object> resultValue= new ArrayList<Object>();
    		resultValue.add(0, prop);
    		resultValue.add(1, cnt);
    		if (result.containsKey(klass)) { result.get(klass).add(resultValue); }
    		else { 
    			ArrayList<ArrayList<Object>> values= new ArrayList<ArrayList<Object>>();
    			values.add(resultValue);
    			result.put(klass, values);
    		}
    	}
    	return result;
    }
}
