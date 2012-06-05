package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import semantics.Prober;
 
public class Probe extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Probe() { super(); }  
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {  		
		String type= request.getParameter("type");
		PrintWriter out= response.getWriter();
		Prober prober= new Prober();
		if (type.equalsIgnoreCase("probe")) { out.write(prober.doProbe(request)); }
		else if (type.equalsIgnoreCase("survey")) { out.write(prober.doRemoteSurvey(request)); }
		out.flush();
	}
}
