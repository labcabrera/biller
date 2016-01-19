package com.luckia.biller.web.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.common.SettingsManager;

@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminRestService {

	@Inject
	private SettingsManager settingsManager;

	@GET
	@Path("info")
	public Map<String, String> info() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("Biller BD connection", settingsManager.getProperties("jpa-biller").getProperty("javax.persistence.jdbc.url"));
		map.put("Biller BD user", settingsManager.getProperties("jpa-biller").getProperty("javax.persistence.jdbc.user"));
		map.put("LIS DB connection", settingsManager.getProperties("jpa-lis").getProperty("javax.persistence.jdbc.url"));
		map.put("LIS DB user", settingsManager.getProperties("jpa-lis").getProperty("javax.persistence.jdbc.user"));
		map.put("Mail", settingsManager.getProperties("mail").getProperty("host"));
		map.put("Repository", settingsManager.getProperties("global").getProperty("repositoryPath"));
		return map;
	}
}
