package servlet; 

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import semantics.Extractor;

public class Extraction extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public Extraction() { super(); } 
    // return JSON with the ?s ?p ?o extent of the requested model (via Construct query)
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException { 		
		String type= request.getParameter("type");
		PrintWriter out= response.getWriter();
		Extractor extractor= new Extractor();
		if (type.equalsIgnoreCase("query")) { out.write(extractor.extract(request)); }
		else if (type.equalsIgnoreCase("constructs")) { out.write(extractor.getSavedConstructs(request)); }
		out.flush();
	}
	// save the requested model and its Construct query to file
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String type= request.getParameter("type");
		PrintWriter out= response.getWriter();
		Extractor extractor= new Extractor();
		if (type.equalsIgnoreCase("delete")) { out.write(extractor.delete(request)); }
		else if (type.equalsIgnoreCase("save")){ out.write(extractor.save(request)); }
		out.flush();
	}	
}
