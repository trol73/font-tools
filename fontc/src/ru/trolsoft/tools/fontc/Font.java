package ru.trolsoft.tools.fontc;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import ru.trolsoft.utils.JavaFileGenerator;
import ru.trolsoft.utils.Image;

public class Font {
	private String fileName;
	private Rectangle charRects[];
	private Image imgSrc;
	private int minWidth = -1;	// оптимальные размеры изображения
	private int minHeight = -1;
	private Rectangle newRects[];
	private List<Integer> packedChars;
	private List<Integer> fontArray;

	String name;
	String imgSrcName;
	String imgOutName;
	Image imgOut;
	String chars;
	List<Rectangle> charRect;
	int baseLine;
	int distance;
	int height;
	int maxWidth;
	int maxHeight;
	
	
	
	/**
	 * 
	 * @param fileName
	 */
	public Font(String fileName) {
		try {
			parse(fileName);
			imgSrc = new Image();
			try {
				imgSrc.loadImage(imgSrcName);
			} catch (Exception e) {
				System.out.println("Can't load source image: " + imgSrcName);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 */
	public void compile() {
		initCharsRectangles();
		sortChars();
		buildFontDataArray();
		calcFontHeight();
		getOptimalImageSize();
		calcNewSymbolRects();
		writeDataFile("font.java");
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void save() throws IOException {
		imgOut.saveImage(imgOutName, Image.FORMAT_PNG);
	}
	
	/**
	 * 
	 * @param fileName
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ResourceSyntaxException
	 */
	private void parse(String fileName) throws ParserConfigurationException, SAXException, IOException, ResourceSyntaxException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		parse(doc);
		this.fileName = fileName;
	}
	

	/**
	 * 
	 * @param doc
	 * @throws ResourceSyntaxException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private void parse(Document doc) throws ResourceSyntaxException, ParserConfigurationException, SAXException, IOException {
		doc.getDocumentElement().normalize();
		NodeList nFonts = doc.getElementsByTagName("font");
		if ( nFonts.getLength() != 1 ) {
			throw new ResourceSyntaxException(fileName, "one <font> root tag expected");
		}
		parse(nFonts.item(0));
	}
	
	/**
	 * 
	 * @param nodes
	 * @throws ResourceSyntaxException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private void parse(Node node) throws ParserConfigurationException, SAXException, IOException, ResourceSyntaxException {
		//System.out.println(node.getNodeName());
		this.name = getTagAttribute(node, "name");
		this.imgSrcName = getTagAttribute(node, "imgSrc");
		this.imgOutName = getTagAttribute(node, "imgOut");
		this.chars = getTagAttribute(node, "char");
		this.baseLine = getTagAttributeInt(node, "baseLine", 0);
		this.distance = getTagAttributeInt(node, "distance", 0);
		this.height = getTagAttributeInt(node, "height", 0);
		this.maxWidth = getTagAttributeInt(node, "maxWidth", 0);
		this.maxHeight = getTagAttributeInt(node, "maxHeight", 0);
	}
	
	/**
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	private static String getTagAttribute(Node node, String name) {
		Node nAttr = node.getAttributes().getNamedItem(name);
		return nAttr == null ? null :  nAttr.getTextContent();
	}

	/**
	 * 
	 * @param node
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	private static int getTagAttributeInt(Node node, String name, int defaultValue) {
		Node nAttr = node.getAttributes().getNamedItem(name);
		try {
			return Integer.parseInt(nAttr.getTextContent());
		} catch ( Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * 
	 */
	private void initCharsRectangles() {
		charRects = new Rectangle[chars.length()];
		int charsCount = 0;
		int transparentColor = imgSrc.getPixel(0, 0);
		boolean scanChar = false;
		
		// вычисляем начало символов сверху
		int y0 = 1;
		for ( int y = 1; y < imgSrc.getHeight(); y++ ) {
			boolean empty = true;
			for ( int x = 0; x < imgSrc.getWidth()-1; x++ ) {
				if ( imgSrc.getPixel(x, y) != imgSrc.getPixel(x+1, y) ) {
					empty = false;
					break;
				}
			}
			if ( empty ) {
				y0 = y;
			} else {
				break;
			}
		}

		
		for ( int i = 0; i < imgSrc.getWidth(); i++ ) {
			int pixel = imgSrc.getPixel(i, 0);
			
			boolean isOpaque = pixel != transparentColor && (pixel & 0xff000000) != 0;
			if ( isOpaque ) {	// область символа
				if ( !scanChar ) {
					scanChar = true;
					Rectangle r = new Rectangle();
					r.x = i;
					r.y = y0 + 1;
					r.width = 0;
					r.height = imgSrc.getHeight() - r.y;
					if ( charsCount >= charRects.length ) {
						System.out.	println("ERROR: " + fileName + " - numbers of symbols are different: declared in font string - " + chars.length() + " found in image: > " + charsCount );
						throw new RuntimeException("number of symbols mismath");
					}
					charRects[charsCount++] = r;
				}
			} else { // область фона
				if ( scanChar ) {
					scanChar = false;
					charRects[charsCount-1].width = i - charRects[charsCount-1].x;
				}
			}
		} // for
		if ( scanChar ) { 			
			charRects[charsCount-1].width = imgSrc.getWidth() - charRects[charsCount-1].x;
		}

		if ( charsCount != charRects.length ) {
			System.out.println("ERROR: " + fileName + " - numbers of symbols are different: declared in font string - " + chars.length() + " found in image: = " + charsCount );
			throw new RuntimeException("number of symbols mismath");
		}
	}
	
	
	/**
	 * 
	 */
	private void sortChars() {
		// нужно отсортировать массив символов по возрастанию и создать массив маппинга
		int count = chars.length();
		int sortIndex[] = new int[count];	
		for ( int i = 0; i < count; i++ ) {
			sortIndex[i] = i;
		}
		
		for ( int i = 0; i < count-1; i++ ) {
			char min = chars.charAt(sortIndex[i]);
			int minIndex = i;
			for ( int j = i+1; j < count; j++ ) { 		
				if ( chars.charAt(sortIndex[j]) < min ) {
					min = chars.charAt(sortIndex[j]);
					minIndex = j;
				}
			}
			int temp = sortIndex[minIndex];
			sortIndex[minIndex] = sortIndex[i];
			sortIndex[i] = temp;
		}

		
		// сортируем данные
		String sortedChars = "";
		newRects = new Rectangle[count];
		for ( int i = 0; i < count; i++ ) {
			sortedChars += chars.charAt(sortIndex[i]);
			newRects[i] = new Rectangle(charRects[sortIndex[i]]);
		}
		chars = sortedChars;
		for ( int i = 0; i < count; i++ ) {
			charRects[i] = new Rectangle(newRects[i]);
		}
	}


	/**
	 * 
	 */
	private void buildFontDataArray() {
		List<Integer> listLen = new ArrayList<Integer>();		// тут хранятся длины списков	
		List<Integer> listStart = new ArrayList<Integer>();		// индексы первых символов списков

		// находим все списки
		listStart.add(new Integer(0));
		listLen.add(new Integer(1));	
		for ( int i = 1; i < chars.length(); i++ ) {
			if ( chars.charAt(i) - chars.charAt(i-1) == 1 ) {	// если два подрядследующих символа, то увеличиваем длину списка
				listLen.set(listLen.size()-1, listLen.get(listLen.size()-1) + 1);
			} else {  // иначе, добавляем новый список
				listLen.add(new Integer(1));
				listStart.add(new Integer(i));
			}
		}

		// сортируем их по длине - самые длинные - в начало
		List<Integer> sortListIndexes = new ArrayList<Integer>();	// индексы сортировки списков
		List<Integer> sortIndexesMap = new ArrayList<Integer>();	// массив маппинга для сортировки символов
		
		for ( int i = 0; i < listLen.size(); i++ ) {
			// считаем, сколько есть списков короче данного (i-го)
			int index = 0;
			for (int j = 0; j < listLen.size(); j++) {
				int lli = listLen.get(i);
				int llj = listLen.get(j);
				if ( (llj >= lli) && ( j <= i ) || (llj > lli) && ( j > i ) ) {
					index++;
				}
			}
			sortListIndexes.add(new Integer(index-1));
		}
		
		// теперь по найденным местам создаем вектор маппинга для сортировки самих символов
		for ( int i = 0; i < listLen.size(); i++ ) {
			for ( int j = 0; j < listLen.size(); j++ ) { 
				if ( sortListIndexes.get(j) == i) {
					sortIndexesMap.add(new Integer(j));				
					break;
				}
			}
		}
		
		// создаем компактный массив
		packedChars = new ArrayList<Integer>();
		boolean zerro = false;
		int j = 0;
		for ( int i = 0; i < listLen.size(); i++ ) {
			int index = sortIndexesMap.get(i);
			int lli = listLen.get(index).intValue();
			if ( lli > 0 ) {		
				packedChars.add(lli);
				packedChars.add((int)chars.charAt(listStart.get(index)));
			} else {
				if ( !zerro ) {
					packedChars.add(0);
					zerro = true;
				}
				packedChars.add((int)chars.charAt(listStart.get(index)));
			}
		}
		if ( !zerro ) { 
			packedChars.add(0);
		}


		// теперь надо отсортировать массивы символов и их областей
		int count = chars.length();
		char charsTemp[] = new char[count];				// временный массив символов
		Rectangle clipsTemp[] = new Rectangle[count];	// временный массив областей символов

		int cntSort = 0;		// счетчик символов в отсортированном массиве
		for ( int i = 0; i < listLen.size(); i++ ) {
			int index = sortIndexesMap.get(i);
			for ( j = 0; j < listLen.get(index); j++ ) {
				int lsi = listStart.get(index).intValue();
				charsTemp[cntSort+j] = chars.charAt(lsi + j);
				clipsTemp[cntSort+j] = new Rectangle(charRects[lsi + j]);
			}
			cntSort += listLen.get(index);
		}

		// копируем отсортированный массив на место старого
		chars = "";
		for ( int i = 0; i < count; i++ ) {
			chars += charsTemp[i];
			charRects[i] = new Rectangle(clipsTemp[i]);
		}		
	}
	
	/**
	 * 
	 */
	private void calcFontHeight() {
		// если высота не указана явно, ее нужно вычислить
		if ( height > 0 ) {
			return;
		}
		
		height = imgSrc.getHeight()-1;
		for ( int y = imgSrc.getHeight()-1; y > 0; y-- ) {
			boolean empty = true;
			for ( int x = 0; x < imgSrc.getWidth()-1; x++ ) {
				if ( imgSrc.getPixel(x, y) != imgSrc.getPixel(x+1, y) ) {
					empty = false;
					break;
				}
			}
			if ( empty ) {
				height--;
			}			
		}
	}

	/**
	 * 
	 */
	private void getOptimalImageSize() {
		// если не задано ограничений на размер картинки, устанавливаем их вручную
		if ( maxWidth <= 0 ) {
			maxWidth = 102400;
		}
		if ( maxHeight <= 0 ) {
			maxHeight = 102400;
		}

		int count = chars.length();
		
		// определяем наиболее оптимальные размеры конечного файла
		int minArea = Integer.MAX_VALUE;
		minWidth = -1;
		minHeight = -1;

		int maxChawWidth = charRects[0].width;
		for ( int i = 0; i < count; i++ ) {
			if ( charRects[i].width > maxChawWidth ){
				maxChawWidth = charRects[i].width;
			}
		}

		// изменяем ширину от минимума до максимума и ищем минимальную площадь
		for ( int testWidth = maxChawWidth; testWidth <= maxWidth; testWidth++ ) {		
			int imageWidth = 0;
			int imageHeight = 0;
			int lineWidth = 0;
			for ( int i = 0; i < count; i++ ) {
				// если символ не умещается на строке, переходим на новую строку
				if ( lineWidth + charRects[i].width > testWidth ) {
					// определяем ширину конечного рисунка по максимально длинной строке
					if ( lineWidth > imageWidth ) { 
						imageWidth = lineWidth;
					}
					imageHeight += height;
					lineWidth = 0;			
				}
				// размещаем следующий символ
				lineWidth += charRects[i].width;
			} // цикл по символам
			if ( lineWidth > 0 ) {
				imageHeight += height; // увеличиваем высоту, если есть неполная строка
			}
			if ( lineWidth > imageWidth ) {
				imageWidth = lineWidth; // ?? А ОНО ТУТ НАДО?? СТРАХОВКА
			}
			// проверяем минимум
			if ( (imageWidth*imageHeight <= minArea) && (imageWidth <= maxWidth) && (imageHeight <= maxHeight) ) {
				minArea = imageWidth*imageHeight;
				minWidth = imageWidth; 
				minHeight = imageHeight;
			}
		} // цикл минимизации по ширине
		if ( minWidth < 0 ) {
			System.out.println("ERROR: " + fileName + " - can't place letters in this region");
			throw new RuntimeException("can't place letters in this region"); 
		}
	}

	/**
	 * 
	 */
	private void calcNewSymbolRects() {
		// вычисляем координаты	символов на новом рисунке
		int count = chars.length();
		
		int resultImageWidth = minWidth;
		int imageWidth = 0;
		int imageHeight = 0;
		int lineWidth = 0;
		for ( int i = 0; i < count; i++ ) {
			// если символ не умещается на строке, переходим на новую строку
			if ( lineWidth + charRects[i].width > resultImageWidth ) {
				// определяем ширину конечного рисунка по максимально длинной строке
				if ( lineWidth > imageWidth ) {
					imageWidth = lineWidth;
				}
				imageHeight += height;
				lineWidth = 0;			
			}
			// размещаем следующий символ
			newRects[i].x = lineWidth;
			newRects[i].y = imageHeight;
			newRects[i].width = charRects[i].width;
			newRects[i].height = height - 1;
//System.out.println("< " + charRects[i].x + " " + charRects[i].y + " " + charRects[i].width + " " + charRects[i].height);			
//System.out.println("> " + newRects[i].x + " " + newRects[i].y + " " + newRects[i].width + " " + newRects[i].height);
/*			
			if ( newRects[i].y + newRects[i].height > imgSrc.getHeight() ) {
				System.out.println("!!! wrong symbol height");
				newRects[i].height = imgSrc.getHeight() - newRects[i].y; 
			}
*/
			lineWidth += newRects[i].width;
//			System.out.println(""+i+": " + newRects[i]);
		}
//System.out.println(";;;;;;"+charRect + " <-> " + newRects);		
//for (int i = 0; i < count; i++)
//System.out.println("!>> "+chars.charAt(i) + "-> " + charRects[i] + "<->"+newRects[i]);		
		if ( lineWidth > 0 ) {
			imageHeight += height; // увеличиваем высоту, если есть неполная строка
		}
		if ( lineWidth > imageWidth ) {
			imageWidth = lineWidth; // ?? А ОНО ТУТ НАДО?? СТРАХОВКА
		}
	

		imgOut = new Image(imageWidth, imageHeight, imgSrc.getType());
		int clrBackground = imgSrc.getPixel(0, 0);
		for ( int y = 0; y < imgOut.getHeight(); y++ ) {
			for ( int x = 0; x < imgOut.getWidth(); x++ ) {
				imgOut.setPixel(x, y, clrBackground);
			}
		}
		for ( int i = 0; i < count; i++ ) {
			// копируем символ
			imgOut.drawImage(imgSrc, charRects[i].x, charRects[i].y, newRects[i]);
		}
		// копируем и новые координаты
		for ( int i = 0; i < count; i++ ) { 
			charRects[i] = newRects[i];
		}
	
		// преобразуем упакованный массив
		fontArray = new ArrayList<Integer>();
		boolean groups = true;
		boolean isCount = true;
		for ( int i = 0; i < packedChars.size(); i++ ) {
			if ( ( packedChars.get(i) == 1 ) && isCount ) {
				if ( groups ) {
					groups = false;
					fontArray.add(0);
				}
			} else {
				fontArray.add(packedChars.get(i));
			}
			isCount = !isCount;
		}
	}
	
	/**
	 * 
	 * @param fileName
	 */
	private void writeDataFile(String fileName) {
		int count = chars.length();
		int posX[] = new int[count];
		int posY[] = new int[count];
		int posWidth[] = new int[count];
		int posHeight[] = new int[count];
		
		for ( int i = 0; i < count; i++ ) {
			posX[i] = charRects[i].x;
			posY[i] = charRects[i].y;
			posWidth[i] = charRects[i].width;
			posHeight[i] = charRects[i].height;
		}
		
		String fontData[] = new String[fontArray.size()];
		boolean isChar = false;
		for ( int i = 0; i < fontArray.size(); i++ ) {
			int v = fontArray.get(i);
			if ( v == 0 ) {
				isChar = false;
			}
			fontData[i] = isChar ? ( "'" + (char)v + "'") : Integer.toString(v);
			isChar = !isChar;
		}
		JavaFileGenerator java = new JavaFileGenerator();
		java.addConst("FONT_POS_X", posX);
		//java.addConst("FONT_POS_Y", posY);
		java.addConst("FONT_POS_WIDTH", posWidth);
		//java.addConst("FONT_POS_HEIGHT", posHeight);
		java.addConst("FONT_HEIGHT", posHeight[0]);
		java.addTypedConst("char", "FONT_DATA", fontData, 20);
		
		System.out.println("<font name=\"" + name + "\" image=\"" + imgOutName + "\" distance=\""+ distance +"\" height=\""+ height +"\">");
		for ( int i = 0; i < posX.length; i++ ) {
			System.out.println("\t<char char=\"" + chars.charAt(i) + "\" x=\""+posX[i] +"\" width=\""+posWidth[i] + "\"/>");
		}
		System.out.println("</font>");
		
/*
		<font name="FONT_HIGHSCORES" image="hightscore_numbers_m.png" distance="2" height="24">
		<char char="0" x="0" width="18"/>
		<char char="1" x="18" width="14"/>
		<char char="2" x="32" width="20"/>
		<char char="3" x="52" width="20"/>
		<char char="4" x="72" width="19"/>
		<char char="5" x="91" width="17"/>
		<char char="6" x="108" width="17"/>
		<char char="7" x="125" width="15"/>
		<char char="8" x="140" width="17"/>
		<char char="9" x="157" width="19"/>
		<char char="." x="176" width="8"/>
	</font>
*/	
//		java.println();
	}
	
}
	

