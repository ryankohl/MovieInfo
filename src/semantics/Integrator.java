package semantics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.json.simple.JSONArray;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

public class Integrator {
		public int addOntologyFile(HttpServletRequest request) {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				return uploadFile("resources/ontology", request);				
			} else { return 500; }
		}
		public int addRulesFile(HttpServletRequest request) {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				return uploadFile("resources/rules", request);				
			} else { return 500; }
		}
		public int deleteOntologyFile(HttpServletRequest request) {
			String filename= request.getParameter("filename");
			String dirName= request.getServletContext().getRealPath("resources/ontology");
			return deleteFile(dirName, filename);
		}
		public int deleteRulesFile(HttpServletRequest request) {
			String filename= request.getParameter("filename");
			String dirName= request.getServletContext().getRealPath("resources/rules");
			return deleteFile(dirName, filename);
		}
		private int deleteFile(String directory, String filename) {			
			File file= new File(directory, filename);
			Boolean success= file.delete();
			if (success) { return 200; }
			else { return 500; }			
		}
		private int uploadFile(String directory, HttpServletRequest request) {
			ServletFileUpload upload = new ServletFileUpload();
			int result= 200;
			try {
				FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					String dirName= request.getServletContext().getRealPath(directory);	
					String fileName = item.getName();
					if (fileName != null) {
						File uploadedFile = new File(dirName, fileName);
						InputStream is= item.openStream();
						FileOutputStream os= new FileOutputStream(uploadedFile);				    
						Streams.copy(is, os, true);
					}
				}
			} 
			catch (FileUploadException e) { result= 500; } 
			catch (IOException e) { result= 500; }
			return result;
		}
		public String survey(HttpServletRequest request) 
				throws FileNotFoundException {				
			InfModel model= getGraph(request);			
			return this.runSurvey(model);
		}
		public String query(HttpServletRequest request) 
				throws FileNotFoundException { 			
			String query= request.getParameter("query");
			InfModel model= getGraph(request);
			return new Surveyor().doQuery(model, query);
		}
		public String getDataFiles(HttpServletRequest request) {			
			return getDirectoryFiles("resources/rdf", request);
		}
		public String getOntologyFiles(HttpServletRequest request) {			
			return getDirectoryFiles("resources/ontology", request);
		}
		public String getRulesFiles(HttpServletRequest request) {
			return getDirectoryFiles("resources/rules", request);
		}
		@SuppressWarnings("unchecked")
		private String getDirectoryFiles(String directory, HttpServletRequest request) {			
			JSONArray result= new JSONArray();
			File dir= new File(request.getServletContext().getRealPath(directory));					
			for (File file : dir.listFiles()) {
				if (file.isFile() && !(file.isHidden())) {
					result.add(file.getName());					
				}
			}
			return result.toJSONString();
		}
		public InfModel getGraph(HttpServletRequest request) 
				throws FileNotFoundException {
			Model model= this.readDirectory("resources/rdf", request);
			Resource config= model.createResource().addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");			
			model.add(this.getOntologyGraph(request)); 
			setRules(config, request); 
			Reasoner reasoner= GenericRuleReasonerFactory.theInstance().create(config);
			return ModelFactory.createInfModel(reasoner, model);				
		}				
		private Model getOntologyGraph(HttpServletRequest request) 
				throws FileNotFoundException {
			return this.readDirectory("resources/ontology", request);			
		}
		// ADD ITERATOR FOR EACH RULES FILE IN DIRECTORY
		private void setRules(Resource config, HttpServletRequest request) {
			File dir= new File(request.getServletContext().getRealPath("resources/rules"));
			for (File file : dir.listFiles()) {
				if (file.isFile() && !(file.isHidden())) {
					config.addProperty(ReasonerVocabulary.PROPruleSet, file.getPath());		
				}
			}						
		}		
		private Model readDirectory(String directory, HttpServletRequest request) 
				throws FileNotFoundException {
			Model model= ModelFactory.createDefaultModel();
			File dir= new File(request.getServletContext().getRealPath(directory));			 
			for (File file : dir.listFiles()) {
				if (file.isFile() && !(file.isHidden())) {
					String lang= FileUtils.guessLang(file.getName());					
					model.read(new FileInputStream(file), "", lang);
				}
			}
			return model;
		}
		private String runSurvey(Model model) {
			Surveyor surveyor= new Surveyor();
			return surveyor.doSurvey(model);		
		}
		
}
