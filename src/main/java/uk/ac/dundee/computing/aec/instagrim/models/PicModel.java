package uk.ac.dundee.computing.aec.instagrim.models;

/*
Importing all the libraries and files that are used in this file.
 */
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.Bytes;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import static org.imgscalr.Scalr.*;
import org.imgscalr.Scalr.Method;

import uk.ac.dundee.computing.aec.instagrim.lib.*;
import uk.ac.dundee.computing.aec.instagrim.stores.Pic;

//class
public class PicModel {

    Cluster cluster;
    //constructor
    public void PicModel() {

    }
    //setting up the cluster
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
    
    //Method that inserts picture into database
    public void insertPic(byte[] b, String type, String name, String user, String tag, String comment) {
        try {
            Convertors convertor = new Convertors();
            
            String types[]=Convertors.SplitFiletype(type);
            ByteBuffer buffer = ByteBuffer.wrap(b);
            int length = b.length;
            java.util.UUID picid = convertor.getTimeUUID();
           
            //The following is a quick and dirty way of doing this, will fill the disk quickly !
            
           
            Boolean success = (new File("/var/tmp/instagrim/")).mkdirs();
            FileOutputStream output = new FileOutputStream(new File("/var/tmp/instagrim/" + picid));
            
            
            output.write(b);
            byte []  thumbb = picresize(picid.toString(),types[1]);
            int thumblength= thumbb.length;
            ByteBuffer thumbbuf=ByteBuffer.wrap(thumbb);
            byte[] processedb = picdecolour(picid.toString(),types[1]);
            ByteBuffer processedbuf=ByteBuffer.wrap(processedb);
            int processedlength=processedb.length;
            
            //starting session, connecting to the database.
            Session session = cluster.connect("instagrim");
            
            //Prepare statements that will be executed when the method is ran.
            PreparedStatement psInsertTagsToPics = session.prepare("insert into tag ( tag, picid) values(?,?)");
            PreparedStatement psInsertPic = session.prepare("insert into pics( picid, image,thumb,processed, user, interaction_time,imagelength,thumblength, processedlength,type,name, tag) values(?,?,?,?,?,?,?,?,?,?,?,?)");
            PreparedStatement psInsertPicToUser = session.prepare("insert into userpiclist ( picid, user, pic_added) values(?,?,?)");
            PreparedStatement psInsertComments = session.prepare("insert into comments ( picid, comment) values(?,?)");
            
            //Binding Statements so that they can be used in execute.
            BoundStatement bsInsertPic = new BoundStatement(psInsertPic);
            BoundStatement bsInsertPicToUser = new BoundStatement(psInsertPicToUser);
            BoundStatement bsInsertTagsToPics = new BoundStatement(psInsertTagsToPics);
            BoundStatement bsInsertComments = new BoundStatement(psInsertComments);
            
            //Getting current date.
            Date DateAdded = new Date();
            comment = "This was uploaded on: " + DateAdded;
            
            //Executing statements (adding parameters into database).
            session.execute(bsInsertPic.bind(picid, buffer, thumbbuf, processedbuf, user, DateAdded, length, thumblength, processedlength, type, name, tag));
            session.execute(bsInsertPicToUser.bind(picid, user, DateAdded));
            session.execute(bsInsertTagsToPics.bind(tag, picid));
            session.execute(bsInsertComments.bind(picid, comment));
            
            session.close();

        } catch (IOException ex) {
            System.out.println("Error --> " + ex);
        }
    }
    
    //Method that inserts comment for picture into database.
    public void InsertComment(String comment) {
        try {
            Convertors convertor = new Convertors();

           
          
            //
            java.util.UUID picid = convertor.getTimeUUID();
           
            //The following is a quick and dirty way of doing this, will fill the disk quickly !
            Boolean success = (new File("/var/tmp/instagrim/")).mkdirs();
            FileOutputStream output = new FileOutputStream(new File("/var/tmp/instagrim/" + picid));
            
          
            //connection to the database
            Session session = cluster.connect("instagrim");
            
            //preparing statement to execute
            PreparedStatement psInsertComments = session.prepare("insert into comments ( picid, comment) values(?,?)");
          
            //binding statement so that it can be executed
            BoundStatement bsInsertComments = new BoundStatement(psInsertComments);
           
            Date DateAdded = new Date();
            
          
            //executing statement
            session.execute(bsInsertComments.bind(picid, comment));
            //closing session
            session.close();

        } catch (IOException ex) {
            System.out.println("Error --> " + ex);
        }
    }

    //not working method, its supposed to insert profile picture in the table userprofiles
    public void InsertProfilePic(){
        try {
            
            Convertors convertor = new Convertors();
            java.util.UUID picid = convertor.getTimeUUID();
           
            //The following is a quick and dirty way of doing this, will fill the disk quickly !
            Boolean success = (new File("/var/tmp/instagrim/")).mkdirs();
            FileOutputStream output = new FileOutputStream(new File("/var/tmp/instagrim/" + picid));
            
            Session session = cluster.connect("instagrim");
            
            PreparedStatement psInsertProfilePic = session.prepare("insert into userprofiles ( picid) values(?)");
            
            BoundStatement bsInsertProfilePic = new BoundStatement(psInsertProfilePic);
            Date DateAdded = new Date();
            
            session.execute(bsInsertProfilePic.bind(picid));           
            session.close();

        } catch (IOException ex) {
            System.out.println("Error --> " + ex);
        }
    }
    
    
    //takes the picture id and type and r
    public byte[] picresize(String picid,String type) {
        try {
            BufferedImage BI = ImageIO.read(new File("/var/tmp/instagrim/" + picid));
            BufferedImage thumbnail = createThumbnail(BI);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, type, baos);
            //Flushes this output stream and forces any buffered output bytes to be written out.
            baos.flush();
            
            byte[] imageInByte = baos.toByteArray();
            //Closes this output stream and releases any system resources associated with this stream. 
            baos.close();
            return imageInByte;
        } catch (IOException et) {

        }
        return null;
    }
    
    public byte[] picdecolour(String picid,String type) {
        try {
            BufferedImage BI = ImageIO.read(new File("/var/tmp/instagrim/" + picid));
            BufferedImage processed = createProcessed(BI);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processed, type, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException et) {

        }
        return null;
    }
    //Creates the thumbnail for the image ( takes buffered image ) 
    public static BufferedImage createThumbnail(BufferedImage img) {
        img = resize(img, Method.SPEED, 250, OP_ANTIALIAS); //OP_GRAYSCALE
        // Let's add a little border before we return result.
        return pad(img, 2);
    }
    //Creates processed image ( takes buffered image ) 
    public static BufferedImage createProcessed(BufferedImage img) {
        int Width=img.getWidth()-1;
        img = resize(img, Method.SPEED, Width, OP_ANTIALIAS); //, OP_GRAYSCALE
        return pad(img, 4);
    }
    //Method that select the pictures for the user by making a query in the databse and pulling all the picid's for the user that is given to the method.
    public java.util.LinkedList<Pic> getPicsForUser(String User) {
        // Creates a instance of a linked list
        java.util.LinkedList<Pic> Pics = new java.util.LinkedList<>();
        // Connects to the database.
        Session session = cluster.connect("instagrim");
        //prepares a statement to be executed
        PreparedStatement ps = session.prepare("select picid from userpiclist where user =?");
        //Makes a results set and sets it to null;
        ResultSet rs = null;
        //Binds statement 
        BoundStatement boundStatement = new BoundStatement(ps);
        // this is where the query is executed
        rs = session.execute( 
                // here you are binding the boundStatement
                boundStatement.bind( 
                                    User));
        //If statement that checks if there are images in the query, returns null if not.
        if (rs.isExhausted()) {
            System.out.println("No Images returned");
            return null;
        //Executes the query if it's not exhausted ( empty ) 
        } else {
            //for loop that goes through every row.
            for (Row row : rs) {
                // Creates a new node in the linked list
                Pic pic = new Pic();
                // Gets the picid for every row.
                java.util.UUID UUID = row.getUUID("picid");
                // Prints it out in the console.
                System.out.println("UUID" + UUID.toString());
                //sets the picid to the node.
                pic.setUUID(UUID);
                //adds the node to the linked list
                Pics.add(pic);

            }
        }
        
        //returns the linked list so that it can be printed in the jsp page.
        return Pics;
    }
    
    
     //Method that select the pictures for all the users by making a query in the databse and pulling all the picid's.
     public java.util.LinkedList<Pic> getPicsForUsers() {
         
        java.util.LinkedList<Pic> Pics = new java.util.LinkedList<>();
        Session session = cluster.connect("instagrim");
        PreparedStatement ps = session.prepare("select picid from pics");
        ResultSet rs = null;
        BoundStatement boundStatement = new BoundStatement(ps);
        rs = session.execute( // this is where the query is executed
                boundStatement.bind( // here you are binding the 'boundStatement'
                        ));
        if (rs.isExhausted()) {
            System.out.println("No Images returned");
            return null;
        } else {
            for (Row row : rs) {
                
                Pic pic = new Pic();
                java.util.UUID UUID = row.getUUID("picid");
                System.out.println("UUID" + UUID.toString());
                pic.setUUID(UUID);
                Pics.add(pic);

            }
        }
        return Pics;
    }
     
      
        //Method that finds the id of the picture being clicked and pulls it out of the database.
        public java.util.LinkedList<Pic> GetClickedPickture(java.util.UUID picid) {
           
        java.util.LinkedList<Pic> Pics = new java.util.LinkedList<>();
        Session session = cluster.connect("instagrim");
        PreparedStatement ps = session.prepare("select * from pics where picid =?");
        ResultSet rs = null;
         System.out.println("No Images returned FAGGY1");
        BoundStatement boundStatement = new BoundStatement(ps);
         
        rs = session.execute( // this is where the query is executed
                boundStatement.bind( // here you are binding the 'boundStatement'
                        picid));
         
        if (rs.isExhausted()) {
           
            return null;
        } else {
            for (Row row : rs) {
                Pic pic = new Pic();
                java.util.UUID UUID = row.getUUID("picid");
                System.out.println("UUID" + UUID.toString());
                pic.setUUID(UUID);
                Pics.add(pic);

            }
        }
       
        return Pics;
    }
         //Method that selects all the comments for the picture that has been clicked or selected.
         public java.util.LinkedList<Pic> GetAllComments(java.util.UUID picid) {
           
        java.util.LinkedList<Pic> comment = new java.util.LinkedList<>();
        Session session = cluster.connect("instagrim");
        PreparedStatement ps = session.prepare("select * from comments where picid =?");
        ResultSet rs = null;
         System.out.println("No comments returned FAGGY1");
        BoundStatement boundStatement = new BoundStatement(ps);
         
        rs = session.execute( // this is where the query is executed
                boundStatement.bind( // here you are binding the 'boundStatement'
                        picid));
         
        if (rs.isExhausted()) {
           
            return null;
        } else {
            for (Row row : rs) {
                Pic pic = new Pic();
                java.util.UUID UUID = row.getUUID("picid");
                String comment1 = row.getString("comment");
                System.out.println("UUID" + UUID.toString());
                System.out.println(comment1);
                pic.setComments(comment1);
                comment.add(pic);

            }
        }
       
        return comment;
    }
         
       //Method that gets all the pictures with the a tag that the user has entered in the search field.
       public java.util.LinkedList<Pic> GetSearchTagPics(String Tag) {
        java.util.LinkedList<Pic> Pics = new java.util.LinkedList<>();
        Session session = cluster.connect("instagrim");
        PreparedStatement ps = session.prepare("select picid from tag where tag =?");
        ResultSet rs = null;
        BoundStatement boundStatement = new BoundStatement(ps);
        rs = session.execute( // this is where the query is executed
                boundStatement.bind( // here you are binding the 'boundStatement'
                      Tag));
        if (rs.isExhausted()) {
            System.out.println("No Images returned");
            return null;
        } else {
            for (Row row : rs) {
                Pic pic = new Pic();
                java.util.UUID UUID = row.getUUID("picid");
                System.out.println("UUID" + UUID.toString());
                pic.setUUID(UUID);
                Pics.add(pic);

            }
        }
        return Pics;
    }
    
    //Method to get the picture when displaying the image.
    public Pic getPic(int image_type, java.util.UUID picid) {
        Session session = cluster.connect("instagrim");
        ByteBuffer bImage = null;
        String type = null;
        int length = 0;
        try {
            Convertors convertor = new Convertors();
            ResultSet rs = null;
            PreparedStatement ps = null;
         
            if (image_type == Convertors.DISPLAY_IMAGE) {
                
                ps = session.prepare("select image,imagelength,type from pics where picid =?");
            } else if (image_type == Convertors.DISPLAY_THUMB) {
                ps = session.prepare("select thumb,imagelength,thumblength,type from pics where picid =?");
            } else if (image_type == Convertors.DISPLAY_PROCESSED) {
                ps = session.prepare("select processed,processedlength,type from pics where picid =?");
            }
            BoundStatement boundStatement = new BoundStatement(ps);
            rs = session.execute( // this is where the query is executed
                    boundStatement.bind( // here you are binding the 'boundStatement'
                            picid));

            if (rs.isExhausted()) {
                System.out.println("No Images returned");
                return null;
            } else {
                for (Row row : rs) {
                    if (image_type == Convertors.DISPLAY_IMAGE) {
                        bImage = row.getBytes("image");
                        length = row.getInt("imagelength");
                    } else if (image_type == Convertors.DISPLAY_THUMB) {
                        bImage = row.getBytes("thumb");
                        length = row.getInt("thumblength");
                
                    } else if (image_type == Convertors.DISPLAY_PROCESSED) {
                        bImage = row.getBytes("processed");
                        length = row.getInt("processedlength");
                    }
                    
                    type = row.getString("type");

                }
            }
        } catch (Exception et) {
            System.out.println("Can't get Pic" + et);
            return null;
        }
        session.close();
        Pic p = new Pic();
        p.setPic(bImage, length, type);

        return p;

    }

}
