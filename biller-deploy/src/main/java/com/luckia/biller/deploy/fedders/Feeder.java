package com.luckia.biller.deploy.fedders;

import java.io.InputStream;

public interface Feeder<T> {

	public void loadEntities(InputStream source);

}
