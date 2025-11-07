package com.bariskokulu.crudgen.util;

public enum HTTPMethod {

	POST("Post"), GET("Get"), PATCH("Patch"), PUT("Put"), DELETE("Delete");
	
	public final String text;
	
	HTTPMethod(String text) {
		this.text = text;
	}

	public String text() {
		return text;		
	}
	
}
