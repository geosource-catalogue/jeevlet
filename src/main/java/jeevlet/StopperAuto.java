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

import jeevlet.constants.Config;
import jeevlet.utils.ConfigUtils;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

/**
 * @author m.coudert
 * 
 */
public class StopperAuto {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String propertyFilePath = args.length > 0 ? args[0] : "jeevlet.properties";

		// Get configuration
		int listenPort = Integer.parseInt(ConfigUtils.getProps(propertyFilePath).getProperty(
				Config.LISTEN_PORT));
		String hostDomain = ConfigUtils.getProps(propertyFilePath).getProperty(
				Config.HOST_DOMAIN);

		// Prepare the request
		String url = "http://" + hostDomain + ":" + listenPort + "/";
		Request request = new Request(Method.GET, url);
		request.setEntity(Config.STOP_GEONETWORK, MediaType.TEXT_PLAIN);

		// Handle it using an HTTP client connector
		Client client = new Client(Protocol.HTTP);

		try {
			client.start();
			Response response = client.handle(request);
		} catch (Exception e) {
			System.err.println("ERROR: Not running!");
			e.printStackTrace();
		} finally {
			client.stop();
			System.out.println("GeoNetwork shutdown ...");
			System.exit(0);
		}
	}

}
