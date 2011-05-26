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

import jeeves.utils.Xml;

import org.jdom.Document;
import org.jdom.Element;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;;

/**
 * 
 * @author Jean-Pascal Boignard
 *
 * TODO Handle cache properties
 */
public class JeevletRepresentation extends OutputRepresentation {

	protected Element response = null;
	
	@Override
	public void write(OutputStream arg0) throws IOException {
		Xml.writeResponse(new Document(response), arg0);
		arg0.flush();
	}

	public JeevletRepresentation(MediaType mediaType, long expectedSize) {
		super(mediaType, expectedSize);
	}
	
	public JeevletRepresentation(Element element, MediaType mediaType, long expectedSize) {
		super(mediaType, expectedSize);
		this.response = element;
	}

	public JeevletRepresentation(MediaType mediaType) {
		super(mediaType);
	}

	public JeevletRepresentation(Element element, MediaType mediaType, boolean cache) {
		super(mediaType);
		this.response = element;
		// FIXME cache properties
	}
	
	//---------------------------------------------------------------------------

	public String getContentType()
	{
		return response.getAttributeValue("contentType");
	}

	//---------------------------------------------------------------------------

	public String getContentLength()
	{
		return response.getAttributeValue("contentLength");
	}

	//---------------------------------------------------------------------------

	public String getContentDisposition()
	{
		return response.getAttributeValue("contentDisposition");
	}

	//---------------------------------------------------------------------------

	public int getResponseCode()
	{
		return Integer.parseInt(response.getAttributeValue("responseCode"));
	}
	
}
