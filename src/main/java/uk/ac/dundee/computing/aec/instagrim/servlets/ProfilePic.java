package uk.ac.dundee.computing.aec.instagrim.servlets;

import com.datastax.driver.core.Cluster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import uk.ac.dundee.computing.aec.instagrim.lib.CassandraHosts;
import uk.ac.dundee.computing.aec.instagrim.lib.Convertors;
import uk.ac.dundee.computing.aec.instagrim.models.PicModel;
import uk.ac.dundee.computing.aec.instagrim.models.User;
import uk.ac.dundee.computing.aec.instagrim.stores.LoggedIn;
import uk.ac.dundee.computing.aec.instagrim.stores.Pic;



@WebServlet(name = "ProfilePic", urlPatterns = ("/ProfilePic/*"))
  
    //"/SingleImage/*"
   // "/SingleImageComments"
    //
//})


public class ProfilePic extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Cluster cluster;
    


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProfilePic() {
         
        // TODO Auto-generated constructor stub
     
       // CommandsMap.put("SingleImageComments", 2);
      
        
       

    }

    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        cluster = CassandraHosts.getCluster();
    }



   
  

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
                PicModel tm = new PicModel();
                tm.setCluster(cluster);
                tm.InsertProfilePic();
                response.sendRedirect("/Instagrim");
            
     
        
        
     
    }
    
    
     
   
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
        private void error(String mess, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = null;
        out = new PrintWriter(response.getOutputStream());
        out.println("<h1>You have a na error in your input</h1>");
        out.println("<h2>" + mess + "</h2>");
        out.close();
        return;
    }
        
       
}


