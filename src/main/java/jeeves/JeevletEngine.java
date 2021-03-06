//==============================================================================
//===
//===   JeevletEngine
//===
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

package jeeves;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerFactory;

import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Activator;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.monitor.MonitorManager;
import jeeves.server.JeevesEngine;
import jeeves.server.ScheduleManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.JeevletServiceManager;
import jeeves.server.resources.ProviderManager;
import jeeves.server.sources.JeevletServiceRequest;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeevlet.exception.JeevletException;

import org.apache.log4j.PropertyConfigurator;
import org.jdom.Element;

//=============================================================================

/**
 * This is the main class. It handles http connections and inits the system.
 * 
 * Most of the code is similar to {@link JeevesEngine}. (TODO : could be improved probably)
 * 
 * @author Jean-Pascal Boignard
 * @author Mathieu Coudert
 */

public class JeevletEngine {
	private String defaultSrv;
	private String profilesFile;
	private String defaultLang;
	private String defaultContType;
	private String uploadDir;
	private int maxUploadSize;
	private String appPath;
	private boolean defaultLocal;
	private boolean debugFlag;

	/** true if the 'general' part has been loaded */
	private boolean generalLoaded;

	private JeevletServiceManager serviceMan = new JeevletServiceManager();
	private ProviderManager providerMan = new ProviderManager();
	private ScheduleManager scheduleMan = new ScheduleManager();
	private SerialFactory serialFact = new SerialFactory();

	private Logger appHandLogger = Log.createLogger(Log.APPHAND);
	private List appHandList = new ArrayList();
	private Vector vAppHandlers = new Vector();
	private Vector vActivators = new Vector();
	private MonitorManager monitorManager;
	private String nodeId = "";

	// ---------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Inits the engine, loading all needed data
	 * 
	 * FIXME use nodeId on object creation
	 */
	public void init(String appPath, String configPath, String baseUrl,
			String nodeId) throws JeevletException {
		try {
			this.appPath = appPath;
            monitorManager = new MonitorManager(null);

			long start = System.currentTimeMillis();

			long maxMem = Runtime.getRuntime().maxMemory() / 1024;
			long totMem = Runtime.getRuntime().totalMemory() / 1024;
			long freeMem = Runtime.getRuntime().freeMemory() / 1024;

			long usedMem = totMem - freeMem;
			long startFreeMem = maxMem - usedMem;

			// Config file per adherent because configPath <>
			PropertyConfigurator.configure(configPath + "log4j.cfg");

			info("=== Starting system ========================================");

			// ---------------------------------------------------------------------
			// --- init system

			info("Engine : " + this.getClass().getName());
			info("Java version : " + System.getProperty("java.vm.version"));
			info("Java vendor  : " + System.getProperty("java.vm.vendor"));
			info("XSLT factory : "
					+ TransformerFactory.newInstance().newTransformer()
							.getClass().getName());

			info("Path    : " + appPath);
			info("BaseURL : " + baseUrl);

			serviceMan.setAppPath(appPath);
			serviceMan.setProviderMan(providerMan);
			serviceMan.setSerialFactory(serialFact);
			serviceMan.setMonitorMan(monitorManager);
			serviceMan.setBaseUrl(baseUrl);

			scheduleMan.setAppPath(appPath);
			scheduleMan.setProviderMan(providerMan);
			scheduleMan.setSerialFactory(serialFact);
			scheduleMan.setMonitorManager(monitorManager);
			scheduleMan.setBaseUrl(baseUrl);

			loadConfigFile(configPath, Jeeves.CONFIG_FILE, serviceMan);

			info("Initializing profiles...");
			serviceMan.loadProfiles(profilesFile, nodeId);

			// --- handlers must be started here because they may need the
			// context
			// --- with the ProfileManager already loaded

			for (int i = 0; i < appHandList.size(); i++)
				initAppHandler((Element) appHandList.get(i));

			info("Starting schedule manager...");
			scheduleMan.start();

			// ---------------------------------------------------------------------

			long end = System.currentTimeMillis();
			long duration = (end - start) / 1000;

			freeMem = Runtime.getRuntime().freeMemory() / 1024;
			totMem = Runtime.getRuntime().totalMemory() / 1024;
			usedMem = totMem - freeMem;

			long endFreeMem = maxMem - usedMem;
			long dataMem = startFreeMem - endFreeMem;

			info("Memory used is  : " + dataMem + " Kb");
			info("Total memory is : " + maxMem + " Kb");
			info("Startup time is : " + duration + " (secs)");

			info("=== System working =========================================");
		} catch (Exception e) {
			fatal("Raised exception during init");
			fatal("   Exception : " + e);
			fatal("   Message   : " + e.getMessage());
			fatal("   Stack     : " + Util.getStackTrace(e));

			throw new JeevletException("Exception raised", e);
		}
	}

	// ---------------------------------------------------------------------------

	private void loadConfigFile(String path, String file,
			JeevletServiceManager serviceMan) throws Exception {
		file = path + file;

		info("Loading : " + file);

		Element configRoot = Xml.loadFile(file);

		Element elGeneral = configRoot.getChild(ConfigFile.Child.GENERAL);
		Element elDefault = configRoot.getChild(ConfigFile.Child.DEFAULT);

		if (!generalLoaded) {
			if (elGeneral == null)
				throw new NullPointerException(
						"Missing 'general' element in config file :" + file);

			if (elDefault == null)
				throw new NullPointerException(
						"Missing 'default' element in config file :" + file);

			generalLoaded = true;

			initGeneral(elGeneral, serviceMan);
			initDefault(elDefault, serviceMan);
		} else {
			if (elGeneral != null)
				throw new IllegalArgumentException(
						"Illegal 'general' element in secondary include");

			if (elDefault != null)
				throw new IllegalArgumentException(
						"Illegal 'default' element in secondary include");
		}

		// --- init resources

		List resList = configRoot.getChildren(ConfigFile.Child.RESOURCES);

		for (int i = 0; i < resList.size(); i++)
			initResources((Element) resList.get(i));

		// --- init app-handlers

		appHandList
				.addAll(configRoot.getChildren(ConfigFile.Child.APP_HANDLER));

		// --- init services

		List srvList = configRoot.getChildren(ConfigFile.Child.SERVICES);

		for (int i = 0; i < srvList.size(); i++)
			initServices((Element) srvList.get(i));

		// --- init schedules

		List schedList = configRoot.getChildren(ConfigFile.Child.SCHEDULES);

		for (int i = 0; i < schedList.size(); i++)
			initSchedules((Element) schedList.get(i));

        //--- init monitoring - TODO for JeevletEngine
//
//        List<Element> monitorList = configRoot.getChildren(ConfigFile.Child.MONITORS);
//
//        for(int i=0; i<monitorList.size(); i++)
//            monitorManager.initMonitors(monitorList.get(i));

		// --- recurse on includes

		List includes = configRoot.getChildren(ConfigFile.Child.INCLUDE);

		for (int i = 0; i < includes.size(); i++) {
			Element include = (Element) includes.get(i);

			loadConfigFile(path, include.getText(), serviceMan);
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'general' element
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Setup parameters from config tag (config.xml)
	 */

	private void initGeneral(Element general, JeevletServiceManager serviceMan)
			throws BadInputEx {
		info("Initializing general configuration...");

		profilesFile = Util
				.getParam(general, ConfigFile.General.Child.PROFILES);
		uploadDir = Util.getParam(general, ConfigFile.General.Child.UPLOAD_DIR);
		try {
			maxUploadSize = Integer.parseInt(Util.getParam(general,
					ConfigFile.General.Child.MAX_UPLOAD_SIZE));
		} catch (Exception e) {
			maxUploadSize = 50;
			error("Maximum upload size not properly configured in config.xml. Using default size of 50MB");
			error("   Exception : " + e);
			error("   Message   : " + e.getMessage());
			error("   Stack     : " + Util.getStackTrace(e));
		}

		if (!new File(uploadDir).isAbsolute())
			uploadDir = appPath + uploadDir;

		if (!uploadDir.endsWith("/"))
			uploadDir += "/";

		new File(uploadDir).mkdirs();

		debugFlag = "true".equals(general
				.getChildText(ConfigFile.General.Child.DEBUG));

		serviceMan.setUploadDir(uploadDir);
		serviceMan.setMaxUploadSize(maxUploadSize);
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'general' element
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Setup parameters from config tag (config.xml)
	 */

	private void initDefault(Element defaults, JeevletServiceManager serviceMan)
			throws Exception {
		info("Initializing defaults...");

		defaultSrv = Util.getParam(defaults, ConfigFile.Default.Child.SERVICE);
		defaultLang = Util
				.getParam(defaults, ConfigFile.Default.Child.LANGUAGE);
		defaultContType = Util.getParam(defaults,
				ConfigFile.Default.Child.CONTENT_TYPE);

		defaultLocal = "true".equals(defaults
				.getChildText(ConfigFile.Default.Child.LOCALIZED));

		info("   Default local is :" + defaultLocal);

		serviceMan.setDefaultLang(defaultLang);
		serviceMan.setDefaultLocal(defaultLocal);
		serviceMan.setDefaultContType(defaultContType);

		List errorPages = defaults.getChildren(ConfigFile.Default.Child.ERROR);

		for (int i = 0; i < errorPages.size(); i++)
			serviceMan.addErrorPage((Element) errorPages.get(i));

		Element gui = defaults.getChild(ConfigFile.Default.Child.GUI);

		if (gui != null) {
			List guiElems = gui.getChildren();

			for (int i = 0; i < guiElems.size(); i++)
				serviceMan.addDefaultGui((Element) guiElems.get(i));
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'resources' element
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Setup resources from the resource element (config.xml)
	 */

	private void initResources(Element resources) {
		info("Initializing resources...");

		List resList = resources
				.getChildren(ConfigFile.Resources.Child.RESOURCE);

		for (int i = 0; i < resList.size(); i++) {
			Element res = (Element) resList.get(i);

			String name = res.getChildText(ConfigFile.Resource.Child.NAME);
			String provider = res
					.getChildText(ConfigFile.Resource.Child.PROVIDER);
			Element config = res.getChild(ConfigFile.Resource.Child.CONFIG);
			Element activator = res
					.getChild(ConfigFile.Resource.Child.ACTIVATOR);

			String enabled = res
					.getAttributeValue(ConfigFile.Resource.Attr.ENABLED);

			if ((enabled == null) || enabled.equals("true")) {
				info("   Adding resource : " + name);

				try {
					if (activator != null) {
						String clas = activator
								.getAttributeValue(ConfigFile.Activator.Attr.CLASS);

						info("      Loading activator  : " + clas);
						Activator activ = (Activator) Class.forName(clas)
								.newInstance();

						info("      Starting activator : " + clas);
						activ.startup(appPath, activator);

						vActivators.add(activ);
					}

					providerMan.register(provider, name, config);
				} catch (Exception e) {
					error("Raised exception while initializing resource. Skipped.");
					error("   Resource  : " + name);
					error("   Provider  : " + provider);
					error("   Exception : " + e);
					error("   Message   : " + e.getMessage());
					error("   Stack     : " + Util.getStackTrace(e));
				}
			}
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'appHandler' element
	// ---
	// ---------------------------------------------------------------------------

	private void initAppHandler(Element handler) throws Exception {
		if (handler == null)
			info("Handler not found");
		else {
			String className = handler
					.getAttributeValue(ConfigFile.AppHandler.Attr.CLASS);

			if (className == null)
				throw new IllegalArgumentException("Missing '"
						+ ConfigFile.AppHandler.Attr.CLASS + "' attribute in '"
						+ ConfigFile.Child.APP_HANDLER + "' element");

			info("Found handler : " + className);

			Class c = Class.forName(className);

			ApplicationHandler h = (ApplicationHandler) c.newInstance();

			ServiceContext srvContext = serviceMan
					.createServiceContext("AppHandler");
			srvContext.setLanguage(defaultLang);
			srvContext.setLogger(appHandLogger);

			try {
				info("--- Starting handler --------------------------------------");

				Object context = h.start(handler, srvContext);

				srvContext.getResourceManager().close();
				vAppHandlers.add(h);
				serviceMan.registerContext(h.getContextName(), context);
				scheduleMan.registerContext(h.getContextName(), context);

				info("--- Handler started ---------------------------------------");
			} catch (Exception e) {
				error("Raised exception while starting appl handler. Skipped.");
				error("   Handler   : " + className);
				error("   Exception : " + e);
				error("   Message   : " + e.getMessage());
				error("   Stack     : " + Util.getStackTrace(e));

				srvContext.getResourceManager().abort();
			}
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'services' element
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Setup services found in the services tag (config.xml)
	 */

	private void initServices(Element services) throws Exception {
		info("Initializing services...");

		// --- get services root package
		String pack = services
				.getAttributeValue(ConfigFile.Services.Attr.PACKAGE);

		// --- scan services elements
		List srvList = services.getChildren(ConfigFile.Services.Child.SERVICE);

		for (int i = 0; i < srvList.size(); i++) {
			Element service = (Element) srvList.get(i);
			String name = service
					.getAttributeValue(ConfigFile.Service.Attr.NAME);

			info("   Adding service : " + name);

			try {
				serviceMan.addService(pack, service);
			} catch (Exception e) {
				warning("Raised exception while registering service. Skipped.");
				warning("   Service   : " + name);
				warning("   Package   : " + pack);
				warning("   Exception : " + e);
				warning("   Message   : " + e.getMessage());
				warning("   Stack     : " + Util.getStackTrace(e));
			}
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- 'schedules' element
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Setup schedules found in the 'schedules' element (config.xml)
	 */

	private void initSchedules(Element schedules) throws Exception {
		info("Initializing schedules...");

		// --- get schedules root package
		String pack = schedules
				.getAttributeValue(ConfigFile.Schedules.Attr.PACKAGE);

		// --- scan schedules elements
		List schedList = schedules
				.getChildren(ConfigFile.Schedules.Child.SCHEDULE);

		for (int i = 0; i < schedList.size(); i++) {
			Element schedule = (Element) schedList.get(i);
			String name = schedule
					.getAttributeValue(ConfigFile.Schedule.Attr.NAME);

			info("   Adding schedule : " + name);

			try {
				scheduleMan.addSchedule(pack, schedule);
			} catch (Exception e) {
				error("Raised exception while registering schedule. Skipped.");
				error("   Schedule  : " + name);
				error("   Package   : " + pack);
				error("   Exception : " + e);
				error("   Message   : " + e.getMessage());
				error("   Stack     : " + Util.getStackTrace(e));
			}
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Destroy
	// ---
	// ---------------------------------------------------------------------------

	public void destroy() {
		try {
			info("=== Stopping system ========================================");

			info("Stopping schedule manager...");
			scheduleMan.exit();

			info("Stopping handlers...");
			stopHandlers();

			info("Stopping resources...");
			stopResources();

			info("=== System stopped ========================================");
		} catch (Exception e) {
			error("Raised exception during destroy");
			error("  Exception : " + e);
			error("  Message   : " + e.getMessage());
			error("  Stack     : " + Util.getStackTrace(e));
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * Stop handlers
	 */

	private void stopHandlers() throws Exception {
		for (int i = 0; i < vAppHandlers.size(); i++) {
			ApplicationHandler h = (ApplicationHandler) vAppHandlers.get(i);

			h.stop();
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * Stop resources
	 */

	private void stopResources() {
		providerMan.end();

		for (Iterator i = vActivators.iterator(); i.hasNext();) {
			Activator a = (Activator) i.next();

			info("   Stopping activator : " + a.getClass().getName());
			a.shutdown();
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public String getUploadDir() {
		return uploadDir;
	}

	// ---------------------------------------------------------------------------

	public int getMaxUploadSize() {
		return maxUploadSize;
	}

	// ---------------------------------------------------------------------------

	public void dispatch(JeevletServiceRequest srvReq, UserSession session) {
		if (srvReq.getService() == null || srvReq.getService().length() == 0)
			srvReq.setService(defaultSrv);

		if (srvReq.getLanguage() == null || srvReq.getLanguage().length() == 0)
			srvReq.setLanguage(defaultLang);

		srvReq.setDebug(srvReq.hasDebug() && debugFlag);

		// --- normal dispatch pipeline

		serviceMan.dispatch(srvReq, session);
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Other private methods
	// ---
	// ---------------------------------------------------------------------------

	private void debug(String message) {
		Log.debug(Log.ENGINE, message);
	}

	private void info(String message) {
		Log.info(Log.ENGINE, message);
	}

	private void warning(String message) {
		Log.warning(Log.ENGINE, message);
	}

	private void error(String message) {
		Log.error(Log.ENGINE, message);
	}

	private void fatal(String message) {
		Log.fatal(Log.ENGINE, message);
	}
}

// =============================================================================

