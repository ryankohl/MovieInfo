package semantics;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class Analyzer {
	private String prefix= "prefix : <http://tutori.al/> ";
	private String itemsQ= prefix +
						  "select ?item " +
						  "{ ?item a ?class . ?class a :ClassOfInterest }";
	private String piesQ= prefix +
			"select ?pie " +
			"{ ?pie a :PieChartEventClass }";
	private String namesQ= prefix +
						   "select ?item ?name " +
						   "{ ?item :name ?name . ?item a ?class . ?class a :ClassOfInterest} ";
	private String identifyingQ= prefix +
			"select ?item ?prop ?val " + 
			"{ ?item ?prop ?val . ?prop a :IdentifyingProperty }";
	private String activityQ= prefix +
			"select ?item ?prop ?value" + 
			"{ ?item ?prop ?value . ?prop a :ActivityProp }";			
	private String associateQ= prefix +			
			"select ?item ?valProp ?val (count(?event) as ?cnt)" + 
			"{ ?event a ?class . ?class a :AssociateEventClass ." +			
			"  ?event ?actorProp ?item . ?actorProp a :EventActorProp . " +			
			"  ?event ?valProp ?val . ?valProp a :EventValueProp }" +
			"group by ?item ?prop ?val ?class ?valProp ?actorProp";						
	private String piechartQ= prefix +
			"select ?item ?kind ?label ?cnt " + 
			"{ ?event a ?kind . ?kind a :PieChartEventClass . " +			
			"  ?event ?actorProp ?item . ?actorProp a :EventActorProp . " +
			"  ?event ?labelProp ?label . ?labelProp a :EventLabelProp ." +
			"  ?event ?valueProp ?cnt . ?valueProp a :EventValueProp }";
	private String timelineQ= prefix +
			"select ?item ?val ?time" + 
			"{ ?event a ?class . ?class a :TimelineEventClass ." +
			"  ?event ?valProp ?val . ?event ?timeProp ?time . ?event ?actorProp ?item" +
			"  ?valProp a EventValueProp . " +
			"  ?timeProp a EventTimestampProp . " +
			"  ?actorProp a EventActorProp }";
	
	@SuppressWarnings("unchecked")
	public String get(HttpServletRequest request) 
			throws FileNotFoundException {		
		InfModel model= new Integrator().getGraph(request);
		JSONObject ids_names= getNames(model); 
		JSONObject result= new JSONObject();
		result.put("items", getItems(model));
		result.put("pies", getPies(model));
		result.put("ids", ids_names.get("ids"));
		result.put("names", ids_names.get("names"));
		result.put("identifying", getIdentifying(model));
		result.put("activity", getActivity(model));
		result.put("associate", getAssociate(model));
		result.put("piechart", getPiechart(model));
		// result.put("timeline", getTimeline(model));
		return result.toJSONString();
	}
	@SuppressWarnings("unchecked")
	private JSONArray getItems(Model model) {
		ResultSet rs= QueryExecutionFactory.create(itemsQ, model).execSelect();
		JSONArray answer= new JSONArray();
		while (rs.hasNext()) {
			String item= rs.next().get("item").toString();
			answer.add(item);			
		}		
		return answer;
	}
	@SuppressWarnings("unchecked")
	private JSONArray getPies(Model model) {
		ResultSet rs= QueryExecutionFactory.create(piesQ, model).execSelect();
		JSONArray answer= new JSONArray();
		while (rs.hasNext()) {
			String item= rs.next().get("pie").toString();
			answer.add(item);			
		}		
		return answer;
	}
	@SuppressWarnings("unchecked")
	private JSONObject getNames(Model model) {
		ResultSet rs= QueryExecutionFactory.create(namesQ, model).execSelect();
		JSONObject answers= new JSONObject();		
		JSONObject ids= new JSONObject();
		JSONObject names= new JSONObject();
		while (rs.hasNext()) {
			QuerySolution result= rs.next();
			String item= result.get("item").toString();
			String name= result.get("name").toString();			
			addFact(ids, item, "name", name);
			addFact(names, name, "id", item);
		}
		answers.put("ids", ids);
		answers.put("names", names);
		return answers;
	}
	private JSONObject getIdentifying(Model model) {
		ResultSet rs= QueryExecutionFactory.create(identifyingQ, model).execSelect();
		JSONObject answers= new JSONObject();
		while (rs.hasNext()) {	
			QuerySolution result= rs.next();
			String item= result.get("item").toString();
			String prop= result.get("prop").toString();
			String val= result.get("val").toString();
			addFact(answers, item, prop, val);
		}
		return answers;
	}
	private JSONObject getActivity(Model model) {
		ResultSet rs= QueryExecutionFactory.create(activityQ, model).execSelect();
		JSONObject answers= new JSONObject();
		try {
			while (rs.hasNext()) {	
				QuerySolution result= rs.next();
				String item= result.get("item").toString();
				String prop= result.get("prop").toString();			
				Integer value= result.get("value").asLiteral().getInt();			
				addFact(answers, item, prop, value);
			}
		} catch (NullPointerException e) {}
		return answers;
	}
	@SuppressWarnings("unchecked")
	private JSONObject getAssociate(Model model) {
		ResultSet rs= QueryExecutionFactory.create(associateQ, model).execSelect();
		JSONObject answers= new JSONObject();
		try {
			while (rs.hasNext()) {	
				QuerySolution result= rs.next();
				String item= result.get("item").toString();
				String valProp= result.get("valProp").toString();
				String val= result.get("val").toString();
				Integer cnt= result.get("cnt").asLiteral().getInt();
				JSONArray value= new JSONArray();
				value.add(val);
				value.add(cnt);
				addFact(answers, item, valProp, value);
			}
		} catch (NullPointerException e) {}
		return answers;
	}
	@SuppressWarnings("unchecked")
	private JSONObject getPiechart(Model model) {
		ResultSet rs= QueryExecutionFactory.create(piechartQ, model).execSelect();
		JSONObject answers= new JSONObject();
		while (rs.hasNext()) {	
			QuerySolution result= rs.next();
			String item= result.get("item").toString();
			String kind= result.get("kind").toString();
			String label= result.get("label").toString();
			Integer cnt= result.get("cnt").asLiteral().getInt();
			JSONArray value= new JSONArray();
			value.add(label);
			value.add(cnt);
			addFact(answers, item, kind, value);
		}
		return answers;
	}
	@SuppressWarnings({ "unchecked", "unused" })
	private JSONObject getTimeline(Model model) {
		ResultSet rs= QueryExecutionFactory.create(timelineQ, model).execSelect();
		JSONObject answers= new JSONObject();
		while (rs.hasNext()) {	
			QuerySolution result= rs.next();
			String item= result.get("item").toString();			
			String val= result.get("val").toString();
			String time= result.get("time").toString();
			JSONArray value= new JSONArray();
			value.add(time);
			value.add(val);
			addFact(answers, item, "value", value);
		}
		return answers;
	}
	@SuppressWarnings("unchecked")	
	private void addFact(JSONObject answers, String item, String prop, Object val) {
		JSONObject itemEntry;
		JSONArray propEntry;
		if (answers.containsKey(item)) {			
			itemEntry= (JSONObject) answers.get(item);				
			if (itemEntry.containsKey(prop)) { propEntry= (JSONArray) itemEntry.get(prop); }
			else { 
				propEntry= new JSONArray();
				itemEntry.put(prop, propEntry);
			}
			propEntry.add(val);	
		}
		else {			
			itemEntry= new JSONObject();
			propEntry= new JSONArray();
			propEntry.add(val);
			itemEntry.put(prop, propEntry);
			answers.put(item, itemEntry);			
		}		
	}
}
