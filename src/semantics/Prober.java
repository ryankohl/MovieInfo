package semantics;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

public class Prober {	
	String dbp= "http://dbpedia.org/resource/";
    private String surveyQuery=    
    		 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    		 "prefix owl: <http://www.w3.org/2002/07/owl#> " +
			 "select ?class ?pred (count(*) as ?cnt) "+
			 "{ ?subj a ?class . ?subj ?pred ?obj . ?subj owl:sameAs ?dbp ." +
			 "  filter(strstarts(str(?dbp), \""+dbp+"\"))} " +
			 "group by ?class ?pred " +
			 "having (?cnt > 50) " +
			 "order by ?class ?cnt ";
    
	public String doProbe(HttpServletRequest request) {
		String source= request.getParameter("source");
		String target= request.getParameter("target");
		String klass= request.getParameter("klass");
		Integer sampleSize= Integer.valueOf(request.getParameter("sampleSize"));
		Integer classSize= Integer.valueOf(request.getParameter("classSize"));
		Integer offset= getOffset(sampleSize, classSize);
		String sampleQuery= getSampleQuery(klass, offset);
		ArrayList<String> samples= getSamples(sampleQuery, source);
		String probeQuery= getProbeQuery(samples);		
		return getProbeResults(probeQuery, target);		
	}     
	@SuppressWarnings("unchecked")
	public String doRemoteSurvey(HttpServletRequest request) {
    	String endpoint= request.getParameter("endpoint");
    	QueryExecution qe= QueryExecutionFactory.sparqlService(endpoint, surveyQuery);
    	ResultSet resultSet= qe.execSelect();
		Model resultGraph= ResultSetFormatter.toModel(resultSet);
		JSONObject result= new JSONObject();
		Surveyor surveyor= new Surveyor();
		result.put("classCount", surveyor.getClassCount(resultGraph));
		result.put("propCount", surveyor.getPropertyCount(resultGraph));
		result.put("classList", surveyor.getClassList(resultGraph));
		result.put("predicateMap", surveyor.getPredicateMap(resultGraph));
		return result.toJSONString();
    }
	private Integer getOffset(Integer sampleSize, Integer classSize) {
		Integer offset= 0;
		Integer difference= classSize - sampleSize;		
		if (difference > 0) { offset= (int) Math.random() * (difference + 1); }; 
		return offset;
	}
	private String getSampleQuery(String klass, Integer offset) {
		String dbp= "http://dbpedia.org/resource/";
		return  "prefix owl: <http://www.w3.org/2002/07/owl#> " +
				"select ?s " +
				"{ ?x a <" + klass + "> . ?x owl:sameAs ?s . " +
				"  filter(strstarts(str(?s), \""+dbp+"\"))} " +
				"limit 10   offset " + offset; 
	}	
	private String getProbeQuery(ArrayList<String> samples) {
		String sampleString= StringUtils.join(samples, "||");						
		return  "select ?p (count(*) as ?cnt) " +
				"{ ?s ?p ?o . filter ("+sampleString+") } " +
				"group by ?p";
	}
	private ArrayList<String> getSamples(String sampleQuery, String source) {
		ResultSet rs= QueryExecutionFactory.sparqlService(source, sampleQuery).execSelect();
		ArrayList<String> samples= new ArrayList<String>();
		while (rs.hasNext()) {						
			QuerySolution result= rs.next();
			try { String sample= result.get("s").toString();
				samples.add(" ?s = <"+sample+"> "); }					
			catch (NullPointerException e) { }						
		}
		return samples;
	}
	private String getProbeResults(String probeQuery, String target) {
		ResultSet rs= QueryExecutionFactory.sparqlService(target, probeQuery).execSelect();
		Surveyor surveyor= new Surveyor();
		return surveyor.getResults(rs);
	}
	
}
