package persistence;

import org.jdom.Element;

/**
 * The <code>ObjectWriter</code> interface must be implemented by object writers
 * (serializers). It defines the <code>write</code> method, which takes a Java object
 * and converts it into a JDOM element.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version ObjectWriter.java - 1.0
 */
public interface ObjectWriter extends Serial {

    public Element write(Object o);
}
