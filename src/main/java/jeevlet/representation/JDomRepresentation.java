//=============================================================================
//===	Copyright (C) 2010-2011 BRGM
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeevlet.representation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeves.utils.Xml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

public class JDomRepresentation extends JeevletRepresentation {
	private Document document;

	/**
	 * Use "application/xml; charset=UTF-8" by default
	 * @param element
	 * @param cache
	 */
	public JDomRepresentation(Element element, boolean cache){
		super(element, MediaType.APPLICATION_XML, cache);
		setCharacterSet(CharacterSet.UTF_8);
		document = new Document(element);
	}
	
	public JDomRepresentation(Element element, MediaType mediaType, boolean cache){
		super(element, mediaType, cache);
		document = new Document(element);
	}	
	
	public JDomRepresentation(MediaType mediaType) {
		super(mediaType);
	}

	public JDomRepresentation(MediaType mediaType, Document document) {
		super(mediaType);
		this.document = document;
	}

	public JDomRepresentation(MediaType mediaType, Representation representation)
			throws JDOMException, IOException {
		super(mediaType);
		this.document = new Document(Xml.loadStream(representation.getStream()));
	}

	public String valueOf(String locationPath, Map namespaces)
			throws JDOMException, IOException {
		XPath xpath = XPath.newInstance(locationPath);
		addNamespaces(xpath, namespaces);
		return xpath.valueOf(getDocument());
	}

	public Number numberValueOf(String locationPath, Map namespaces)
			throws JDOMException, IOException {
		XPath xpath = XPath.newInstance(locationPath);
		addNamespaces(xpath, namespaces);
		return xpath.numberValueOf(getDocument());
	}

	public Integer intValueOf(String locationPath, Map namespaces)
			throws JDOMException, IOException {
		XPath xpath = XPath.newInstance(locationPath);
		addNamespaces(xpath, namespaces);
		Number o = xpath.numberValueOf(getDocument());
		if (o == null)
			return null;
		return new Integer(o.intValue());
	}

	public Object selectSingleNode(String locationPath, Map namespaces)
			throws JDOMException, IOException {
		XPath xpath = XPath.newInstance(locationPath);
		addNamespaces(xpath, namespaces);
		return xpath.selectSingleNode(getDocument());
	}

	public List selectNodes(String locationPath, Map namespaces)
			throws JDOMException, IOException {
		XPath xpath = XPath.newInstance(locationPath);
		addNamespaces(xpath, namespaces);
		return xpath.selectNodes(getDocument());
	}

	public Document getDocument() throws IOException {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void write(OutputStream outputStream) throws IOException {
//		Format format = Format.getRawFormat();
//		// Line separator is normally \r\n which causes newlines to grow when we
//		// parse this content again.
//		format.setLineSeparator("\n");
//		XMLOutputter outputter = new XMLOutputter(format);
//		outputter.output(getDocument(), outputStream);
		
		Xml.writeResponse(getDocument(), outputStream);
		outputStream.flush();
	}

	protected void addNamespaces(XPath xpath, Map namespaces) {
		Iterator it = namespaces.keySet().iterator();
		while (it.hasNext()) {
			String prefix = ((String) (it.next()));
			String uri = (String) (namespaces.get(prefix));
			xpath.addNamespace(prefix, uri);
		}
	}

}
