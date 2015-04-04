package com.luckia.biller.core.model;

import java.util.Comparator;

public class BillerComparator implements Comparator<Bill> {

	@Override
	public int compare(Bill a, Bill b) {
		if (a != null && a.getSender() != null && a.getSender().getName() != null && b != null && b.getSender() != null && b.getSender().getName() != null) {
			return a.getSender().getName().compareTo(b.getSender().getName());
		}
		return 0;
	}

}
