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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.jdom.Element;
import org.restlet.data.MediaType;

public class BlobRepresentation extends JeevletRepresentation{
	
	private static final int BUF_SIZE = 8192;
	

	public BlobRepresentation(MediaType mediaType, long expectedSize) {
		super(mediaType, expectedSize);
	}

	public BlobRepresentation(MediaType mediaType) {
		super(mediaType);
	}
	
	public BlobRepresentation(Element element, MediaType mediaType, long expectedSize){
		super(mediaType, expectedSize);
		this.response = element;
	}
	
	public BlobRepresentation(Element element, MediaType mediaType, boolean cache){
		super(mediaType);
		// TODO expectedSize ?
		// TODO cache
		this.response = element;
	}	

	@Override
	public void write(OutputStream arg0) throws IOException {
		if (response == null)
			return;
		
		String data = response.getText();
	
		// FIXME use apache commons codec instead of sun lib
		// byte blob[] = new BASE64Decoder().decodeBuffer(data);
		
		byte blob[] = Base64.decodeBase64(data.getBytes());
		
		ByteArrayInputStream input = new ByteArrayInputStream(blob);
		copy(input, arg0);
		arg0.flush();
		input.close();
	}
		
	//-----------------------------------------------------------------------------
	// copies an input stream to an output stream

	private static void copy(InputStream in, OutputStream output) throws IOException
	{
		BufferedInputStream input = new BufferedInputStream(in);
		try
		{
			byte buffer[] = new byte[BUF_SIZE];
			int nRead;
			do
			{
				nRead = input.read(buffer, 0, BUF_SIZE);
				output.write(buffer, 0, nRead);

			} while (nRead == BUF_SIZE);
			input.close();
		}
		catch (IOException e)
		{
			input.close();
			throw e;
		}
		output.flush();
	}

}
