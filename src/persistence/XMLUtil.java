package persistence;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * The <code>XMLUtil</code> class provides static methods that are used by
 * <code>SimpleReader</code> and <code>SimpleWriter</code>. The methods of
 * this class are used by the serialization and de-serialization processes.
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version XMLUtil.java - 1.0
 */
public class XMLUtil implements Serial {

    public static void disElement(Object object) throws Exception {
        XMLOutputter out = new XMLOutputter();
        Element xCons = new SimpleWriter().write(object);
        out.output(xCons, System.out);
    }

    public static void disElement(Element element) throws Exception {
        XMLOutputter out = new XMLOutputter();
        out.output(element, System.out);
    }


    public static String element2String(Element element) throws Exception {
        XMLOutputter out = new XMLOutputter();
        return out.outputString(element);
    }

    public static String element2BrowserString(Element element) throws Exception {
        XMLOutputter out = new XMLOutputter();
        String myString = out.outputString(element);
        myString = myString.replaceAll("<", "&lt;");
        myString = myString.replaceAll(">", "&gt;");
        return myString;
    }

    public static Element xmlToElement(String xmlPath) throws Exception {
        try {
            org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
            Document document = builder.build(new File(xmlPath));
            Element element = document.getRootElement();
            return element;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createFile(Element element, File filename) {
        try {
            FileWriter fileWrite = new FileWriter(filename);
            XMLOutputter fmt = new XMLOutputter();
            fmt.output(element, fileWrite);
            fileWrite.flush();
            fileWrite.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateRandomNo() {
        int id = 0;
        int range = Integer.MAX_VALUE / 3 * 2;
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            id = rand.nextInt(range);
        }
        return Integer.toString(id);
    }

    public static String convertXmlToString(String xmlFileName) throws Exception {
        File file = new File(xmlFileName);
        FileInputStream insr = new FileInputStream(file);
        byte[] fileBuffer = new byte[(int) file.length()];
        insr.read(fileBuffer);
        insr.close();
        return new String(fileBuffer);
    }

    public static String getXmlElementAsString(Element xmlElement) {
        XMLOutputter fmt = new XMLOutputter();
        return fmt.outputString(xmlElement);
    }
}
