package ru.trolsoft.tools.linemaker;

import java.io.IOException;

import ru.trolsoft.utils.Image;

public class LineMaker {
	
	public static void usage() {
		System.out.println("Usage: linemaker: <source image> [<out image>]");
	}
	
	private static String color2str(int color) {
		String s = Integer.toHexString(color);
		while ( s.length() < 6 ) {
			s = "0" + s; 
		}
		if ( s.length() > 6 ) {
			while ( s.length() < 8 ) {
				s = "0" + s; 
			}			
		}
		return s;
	}
	
	public static void main(String args[]) throws IOException {
		if ( args.length < 1 ) {
			usage();
			return;
		}
		String src = args[0];
		String out = args.length > 1 ? args[1] : src;
		Image imgSrc = new Image();
		imgSrc.loadImage(src);
		Image imgOut = new Image(imgSrc.getWidth(), imgSrc.getHeight()+1, imgSrc.getType());
		imgOut.drawImage(imgSrc, 0, 1);
		int transparentColor = imgSrc.getPixel(0, 0);		
		//int opaqueColor = (transparentColor & 0xff000000) | ((~transparentColor) & 0xffffff);
		int opaqueColor = 0xff000000 | ((~transparentColor) & 0xffffff);
		System.out.println("Transparent color: " + color2str(transparentColor));
		System.out.println("Opaque color: " + color2str(opaqueColor));
/*		
		for ( int x = 0; x < imgSrc.getWidth(); x++ ) {
			for ( int y = 0; y < imgSrc.getHeight(); y++ ) {
				if ( imgSrc.getPixel(x, y) != transparentColor ) {
					opaqueColor = imgSrc.getPixel(x, y);
					break;
				}
			}
		}
*/
		for ( int x = 0; x < imgSrc.getWidth(); x++ ) {
			boolean empty = true;
			for ( int y = 0; y < imgSrc.getHeight(); y++ ) {
				if ( imgSrc.getPixel(x, y) != transparentColor && imgSrc.getPixelAlpha(x, y) != 0) {
					empty = false;
				}
			}
			imgOut.setPixel(x, 0, empty ? transparentColor : opaqueColor);
		}
		imgOut.saveImage(out, Image.FORMAT_PNG);
	}
}
