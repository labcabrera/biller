/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;

/**
 * Servicio encargado de gestionar el repositorio de ficheros de la aplicación.
 */
public class FileService {

	@Inject
	@Named("file.repository.path")
	private String basePath;
	@Inject
	private EntityManagerProvider entityManagerProvider;

	public AppFile save(String name, String contentType, InputStream inputStream) {
		EntityManager entityManager = entityManagerProvider.get();
		Date now = Calendar.getInstance().getTime();
		File target = generateFileTarget(name, now);
		Long bytesCopied;
		try {
			FileOutputStream out = new FileOutputStream(target);
			bytesCopied = IOUtils.copyLarge(inputStream, out);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		// Guardamos solo la ruta relativa al basePath
		String relativePath = target.getAbsolutePath();
		Pattern pattern = Pattern.compile(String.format("^%s/?(.+)", basePath));
		Matcher matcher = pattern.matcher(relativePath);
		if (matcher.matches()) {
			relativePath = matcher.group(1);
		}
		Boolean currentTransaction = entityManager.getTransaction().isActive();
		if (!currentTransaction) {
			entityManager.getTransaction().begin();
		}
		AppFile entity = new AppFile();
		entity.setName(name);
		entity.setContentType(contentType);
		entity.setGenerated(now);
		entity.setInternalPath(relativePath);
		entity.setSize(bytesCopied);
		entityManager.persist(entity);
		if (!currentTransaction) {
			entityManager.getTransaction().commit();
		}
		return entity;
	}

	public InputStream getInputStream(AppFile appFile) {
		File target = new File(basePath, appFile.getInternalPath());
		Validate.isTrue(target.exists(), "No se encuentra el fichero " + appFile.getInternalPath());
		try {
			return new FileInputStream(target);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	private File generateFileTarget(String name, Date date) {
		File folderBase = new File(basePath);
		if (!folderBase.exists() && !folderBase.mkdirs()) {
			throw new RuntimeException("Error al crear el directorio " + folderBase.getAbsolutePath());
		}
		File folder = new File(folderBase, new SimpleDateFormat("yyyy/MM/dd").format(date));
		if (!folder.exists() && !folder.mkdirs()) {
			throw new RuntimeException("Error al crear el directorio " + folder.getAbsolutePath());
		}
		File target = new File(folder, name);
		int index = 0;
		while (target.exists() && index++ < 1000) {
			int lastDotIndex = name.lastIndexOf(".");
			String prefix = name.substring(0, lastDotIndex);
			String extension = name.substring(lastDotIndex);
			String tmpName = prefix + "-copy-" + index + extension;
			target = new File(folder, tmpName);
		}
		if (target.exists()) {
			throw new RuntimeException("Ya existe el fichero " + target.getAbsolutePath());
		}
		return target;
	}
}
