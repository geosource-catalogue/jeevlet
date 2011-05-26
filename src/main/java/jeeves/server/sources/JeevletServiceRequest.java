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
package jeeves.server.sources;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import jeeves.constants.Jeeves;

import org.jdom.Element;
import org.restlet.Response;
import org.restlet.representation.Representation;

//=============================================================================

/** A ServiceRequest is a generic request for a service
  */

public class JeevletServiceRequest extends ServiceRequest 
{
	//---------------------------------------------------------------------------

	protected String       service   = null;
	protected String       language  = null;
	protected Element      params    = new Element(Jeeves.Elem.REQUEST);
	protected boolean      debug     = false;
	protected OutputStream outStream = null;
	protected String       address   = "0.0.0.0";
	protected int          statusCode= 200;
	protected InputMethod  input     = InputMethod.GET;
	protected OutputMethod output    = OutputMethod.DEFAULT;
	private Map<String, String> headers = new HashMap<String, String>();
		
	protected Response     response = null;
	protected Representation representation = null;

	//---------------------------------------------------------------------------

	public Representation getRepresentation() {
		return representation;
	}

	public void setRepresentation(Representation representation) {
		this.representation = representation;
	}

	public JeevletServiceRequest() {}
	
	public JeevletServiceRequest(Response response) {
		this.response = response;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API
	//---
	//---------------------------------------------------------------------------

	/** Name of the requested service */

	public String getService() { return service; }

	//---------------------------------------------------------------------------
	/** requesting language (eg. 'en') */

	public String getLanguage() { return language; }

	//---------------------------------------------------------------------------
	/** requesting parameters*/

	public Element getParams() { return params; }

	//---------------------------------------------------------------------------
	/**
	 * @return Map of the request headers
	 */
	public Map<String, String> getHeaders() {	return headers; }

	//---------------------------------------------------------------------------
	/** true if the request has the debug option turned on */

	public boolean hasDebug() { return debug; }

	//---------------------------------------------------------------------------
	/** gets the output stream of this request to output data */

	public OutputStream getOutputStream() { return outStream; }

	//---------------------------------------------------------------------------
	/** gets the ip address of the request (if any) */

	public String getAddress() { return address; }

	//---------------------------------------------------------------------------

	public InputMethod  getInputMethod()  { return input;  }
	public OutputMethod getOutputMethod() { return output; }

	//---------------------------------------------------------------------------

	public void setService (String  newService) { service  = newService; }
	public void setLanguage(String  newLang)    { language = newLang;    }
	public void setParams  (Element newParams)  { params   = newParams;  }
	public void setHeaders (Map<String, String> newHeaders) { headers = newHeaders; }

	public void setAddress (String  newAddress) { address  = newAddress; }

	public void setStatusCode(int code) { statusCode = code; }

	public void setInputMethod (InputMethod m)  { input  = m; }
	public void setOutputMethod(OutputMethod m) { output = m; }

	//---------------------------------------------------------------------------

	public void setOutputStream(OutputStream os) { outStream = os; }

	//---------------------------------------------------------------------------

	public void setDebug(boolean yesno) { debug = yesno; }

	//---------------------------------------------------------------------------

//	public void write(Element response) throws IOException {}
//	
//	//---------------------------------------------------------------------------
//	/** called when the system starts streaming data */
//
//	public void beginStream(String contentType, String charset, boolean cache) {}
//
//	//---------------------------------------------------------------------------
//
//	public void beginStream(String contentType, String charset, int contentLength, String contentDisp, boolean cache) {}
//
//	//---------------------------------------------------------------------------
//	/** called when the system ends streaming data*/
//
//	public void endStream() throws IOException {}
	

	
	public Response getResponse() { return response; }
}

//=============================================================================

