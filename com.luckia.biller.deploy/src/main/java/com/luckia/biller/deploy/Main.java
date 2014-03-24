package com.luckia.biller.deploy;

import java.io.IOException;

import com.luckia.biller.deploy.poi.MasterWorkbookProcessor;

/**
 * Clase encargada de realizar la carga inicial de base de datos.
 */
public class Main {

	public static void main(String[] args) throws IOException {
		System.out.println("Biller deployment module");
		Bootstrap.main(args);
		MasterWorkbookProcessor.main(args);
	}
}
