
import	java.io.*;
import java.util.ResourceBundle;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import framework.*;

public class HelloWorld extends HttpServlet {

   
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        
       
        ResourceBundle rb =
            ResourceBundle.getBundle("LocalStrings",request.getLocale());
        response.setContentType("text/html");
        LOG.logOff();

        
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");

	    String title = rb.getString("helloworld.title");

	    out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<h1>" + title + "</h1>");
        out.println("</body>");
        out.println("</html>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
        
        
        
        
    }
    
    
    
}


