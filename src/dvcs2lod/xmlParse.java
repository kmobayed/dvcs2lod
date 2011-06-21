package dvcs2lod;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class xmlParse {


    Document dom;
    private ArrayList<ChangeSet> myChangeSets;


    public xmlParse(){
            myChangeSets = new ArrayList();
    }

    public void parseXmlFile(InputStream InStr){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();

        dom = db.parse(InStr);
        } catch (SAXException ex) {
            Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
                Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("log");
        if(nl != null && nl.getLength() > 0) {
                for(int i = 0 ; i < nl.getLength();i++) {

                        Element el = (Element)nl.item(i);
                        addChangeSet(el);
                }
        }
    }

    private void addChangeSet(Element el){
        String id = getTextValue(el,"revisionid");
        String hashID="";
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(id.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            hashID = bigInt.toString(16);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
        }


        ChangeSet cs = new ChangeSet(hashID);
        String date =getTextValue(el,"timestamp");
        String dateFormatLog="yyyy-MM-dd HH:mm:ss Z";
        String dateFormatLogBazaar="E yyyy-MM-dd HH:mm:ss Z";
        Date D;
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormatLogBazaar);
            sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
            D = sdf1.parse(date);
            SimpleDateFormat sdf2= new SimpleDateFormat(dateFormatLog);
            sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = sdf2.format(D);
            cs.setDate(date);
        } catch (ParseException ex) {
            Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
        }

        cs.setAuthorEmail(getTextValue(el,"committer"));

        NodeList parents = el.getElementsByTagName("parent");
        if(parents != null && parents.getLength() > 0) {
            for(int i = 0 ; i < Math.min(2,parents.getLength());i++) {

            Element el1 = (Element)parents.item(i);
            String parentID = el1.getFirstChild().getNodeValue();
            try {
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(parentID.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            hashID = bigInt.toString(16);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
            }

            cs.addPreviousChgSet(hashID);

            }
        }
        myChangeSets.add(cs);

    }

    private String getTextValue(Element ele, String tagName) {
            String textVal = null;
            NodeList nl = ele.getElementsByTagName(tagName);
            if(nl != null && nl.getLength() > 0) {
                    Element el = (Element)nl.item(0);
                    textVal = el.getFirstChild().getNodeValue();
            }

            return textVal;
    }


    public void printData(){
            FileWriter outFile;
            try {
                outFile = new FileWriter("bzr.log.tmp");
                PrintWriter out = new PrintWriter(outFile);


            for(int i=0; i<myChangeSets.size();i++) {
                    out.println(myChangeSets.get(i).getChgSetID());
                    if (myChangeSets.get(i).getPreviousChgSet().size()>=2)
                    {
                        out.print(myChangeSets.get(i).getPreviousChgSet().get(0)+" ");
                        out.println(myChangeSets.get(i).getPreviousChgSet().get(1));
                    }
                    if (myChangeSets.get(i).getPreviousChgSet().size()==1)
                    {
                        out.println(myChangeSets.get(i).getPreviousChgSet().get(0)+" ");
                    }
                    if (myChangeSets.get(i).getPreviousChgSet().isEmpty())
                    {
                        out.println(" ");
                    }
                    out.println(myChangeSets.get(i).getDate());
                    out.println(myChangeSets.get(i).getAuthorEmail());
            }
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(xmlParse.class.getName()).log(Level.SEVERE, null, ex);
            }
    }


}
