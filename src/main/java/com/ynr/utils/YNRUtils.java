package com.ynr.utils;

import java.util.HashMap;
import java.util.Map;

public class YNRUtils {

	public static Map<Integer, String> countryIdToEngMap = new HashMap<>();
	
	static {
		countryIdToEngMap.put(4, "australia");
		countryIdToEngMap.put(5, "evus");
		countryIdToEngMap.put(6, "france");
	}
	
}