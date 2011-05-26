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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.MediaType;

public class OutputStreamRepresentation extends JeevletRepresentation {

	ByteArrayOutputStream outputStream = null;

	public OutputStreamRepresentation(ByteArrayOutputStream output,
			MediaType mediaType, boolean cache) {
		super(null, mediaType, cache);
		this.outputStream = output;
	}

	@Override
	public void write(OutputStream arg0) throws IOException {
		arg0.write(this.outputStream.toByteArray());
		arg0.flush();
		this.outputStream.close();
	}

}
