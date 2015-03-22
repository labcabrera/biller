package com.luckia.biller.deploy;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatchSupport {

	private static final Logger LOG = LoggerFactory.getLogger(PatchSupport.class);

	protected boolean confirm() {
		Boolean result;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.printf("Esta a punto de ejecutar el patch '%s'\n", getClass().getSimpleName());
			String line;
			do {
				System.out.print("Desea continuar? [y/n]: ");
				line = reader.readLine();
				if ("y".equals(line) || "n".equals(line)) {
					result = "y".equals(line);
					break;
				} else {
					System.out.println("Opcion no valida");
				}
			} while (true);
			return result;
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return true;

	}

}
