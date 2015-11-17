/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.AppFile;

public class FileServiceTest {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		FileService fileService = injector.getInstance(FileService.class);
		InputStream inputStream = new ByteArrayInputStream("test".getBytes());
		AppFile file = fileService.save("test.txt", "text/txt", inputStream);
		System.out.println(file.getId());
		System.out.println(file);
	}
}
