package models.itemCopy.dto;

public class Gallery {

	private String url;
	private int width;
	private int height;
	private String type;

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public Gallery(String url, int width, int height, String type) {
		super();
		this.url = url;
		this.width = width;
		this.height = height;
		this.type = type;
	}

	public Gallery() {
		super();
	}
	
	
	

}
