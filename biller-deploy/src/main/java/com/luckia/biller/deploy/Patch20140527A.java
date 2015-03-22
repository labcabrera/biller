package com.luckia.biller.deploy;

import java.io.FileOutputStream;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.pdf.PDFLiquidationGenerator;

public class Patch20140527A extends PatchSupport implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Patch20140527A.class);

	public static void main(String[] args) {
		new Patch20140527A().run();
	}

	public void run() {
		if (!confirm()) {
			System.out.println("Application aborted");
			return;
		}
		try {
			LOG.info("Ejecutando patch");
			Injector injector = Guice.createInjector(new LuckiaCoreModule());
			injector.getInstance(PersistService.class).start();
			Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
			PDFLiquidationGenerator generator = injector.getInstance(PDFLiquidationGenerator.class);
			EntityManager entityManager = entityManagerProvider.get();
			String liquidationId = "71a7c1e7-83ec-4186-8aea-3bd694e7bdbd";
			Liquidation liquidation = entityManager.find(Liquidation.class, liquidationId);
			Validate.notNull(liquidation, "Missing liquidation " + liquidationId);
			FileOutputStream out = new FileOutputStream("./target/liquidation-test.pdf");
			generator.generate(liquidation, out);
			out.close();
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}
}
