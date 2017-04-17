package com.luckia.biller.deploy;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatchSupport {

	protected boolean confirm() {
		Boolean result;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.printf("Esta a punto de ejecutar el patch '%s'%n",
					getClass().getSimpleName());
			String line;
			do {
				System.out.print("Desea continuar? [y/n]: ");
				line = reader.readLine();
				if ("y".equals(line) || "n".equals(line)) {
					result = "y".equals(line);
					break;
				}
				else {
					System.out.println("Opcion no valida");
				}
			}
			while (true);
			return result;
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return true;

	}

}
