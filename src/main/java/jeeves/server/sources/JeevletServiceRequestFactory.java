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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.constants.Jeeves;
import jeeves.exceptions.FileUploadTooBigEx;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeevlet.exception.JeevletException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;

//=============================================================================

public class JeevletServiceRequestFactory {

	// ---------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// ---------------------------------------------------------------------------
	/**
	 * Builds the request with data supplied by tomcat. A request is in the
	 * form: srv/<language>/<service>[!]<parameters>
	 */

	public static JeevletServiceRequest create(Request req, Response res,
			String uploadDir, int maxUploadSize) throws Exception {
		// String url = req.getPathInfo();
		String url = req.getResourceRef().getPath();

		// FIXME jeeves : if request character encoding is undefined set it to UTF-8
		if (req.getEntity() != null	&& req.getEntity().getCharacterSet() == null)
			req.getEntity().setCharacterSet(CharacterSet.UTF_8);

		// --- extract basic info
		
		JeevletServiceRequest srvReq = new JeevletServiceRequest(res);

		srvReq.setDebug(extractDebug(url));
		srvReq.setLanguage(extractLanguage(url));
		srvReq.setService(extractService(url));
		srvReq.setAddress(req.getClientInfo().getAddress()); // getRemoteAddr());
//		srvReq.setOutputStream(res.getOutputStream());

		// --- discover the input/output methods

		// Output content type negociation 
		List<Preference<MediaType>> accept = req.getClientInfo().getAcceptedMediaTypes();

		if (accept != null) {
			// TODO Use MediaType content negociation instead ? ( at least, needs MediaType for "application/soap+xml" )
			int soapNDX = accept.toString().indexOf("application/soap+xml");
			int xmlNDX = accept.toString().indexOf("application/xml");
			int htmlNDX = accept.toString().indexOf("html"); // peut �tre xhtml ...

			if (soapNDX != -1)
				srvReq.setOutputMethod(jeeves.server.sources.ServiceRequest.OutputMethod.SOAP);

			else if (xmlNDX != -1 && htmlNDX == -1)
				srvReq.setOutputMethod(jeeves.server.sources.ServiceRequest.OutputMethod.XML);
		}

		// Input POST request processing
		if ("POST".equals(req.getMethod().getName())) {
			srvReq.setInputMethod(jeeves.server.sources.ServiceRequest.InputMethod.POST);

			// FIXME posted entity[mediatype] can be null
			String contType = req.getEntity().getMediaType().getName(); // getContentType();

			if (contType != null) {
				if (contType.indexOf("application/soap+xml") != -1) {
					srvReq.setInputMethod(jeeves.server.sources.ServiceRequest.InputMethod.SOAP);
					srvReq.setOutputMethod(jeeves.server.sources.ServiceRequest.OutputMethod.SOAP);
				}

				else if (contType.indexOf("application/xml") != -1
						|| contType.indexOf("text/xml") != -1) {
					srvReq.setInputMethod(jeeves.server.sources.ServiceRequest.InputMethod.XML);
				}
			}
		}

		// --- retrieve input parameters

		jeeves.server.sources.ServiceRequest.InputMethod input = srvReq.getInputMethod();

		if ((input == jeeves.server.sources.ServiceRequest.InputMethod.XML)
				|| (input == jeeves.server.sources.ServiceRequest.InputMethod.SOAP)) {
			
			if ("GET".equals(req.getMethod().getName()))
				srvReq.setParams(extractParameters(req, uploadDir, maxUploadSize));
			else {
				Element params = extractXmlParameters(req);
				srvReq.setParams(params);
			}
		} else {
			// --- GET or POST
			Element params = extractParameters(req, uploadDir, maxUploadSize);
			srvReq.setParams(params);
		}

		srvReq.setHeaders(extractHeaders(req));
		
		return srvReq;
	}

	/**
	 * Build up a map of the HTTP headers.
	 * @param req The web request
	 * @return Map of header keys and values.
	 */
	private static Map<String, String> extractHeaders(Request req)
	{
		// FIXME headers (attributes) supposes <String, String> alors que <String, Object>
		Map<String, String> headerMap = new HashMap<String, String>(); 
		Set<String> e = req.getAttributes().keySet();
		for (String key : e) {
			headerMap.put(key, req.getAttributes().get(key).toString());
		}
		

		// FIXME The remote user needs to be saved as a header also
		if (req.getClientInfo().getUser() != null)
		{
			headerMap.put("REMOTE_USER", req.getClientInfo().getUser().toString());
		}
		return headerMap;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Input retrieving methods
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Extracts the debug option from the url
	 */

	private static boolean extractDebug(String url) {
		if (url == null)
			return false;

		return url.indexOf("!") != -1;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Extracts the language code from the url
	 */

	private static String extractLanguage(String url) {
		// FIXME JPB - extractLanguage  
		// url starts with "geosource-idx"  (did not)
		// so searching "/srv"  (to confirm : "srv" must not be changed) 
		if (url == null)
			return null;

		String myUrl = url.substring(1);

		int pos = myUrl.indexOf("/srv");

		if (pos == -1)
			return null;
		String result = myUrl.substring(pos + 5, pos + 8);
		return result;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Extracts the service name from the url
	 */

	private static String extractService(String url) {
		if (url == null)
			return null;

		if (url.endsWith("!"))
			url = url.substring(0, url.length() - 1);

		int pos = url.lastIndexOf("/");

		if (pos == -1)
			return null;

		return url.substring(pos + 1);
	}

	// ---------------------------------------------------------------------------

	private static Element extractXmlParameters(Request req)
			throws IOException, JDOMException {
		return Xml.loadStream(req.getEntity().getStream());
	}

	// ---------------------------------------------------------------------------

	private static Element extractParameters(Request req, String uploadDir, int maxUploadSize)
																throws Exception
		{
//		// --- set parameters from multipart request
//
//		if (ServletFileUpload.isMultipartContent(req))
//			return getMultipartParams(req, uploadDir, maxUploadSize);

//		TODO	if (RestletFileUpload.isMultipartContent(req)) can not be used , needed HttpRequest 
// 		TODO 	Apache File Upload , need Servlet  :-(   
		
		if (isMultipartContent(req))
			return getMultipartParams(req, uploadDir, maxUploadSize);
				
			Element params = new Element(Jeeves.Elem.REQUEST);

			//--- add parameters from POST request
			Form formQ = req.getResourceRef().getQueryAsForm();
			for (Parameter parameter : formQ) {
				params.addContent(new Element(parameter.getName()).setText(parameter.getValue()));
			}
			
			if (req.isEntityAvailable()){
			Form formE = new Form(req.getEntity());
			for (Parameter parameter : formE) {
				params.addContent(new Element(parameter.getName()).setText(parameter.getValue()));
			}
			}
			return params;
		}

	private static boolean isMultipartContent(Request req){
		Representation entity = req.getEntity();
		return  (entity != null  && 
				MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),	true)
				);
		// FIXME MULTIPART_ALL  multipart mixed ???
		/*if (MediaType.MULTIPART_ALL.equals(entity.getMediaType())
        || MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType())) {*/
	}

	private static Element getMonoPartParams(Request req, String uploadDir, int maxUploadSize) throws Exception {
		Element params = new Element("params");
		
		Representation entity = req.getEntity();
		String name = entity.getDisposition().getFilename();
		String type = entity.getDisposition().getType();
		
		File uploadedFile = new File(uploadDir, name);
		entity.write(new FileOutputStream(uploadedFile));
		
		long size = uploadedFile.length();
		
		Element elem = new Element(name).setAttribute("type", "file").setAttribute("size",
		Long.toString(size)).setText(name);
		//
		if (type != null)
			elem.setAttribute("content-type", type);

		params.addContent(elem);
		
		return params;
	}
	
	private static Element getMultipartParams(Request req, String uploadDir,
			int maxUploadSize) throws Exception {

		Element params = new Element("params");

	
		// FIXME FileUpload - confirm only "multipart/form-data" entities must be parsed here ...
		// if (entity != null && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {

		// The Apache FileUpload project parses HTTP requests which
		// conform to RFC 1867, "Form-based File Upload in HTML". That
		// is, if an HTTP request is submitted using the POST method,
		// and with a content type of "multipart/form-data", then
		// FileUpload can parse that request, and get all uploaded files
		// as FileItem.

		// 1/ Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1000240); // en m�moire
		factory.setRepository(new File(uploadDir));

		// 2/ Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(factory);
		
		upload.setFileSizeMax(maxUploadSize * 1024 * 1024);
		
		List<FileItem> items;
		try {
			items = upload.parseRequest(req);// parseRepresentation(req.getEntity());

			for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
				FileItem item = (FileItem) it.next();

				String name = item.getFieldName();

				if (item.isFormField())
					params.addContent(new Element(name).setText(item.getString()));
				else {
					String file = item.getName();
					String type = item.getContentType();
					long size = item.getSize();
					Log.debug(Log.REQUEST, "Uploading file "+file+" type: "+type+" size: "+size);
					//--- remove path information from file (some browsers put it, like IE)

					file = simplifyName(file);
					Log.debug(Log.REQUEST, "File is called "+file+" after simplification");

					//--- we could get troubles if 2 users upload files with the same name
					item.write(new File(uploadDir, file));

					Element elem = new Element(name)
											.setAttribute("type", "file")
											.setAttribute("size", Long.toString(size))
											.setText(file);

					if (type != null)
						elem.setAttribute("content-type", type);

					Log.debug(Log.REQUEST,"Adding to parameters: "+Xml.getString(elem));
					params.addContent(elem);
					}
				}
			}
		catch (FileUploadBase.FileSizeLimitExceededException e) {
			// throw jeeves exception --> reached code ? see apache docs -
			// FileUploadBase
			throw new FileUploadTooBigEx();
		} catch (FileUploadException e) {
			// Sample Restlet ... " 
			
			// The message of all thrown exception is sent back to client as simple plain text
			// response.setEntity(new StringRepresentation(e.getMessage(), MediaType.TEXT_PLAIN));
			// response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			// e.printStackTrace();
			
			// " ... now throw a JeevletException but
			// FIXME must throw Exception with a correct Status
			throw new JeevletException(e);
		}
		
		return params;
	}

	//---------------------------------------------------------------------------

	private static String simplifyName(String file)
	{
		//--- get the file name without path

		file = new File(file).getName();

		//--- the previous getName method is not enough
		//--- with IE and a server running on Linux we still have a path problem

		int pos1 = file.lastIndexOf("\\");
		int pos2 = file.lastIndexOf("/");

		int pos = Math.max(pos1, pos2);

		if (pos != -1)
			file = file.substring(pos +1).trim();

		//--- we need to sanitize the filename here - make it UTF8, no ctrl
		//--- characters and only containing [A-Z][a-z][0-9],_.-

		//--- start by converting to UTF-8
		try {
			byte[] utf8Bytes = file.getBytes("UTF8");
			file = new String(utf8Bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		//--- replace whitespace with underscore
		file = file.replaceAll("\\s","_");

		//--- remove everything that isn't [0-9][a-z][A-Z],#_.-
		file = file.replaceAll("[^\\w&&[^,_.-]]","");
		return file;
	}
}

//=============================================================================

