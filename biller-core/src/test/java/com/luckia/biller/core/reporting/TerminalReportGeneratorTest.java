package com.luckia.biller.core.reporting;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.LuckiaCoreModule;

public class TerminalReportGeneratorTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		injector.getInstance(PersistService.class).start();
		TerminalReportGenerator generator = injector.getInstance(TerminalReportGenerator.class);
		Date date = Calendar.getInstance().getTime();
		FileOutputStream out = new FileOutputStream("./target/terminals.xls");
		generator.generate(date, out);
	}
}
