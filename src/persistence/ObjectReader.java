package persistence;

import org.jdom.Element;

/**
 * The <code>ObjectReader</code> interface must be implemented by object readers
 * (de-serializers). It defines the <code>read</code> method, which takes a
 * JDOM element, and returns the live object.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version ObjectReader.java - 1.0
 */
public interface ObjectReader extends Serial {

    public Object read(Element xob);
}
