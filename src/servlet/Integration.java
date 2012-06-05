package servlet; 

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import semantics.Integrator;

public class Integration extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public Integration() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {				
		String type= request.getParameter("type");
		PrintWriter out= response.getWriter();
		Integrator integrator= new Integrator();		
		
		if (type.equalsIgnoreCase("survey")) { out.write(integrator.survey(request)); }
		else if (type.equalsIgnoreCase("query")) { out.write(integrator.query(request)); }
		else if (type.equalsIgnoreCase("data")) { out.write(integrator.getDataFiles(request)); }
		else if (type.equalsIgnoreCase("ontologies")) { out.write(integrator.getOntologyFiles(request)); }
		else if (type.equalsIgnoreCase("rules")) {out.write(integrator.getRulesFiles(request)); }
		
		out.flush();
		out.close();
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		String type= request.getParameter("type");
		Integrator integrator= new Integrator();
		
		if (type.equalsIgnoreCase("addOntology")) {response.setStatus(integrator.addOntologyFile(request)); }
		else if (type.equalsIgnoreCase("addRule")) {response.setStatus(integrator.addRulesFile(request)); }
		else if (type.equalsIgnoreCase("deleteOntology")) {response.setStatus(integrator.deleteOntologyFile(request)); }
		else if (type.equalsIgnoreCase("deleteRule")) {response.setStatus(integrator.deleteRulesFile(request)); }
	}		
}
