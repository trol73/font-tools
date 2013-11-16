package ru.trolsoft.utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Version 1.01
 * 
 * @author trol
 *
 */
public class Image {
	public static int FORMAT_PNG = 0;
	public static int FORMAT_JPEG = 1;
	public static int FORMAT_GIF = 2;
	public static int FORMAT_BMP = 3;
	public static int FORMAT_WBMP = 4;
	
	public static final int TYPE_INT_RGB = BufferedImage.TYPE_INT_RGB;
	public static final int TYPE_INT_ARGB = BufferedImage.TYPE_INT_ARGB;
	public static final int TYPE_BYTE_INDEXED = BufferedImage.TYPE_BYTE_INDEXED;
	
	public static final String FORMATS[] = {"png", "jpeg", "gif", "bmp", "wbmp"};
	
	private BufferedImage img;
	
	/**
	 * 
	 */
	public Image() {
	}
	
	public Image(int width, int height, int type) {
		img = new BufferedImage(width, height, type);
	}

	
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void loadImage(String fileName) throws IOException {
		img = ImageIO.read(new File(fileName));
	}
	
	/**
	 * 
	 * @param fileName
	 * @param format
	 * @throws IOException
	 */
	public void saveImage(String fileName, int format) throws IOException {
		File f = new File(fileName);
	    ImageIO.write(img, FORMATS[format], f);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getWidth() {
		return img.getWidth();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getHeight() {
		return img.getHeight();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getType() {
		return img.getType();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param rgb
	 */
	public void setPixel(int x, int y, int rgb) {
		img.setRGB(x, y, rgb);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getPixel(int x, int y) {
		return img.getRGB(x, y);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return 0 - for transparent pixels, 0xFF - for fully opaqued pixels
	 */
	public int getPixelAlpha(int x, int y) {
		return (img.getRGB(x, y) >> 24) & 0xFF;
	}
	
	/**
	 * 
	 * @param image
	 * @param x
	 * @param y
	 */
	public void drawImage(Image image, int x, int y) {
		for ( int xx = 0; xx < image.getWidth(); xx++ ) {
			for ( int yy = 0; yy < image.getHeight(); yy++ ) {
				if ( xx+x >= img.getWidth() || yy+y >= img.getHeight() || x >= image.getWidth() || y >= image.getHeight() ) {
					continue;
				}
				img.setRGB(xx+x, yy+y, image.getPixel(xx, yy));
			}
		}
	}
	
	
	/**
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param rect
	 */
	public void drawImage(Image image, int x, int y, Rectangle rect) {
		for ( int xx = 0; xx < rect.width; xx++ ) {
			for ( int yy = 0; yy < rect.height; yy++ ) {
				img.setRGB(rect.x + xx, rect.y + yy, image.getPixel(x+xx, y+yy));
				//int px = 0xff0000;
				//img.setRGB(rect.x + xx, rect.y + yy, px);
			}
		}
	}

	
	
}
