package ru.trolsoft.tools.fontc;

public class ResourceSyntaxException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private String message;

	public ResourceSyntaxException(String fileName, String msg) {
		super(fileName + ": " + msg);
		this.fileName = fileName;
		this.message = msg;
	}
	
	
	public String getFileName() {
		return fileName;
	}
	
	
	public String getMessage() {
		return message;
	}
}
