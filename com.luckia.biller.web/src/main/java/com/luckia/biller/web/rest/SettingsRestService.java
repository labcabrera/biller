package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppSettings;

@Path("settings")
public class SettingsRestService {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@GET
	@Path("/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppSettings findById(@PathParam("id") String primaryKey) {
		return entityManagerProvider.get().find(AppSettings.class, primaryKey);
	}
}
