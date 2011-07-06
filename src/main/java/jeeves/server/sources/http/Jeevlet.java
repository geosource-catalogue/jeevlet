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

package jeeves.server.sources.http;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;

import jeeves.JeevletEngine;
import jeeves.server.JeevletSession;
import jeeves.server.sources.JeevletServiceRequest;
import jeeves.server.sources.JeevletServiceRequestFactory;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeevlet.constants.Config;
import jeevlet.exception.JeevletException;
import jeevlet.utils.ConfigUtils;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.util.Series;

/**
 * This is the main class. It handles http connections and inits the system
 */
public class Jeevlet extends Restlet {

	private JeevletEngine jeeves = null;
	private String appPath;
	private String baseUrl;
	private String nodeId;

	protected HashMap<String, JeevletSession> sessions = new HashMap<String, JeevletSession>(
			5);
	private Timer sessionGarbageTimer = null;

	protected long interval;
	protected long duration;

	public Jeevlet(Context context, String appPath, String baseUrl,
			String nodeId, String propertyFilePath) {
		super(context);
		this.appPath = appPath;
		this.baseUrl = baseUrl;
		this.nodeId = nodeId;

		try {
			this.interval = 1000 * Integer.parseInt(ConfigUtils.getProps(propertyFilePath)
					.getProperty(Config.INTERVAL));
			this.duration = 1000 * Integer.parseInt(ConfigUtils.getProps(propertyFilePath)
					.getProperty(Config.DURATION));
		} catch (Exception e) {
			System.err.println("Unable to load configuration "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}

		if (!this.appPath.endsWith("/"))
			this.appPath += "//";

		// -----------
		// Jeeves init
		jeeves = new JeevletEngine();

		String configPath = "WEB-INF/";
		if (nodeId != null && !nodeId.equals(""))
			configPath = this.appPath + "WEB-INF-" + nodeId + "/";

		try {

			jeeves.init(this.appPath, configPath, this.baseUrl, this.nodeId);

		} catch (JeevletException e) {
			this.getLogger().logp(Level.SEVERE, this.getClass().getName(),
					"constructor", "Jeevlet initialization failed.",
					(Throwable) e);
			e.printStackTrace();
		}

	}

	/**
	 * SessionGarbageTimer getter
	 * 
	 * @return
	 */
	private Timer getSessionGarbageTimer() {
		if (sessionGarbageTimer == null) {
			sessionGarbageTimer = new Timer();
			sessionGarbageTimer.schedule(new SessionGarbageCollector(),
					interval, interval);
		}
		return sessionGarbageTimer;
	}

	@Override
	public synchronized void start() throws Exception {
		super.start();
		getSessionGarbageTimer();
	}

	class SessionGarbageCollector extends TimerTask {
		public void run() {
			// FIXME null
			// getApplication().getLogger().info("Run session garbage collector ...");
			int count = 0;
			long now = new Date().getTime();
			Set<String> keys = sessions.keySet();
			for (String key : keys) {
				if (now - sessions.get(key).getLastUsed().getTime() > duration) {
					count++;
					sessions.remove(key);
				}
			}
			// FIXME null getApplication().getLogger().info(count
			// +" sessions closed.");
		}
	}

	@Override
	public synchronized void stop() throws Exception {
		jeeves.destroy();
		getSessionGarbageTimer().cancel();
		sessions.clear();
		super.stop();
	}

	@Override
	public void handle(Request request, Response response) {

		String ip = request.getClientInfo().getAddress();

		Log.info(Log.REQUEST,
				"==========================================================");
		Log.info(Log.REQUEST, "HTML Request (from " + ip + ") : "
				+ request.getOriginalRef().toString());
		Log.debug(Log.REQUEST, "Method       : "
				+ request.getMethod().toString());
		// TODO Log.debug(Log.REQUEST, "Content type : "+ "?" ); //
		// request.getEntity().getMediaType().toString());
		// //req.getContentType());
		// TODO Log.debug(Log.REQUEST, "Context path : "+
		// request.getHostRef().getPath()); // req.getContextPath());
		Log.debug(Log.REQUEST, "Char encoding: "
				+ request.getClientInfo().getAcceptedEncodings()); // req.getCharacterEncoding());
		Log.debug(Log.REQUEST, "Accept       : "
				+ request.getClientInfo().getAcceptedCharacterSets()); // req.getHeader("Accept"));
		Log.debug(Log.REQUEST, "Server name  : "
				+ request.getHostRef().getHostDomain()); // req.getServerName());
		Log.debug(Log.REQUEST, "Server port  : "
				+ request.getProtocol().getDefaultPort()); // req.getServerPort());

		// HttpSession httpSession = req.getSession();
		// UserSession session = (UserSession)
		// httpSession.getAttribute("session");

		// TODO User session ?
		// TODO other authorisation mode like BA in RESTLET ? probably not.

		JeevletSession session = null; // UserSession
		Series<Cookie> cookies = request.getCookies();
		Cookie cookie = cookies.getFirst("RSESSIONID");
		if (cookie != null) {
			String sessionid = cookie.getValue();
			session = sessions.get(sessionid);
		}

		// ------------------------------------------------------------------------
		// --- create a new session if doesn't exist

		if (session == null) {
			// --- create session

			session = new JeevletSession(); // UserSession

			String uuid = UUID.randomUUID().toString();
			CookieSetting cookieSetting = new CookieSetting("RSESSIONID", uuid);
			response.getCookieSettings().add(cookieSetting);

			// httpSession.setAttribute("session", session);
			sessions.put(uuid, session);

			// FIXME recycle session table after some times

			Log.debug(Log.REQUEST, "Session created for client : " + ip);
		} else
			session.setLastUsed(new Date());

		// ------------------------------------------------------------------------
		// --- build service request

		// FIXME idAdherent should be in the request parameters instead of
		// attributes
		if (nodeId != null && !nodeId.equals(""))
			request.getAttributes().put("Adherent", nodeId);

		JeevletServiceRequest srvReq = null;

		// --- Create request
		try {
			srvReq = JeevletServiceRequestFactory.create(request, response,
					jeeves.getUploadDir(), jeeves.getMaxUploadSize());
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer();

			sb.append("Cannot build ServiceRequest\n");
			sb.append("Cause : " + e.getMessage() + "\n");
			sb.append("Error : " + e.getClass().getName() + "\n");
			// res.sendError(400, sb.toString());

			// now stick the stack trace on the end and log the whole lot
			sb.append("Stack :\n" + Util.getStackTrace(e));
			Log.error(Log.REQUEST, sb.toString());

			response.setEntity(sb.toString(), MediaType.TEXT_PLAIN);
			response.setStatus(Status.valueOf(400), "Error ..."); // FIXME
			// sb.toString() la description du status ne peut contenir CR LF
			return;
		}

		// --- Execute request

		jeeves.dispatch(srvReq, session);

		response.setEntity(srvReq.getRepresentation());
		response.setStatus(new Status(srvReq.getStatusCode()));
		
		super.handle(request, response);
	}
}