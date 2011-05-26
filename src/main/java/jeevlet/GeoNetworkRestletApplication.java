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

package jeevlet;

import jeeves.server.sources.http.Jeevlet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

public class GeoNetworkRestletApplication extends Application {

	String appPath = "";
	String baseUrl = "";
	String nodeId = "";
	String propertyFilePath = "jeevlet.properties";

	public GeoNetworkRestletApplication(Context context, String appPath,
			String baseUrl, String nodeId, String propertyFilePath) {
		super(context);

		this.appPath = appPath;
		this.baseUrl = baseUrl;
		this.nodeId = nodeId;
		this.propertyFilePath = propertyFilePath;
	}

	@Override
	public Restlet createInboundRoot() {
		// Create a router
		Router router = new Router(getContext().createChildContext());

		Restlet restletNode = new Jeevlet(getContext().createChildContext(),
				appPath, baseUrl, nodeId, this.propertyFilePath);

		// Attach the resources to the router
		router.attach("/srv/{loc}/{service}", restletNode); 

		Directory directory = new Directory(getContext().createChildContext(),
				"file:///" + appPath);

		directory.setDeeplyAccessible(true);
		directory.setListingAllowed(true);
		directory.setModifiable(false);

		router.attachDefault(directory); 

		// Return the root router
		return router;
	}
}
