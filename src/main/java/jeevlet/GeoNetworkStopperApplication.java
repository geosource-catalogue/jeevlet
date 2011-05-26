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

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;

public class GeoNetworkStopperApplication extends Application {
	
	private Component component;

	public GeoNetworkStopperApplication(Component component) {
		super();
		this.component = component;
	}

	@Override
	public void handle(Request arg0, Response arg1) {
		super.handle(arg0, arg1);
		
		String content = arg0.getEntityAsText();
		
		if(content.trim().equalsIgnoreCase(Config.STOP_GEONETWORK))
			try {
				component.stop();
			} catch (Exception e) {
				System.out.println("GeoNetwork shutdown ...");
				System.out.println(e.getLocalizedMessage());
			} 
	}
}
