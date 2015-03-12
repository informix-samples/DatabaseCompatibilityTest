package com.ibm.database.compatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.database.compatibility.Binding.BindingTypeAdapter;

public class GsonUtils {

	public static Gson newGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.disableHtmlEscaping();
		builder.registerTypeAdapter(Binding.class, new BindingTypeAdapter());
		Gson gson = builder.create();
		return gson;
	}
	
}
