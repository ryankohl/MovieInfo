package servlet;

import java.io.IOException; 
import java.io.PrintWriter;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import semantics.Surveyor;

public class Survey extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Survey() { super(); }  
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {  		
		String type= request.getParameter("type");
		PrintWriter out= response.getWriter();
		Surveyor surveyer= new Surveyor();
		if (type.equalsIgnoreCase("survey")) { out.write(surveyer.doRemoteSurvey(request)); }
		else if (type.equalsIgnoreCase("query")) { out.write(surveyer.doRemoteQuery(request)); }
		out.flush();
	}
}
