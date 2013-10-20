package com.venky.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class TextRecognizer {
	private boolean debug = false ;
	
	public boolean isDebug() {
		return debug;
	}
	
	public TextRecognizer() {
		this("monospace",false);
	}

	private Properties properties = new Properties();
	private Histogram histogram = null ;
	public Histogram getHistogram(){
		return histogram;
	}
	private int getMinCharacterWidth() {
		return Integer.valueOf(
				properties.getProperty("minCharacterWidth", "10")).intValue();
	}

	private String getLimitedTrainingCharacters() {
		return properties.getProperty("limitedTrainingCharacters");
	}
	
	private double getRelativeSizeThresholdFraction(){
		return Double.valueOf(properties.getProperty("relativeSizeThresholdFraction","0.1")).doubleValue();
	}
	public int getMinColorThreshold(){
		return Integer.valueOf(properties.getProperty("minColorThreshold","127")).intValue();
	}
	
	public BufferedImage threshold(BufferedImage img){
		return ImageUtil.threshold(img, getMinColorThreshold());
	}

	public TextRecognizer(String font){
		this(font,true);
	}
	public TextRecognizer(String font,boolean debug) {
		this.debug = debug;
		load(font, properties);
		List<Line> lines = getLines(read("/com/venky/ocr/"+ font + ".jpg"), getMinCharacterWidth());
		assert (lines.size() >= 1);
		String trainingCharacters = getLimitedTrainingCharacters();
		int c = 33;
		int i = 0 ;
		for (Line line : lines) {
			for (CharacterRegion cr : line.characterImages) {
				if (trainingCharacters == null
						|| trainingCharacters.indexOf(c) >= 0) {
					trainingMap.put(((char) c), cr);
				}
				if (isDebug()){
					cr.write("debug/training-"+i +".jpg");
					i++;
				}
				c++;
			}
		}
		histogram = new Histogram(trainingMap);
		//System.out.println(trainingMap.size());
		assert trainingMap.size() == (126 - 33 + 1);
	}
	
	

	private TreeMap<Character, CharacterRegion> trainingMap = new TreeMap<Character, CharacterRegion>();

	private static class AbstractCharacterRegion {
		int startRow = -1;
		int endRow = -1;
		int startColumn = -1;
		int endColumn = -1;

		public int height() {
			return (endRow - startRow) + 1;
		}

		public int width() {
			return (endColumn - startColumn) + 1;
		}

		public int area() {
			return height() * width();
		}
		
		public int middleRow(){
			return (startRow + endRow)/2;
		}
		public int middleColumn(){ 
			return (startColumn + endColumn)/2;
		}
		
		public int startRow(){
			return startRow;
		}
		public int startColumn(){ 
			return startColumn;
		}
		
		public int endRow(){
			return endRow;
		}
		public int endColumn(){
			return endColumn;
		}
	}

	public static class CharacterRegion extends AbstractCharacterRegion {
		BufferedImage img = null;
		BufferedImage src = null;
		Line line = null;

		public CharacterRegion createCopy() {
			CharacterRegion copy = new CharacterRegion();
			copy.startColumn = this.startColumn;
			copy.startRow = this.startRow;
			copy.endRow = this.endRow;
			copy.endColumn = this.endColumn;
			copy.img = this.img;
			copy.src = this.src;
			copy.line = this.line;
			return copy;
		}

		private BufferedImage croppedImage = null;
		public BufferedImage croppedImage() {
			if (croppedImage != null && (width() != croppedImage.getWidth() || height() != croppedImage.getHeight())){
				croppedImage = null;
			}
			if (croppedImage == null){
				croppedImage = extract();
			}
			return croppedImage;
		}
		
		public void write(String fileName) {
			try {
				ImageIO.write(croppedImage(), fileName.substring(fileName.length()-3), new File(fileName));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private BufferedImage extract() {
			BufferedImage src = new BufferedImage(width(), height(), img.getType());
			for (int x = startColumn; x <= endColumn; x++) {
				for (int y = startRow; y <= endRow; y++) {
					try {
						src.setRGB(x - startColumn, y - startRow,
								img.getRGB(x, y));
					} catch (Exception ex) {
						throw new RuntimeException("x=" + x + "/" + src.getWidth()
								+ "y=" + y + "/" + src.getHeight(), ex);

					}
				}
			}
			return src;
		}

	}

	public static class Line extends AbstractCharacterRegion {
		int lineNumber = 0;
		int sumCharacterWidth = 0;
		int sumCharacterHeight = 0 ;
		CharacterRegion shortest;
		CharacterRegion tallest;
		CharacterRegion widest;
		
		public int avgCharWidth() {
			return sumCharacterWidth / characterImages.size();
		}
		
		public int avgCharHeight() {
			return sumCharacterWidth / characterImages.size() ;
		}
		

		Stack<CharacterRegion> characterImages = new Stack<TextRecognizer.CharacterRegion>();
		
		public CharacterRegion first(){
			if (characterImages.isEmpty()){
				return null;
			}
			return characterImages.get(0);
		}
		public CharacterRegion last(){ 
			if (characterImages.isEmpty()){
				return null;
			}
			return characterImages.peek();
		}
	}

	public CharacterRegion trim(CharacterRegion i) {
		return trimRight(trimBottom(trimTop(i)));
	}

	private CharacterRegion trimBottom(CharacterRegion i) {
		//CharacterRegion c = i.createCopy();
		CharacterRegion c = i;
		int charRow = c.endRow;
		while (charRow > c.startRow) {
			if (ImageUtil.isLineBlank(c.img, charRow, c.startColumn,
					c.endColumn)) {
				charRow--;
			} else {
				c.endRow = Math.min(c.endRow, charRow + 1);
				break;
			}
		}
		return c;
	}

	private CharacterRegion trimTop(CharacterRegion i) {
		//CharacterRegion c = i.createCopy();
		CharacterRegion c = i;
		int charRow = c.startRow;
		while (charRow < c.endRow) {
			if (ImageUtil.isLineBlank(c.img, charRow, c.startColumn,
					c.endColumn)) {
				charRow++;
			} else {
				c.startRow = Math.max(c.startRow, charRow - 1);
				break;
			}
		}
		return c;
	}
	
	private CharacterRegion trimRight(CharacterRegion i) {
		//CharacterRegion c = i.createCopy();
		CharacterRegion c = i;
		int charColumn = c.endColumn;
		while (charColumn > c.startColumn) {
			if (ImageUtil.isColumnBlank(c.img, charColumn, c.startRow,
					c.endRow)) {
				charColumn--;
			} else {
				c.endColumn = Math.min(c.endColumn, charColumn + 1);
				break;
			}
		}
		return c;
	}

	public List<Line> getLines(BufferedImage src) {
		return getLines(src, 1);
	}

	public List<Line> getLines(BufferedImage src, int minCharacterWidth) {
		int lastBlankRow = -1;
		Stack<Line> lines = new Stack<Line>();
		int lineNumber = 0;
		BufferedImage img = threshold(src);
		for (int y = 0; y < img.getHeight(); y++) {
			if (ImageUtil.isLineBlank(img, y, 0, img.getWidth() - 1)) {
				if (lastBlankRow > 0 && y > lastBlankRow + 1) {
					Line line = lines.peek();
					line.endRow = y;
					int lastBlankColumn = -1;
					for (int x = 0; x < img.getWidth(); x++) {
						if (ImageUtil.isColumnBlank(img, x, line.startRow,line.endRow)) {
							if (lastBlankColumn > 0 && x > lastBlankColumn + 1) {
								CharacterRegion c = line.characterImages.peek();
								if (x < c.startColumn + minCharacterWidth) {
									continue;
								}
								c.endColumn = x;
								c.img = img;
								c.src = src;
								trim(c);
								line.sumCharacterWidth += (c.endColumn - c.startColumn);
								line.sumCharacterHeight += (c.endRow - c.startRow);
								if (line.shortest == null || line.shortest.height() > c.height()){
									line.shortest = c;
								}
								if (line.tallest == null || line.tallest.height() < c.height()){
									line.tallest = c;
								}
								if (line.widest == null || line.widest.width() < c.width()){
									line.widest = c;
								}
							}
							lastBlankColumn = x;
						} else if (lastBlankColumn > 0
								&& x == lastBlankColumn + 1) {
							if (!line.characterImages.isEmpty()) {
								CharacterRegion last = line.characterImages
										.peek();
								if (last.endColumn < 0) {
									continue;
								}
							}
							CharacterRegion c = new CharacterRegion();
							c.startColumn = lastBlankColumn;
							c.startRow = line.startRow;
							c.endRow = line.endRow;
							c.line = line;
							line.characterImages.push(c);
						}
					}
				}
				lastBlankRow = y;
			} else if (lastBlankRow > 0 && y == lastBlankRow + 1) {
				Line line = new Line();
				line.startRow = lastBlankRow;
				line.lineNumber = ++lineNumber;
				lines.push(line);
			}
		}
		Iterator<Line> li = lines.iterator();
		while (li.hasNext()){
			Line line = li.next();
			if (line.characterImages.isEmpty() || line.shortest == null){
				li.remove();
			}
		}
		return lines;
	}

	private double getSpaceWidthFraction() {
		return Double.valueOf(
				properties.getProperty("spaceWidthFraction", "0.5"))
				.doubleValue();
	}

	private void recognize(Line line, StringBuffer out) {
		CandidateBand band = getCandidateBand(line);
		CharacterRegion prevCharRegion = null;
		int i = 1;
		for (CharacterRegion charRegion : line.characterImages) {
			if (prevCharRegion != null) {
				double widthOfSpace = charRegion.startColumn - prevCharRegion.endColumn;
				if (widthOfSpace > getSpaceWidthFraction() * line.avgCharWidth()) {
					out.append(" ");
				}
			}
			prevCharRegion = charRegion;
			if (isDebug()){
				charRegion.write("debug/"+i+".jpg");
				i++;
			}
			recognize(charRegion,band, out);
		}
		out.append(System.getProperty("line.separator"));
	}

	private void recognize(CharacterRegion currentRegion, CandidateBand band, StringBuffer out) {
		if (currentRegion.width() < 2){
			return; // Avoid specs.
		}
		Set<Character> trials = new TreeSet<Character>();
		SortedMap<Integer,List<Character>> trainingCharactersByHeightMap = histogram.getTrainingCharactersByHeight();
		double[] scale = new double []{ (1.0 * band.maxHeight)/ currentRegion.line.tallest.height() ,
										(1.0 * band.maxWidth)/ currentRegion.line.widest.width() ,
										}; 
		Arrays.sort(scale);

		int lbmaxHeight = band.minHeight;
		int ubmaxHeight = band.maxHeight; 
		if (band.maxHeight > band.minHeight){
			SortedMap<Integer,List<Character>> tmpTailMap = null ;
			lbmaxHeight = (int)(scale[0] * currentRegion.height());
			tmpTailMap = trainingCharactersByHeightMap.tailMap(lbmaxHeight);
			if (!tmpTailMap.isEmpty()){
				lbmaxHeight = tmpTailMap.firstKey();
			}
			ubmaxHeight = (int)(scale[1] * currentRegion.height());
			tmpTailMap = trainingCharactersByHeightMap.tailMap(ubmaxHeight);
			if (!tmpTailMap.isEmpty()){
				ubmaxHeight = tmpTailMap.firstKey() ;
			}
		}
		
		
		
		for (int i = lbmaxHeight ; i <= ubmaxHeight ; i ++ ){
			List<Character> characters= trainingCharactersByHeightMap.get(i); 
			if (characters != null){
				trials.addAll(characters);
			}
		}
		trials.addAll(histogram.getCharactersTallerThan(ubmaxHeight, 1));
		
		Set<Character> bestKeys = recognize(currentRegion,trials);

		if (!bestKeys.isEmpty()) {
			if (bestKeys.size() == 1) {
				out.append(bestKeys.iterator().next());
			}else if (bestKeys.size() < 7){
				out.append(bestKeys);
			}else {
				out.append("_");
			}
		}
	}
	private Set<Character> recognize(CharacterRegion currentRegion,Collection<Character> against){
		Set<Character> bestKeys = new HashSet<Character>();
		double bestError = Double.POSITIVE_INFINITY;
		for (Character key : against) {
			CharacterRegion trainedRegion = trainingMap.get(key); 
			double error = distanceBetween(trainedRegion, currentRegion);
			
			if (error > bestError) {
				//
			} else if (error < bestError) {
				bestError = error;
				bestKeys.clear();
				bestKeys.add(key);
			} else {
				bestKeys.add(key);
			}

		}
		return bestKeys;
	}

	private boolean muchGreaterThan(double x1 , double x2){
		if (x1 - x2 < getRelativeSizeThresholdFraction() * (x1+x2)){
			return false; 
		}
		return true;
	}
	private boolean wayOff(CharacterRegion c1 , CharacterRegion c2){
		double h1 = c1.height();
		double h2 = c2.height();
		
		double w1 = c1.width();
		double w2 = c2.width();
		
		if (h1 <= h2 && muchGreaterThan(w1,w2)){ 
			return true;
		}
		
		if (h2 <= h1 && muchGreaterThan(w2,w1)){
			return true;
		}
		
		if (w1 <= w2 && muchGreaterThan(h1, h2)){
			return true;
		}
		
		if (w2 <= w1 && muchGreaterThan(h2, h1)){
			return true;
		}
		
		if (h1 <= h2 && w1 <= w2){
			if (muchGreaterThan(h2/h1, w2/w1) || muchGreaterThan(w2/w1, h2/h1)){
				return true;
			}
		}else if (h2 <= h1 && w2 <= w1){
			if (muchGreaterThan(h1/h2, w1/w2) || muchGreaterThan(w1/w2, h1/h2)){
				return true;
			}
		}
		return false;
	}
	
	private double distanceBetween(CharacterRegion trainedRegion, CharacterRegion testingRegion) {
		CharacterRegion r1 = trainedRegion;
		CharacterRegion r2 = testingRegion;

		if (wayOff(r1, r2)){
			return Double.POSITIVE_INFINITY;
		}
		//NEW 
		if (r1.startRow - r1.line.startRow < r1.line.endRow - r1.startRow) { //r1 starts near line top.
			if (r2.startRow - r2.line.startRow > r2.line.endRow - r2.startRow ){ // r2 starts near line floor.
				return Double.POSITIVE_INFINITY;
			}
		}else if (r1.startRow - r1.line.startRow > r1.line.endRow - r1.startRow) {
			if (r2.startRow - r2.line.startRow < r2.line.endRow - r2.startRow ){
				return Double.POSITIVE_INFINITY;
			}
		}
		if (r1.line.endRow - r1.endRow < r1.endRow - r1.line.startRow){
			if (r2.line.endRow - r2.endRow > r2.endRow - r2.line.startRow){
				return Double.POSITIVE_INFINITY;
			}
		}else if (r1.line.endRow - r1.endRow > r1.endRow - r1.line.startRow){
			if (r2.line.endRow - r2.endRow < r2.endRow - r2.line.startRow){
				return Double.POSITIVE_INFINITY;
			}
		}
		
		BufferedImage i1 = r1.croppedImage(); 
		BufferedImage i2 = r2.croppedImage();
		
		if (r2.height() > r1.height()){
			i1 = threshold(ImageUtil.scale(i1, r2.width(),r2.height()));
			i2 = threshold(i2);
		}else {
			i1 = threshold(i1);
			i2 = threshold(ImageUtil.scale(i2, r1.width(),r1.height()));
		}
	
		
		return ImageUtil.distanceBetween(i1, i2);
	}
	public StringBuffer recognize(File in) throws IOException{
		return recognize(in,1);
	}
	public StringBuffer recognize(File in,int minCharWidth) throws IOException {
		return recognize(ImageIO.read(in),minCharWidth);
	}
	public StringBuffer recognize(InputStream in) throws IOException{
		return recognize(in,1);
	}
	public StringBuffer recognize(InputStream in,int minCharWidth) throws IOException {
		return recognize(ImageIO.read(in),minCharWidth);
	}
	public StringBuffer recognize(BufferedImage in) throws IOException{
		return recognize(in, 1);
	}
	public StringBuffer recognize(BufferedImage in, int minCharWidth) throws IOException {
		List<Line> lines = getLines(in,minCharWidth);

		StringBuffer out = new StringBuffer();
		for (Line line : lines) {
			recognize(line, out);
		}
		return out;
	}

	private void load(String font, Properties properties) {
		try {
			properties.load(getClass().getResourceAsStream(
					"/com/venky/ocr/" + font + ".properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private BufferedImage read(String name) {
		BufferedImage img;
		try {
			img = ImageIO.read(getClass().getResourceAsStream(name));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return img;
	}

	public static class CandidateBand {
		int minHeight; 
		int maxHeight;
		int maxWidth;
		public String toString(){
			return "(min:"+minHeight +"max:" +maxHeight +"maxW:" + maxWidth +")";
		}
	}
	
	public CandidateBand getCandidateBand(Line line){
		Set<Character> shortestCharacterChoices = recognize(line.shortest,trainingMap.keySet());
		int minHeight = histogram.getTrainingCharactersByHeight().lastKey();
		for (Character choice:shortestCharacterChoices){
			if (minHeight > trainingMap.get(choice).height()){
				minHeight = trainingMap.get(choice).height();
			}
		}
		Set<Character> widestCharacterChoices = recognize(line.widest, trainingMap.keySet());
		int maxWidth = histogram.getTrainingCharactersByWidth().firstKey(); 
		for (Character choice:widestCharacterChoices){
			if (maxWidth < trainingMap.get(choice).width()){
				maxWidth = trainingMap.get(choice).width();
			}
		}
		
		double hscale = ((1.0 *minHeight)/line.shortest.height() + (1.0 * maxWidth)/line.widest.width())/2.0;
		
		int maxHeightCandidate = minHeight ;
		if (line.tallest.height() > line.shortest.height()){
			SortedMap<Integer,List<Character>> tailMap = histogram.getTrainingCharactersByHeight().tailMap((int)(hscale * line.tallest.height()));
			if (!tailMap.isEmpty()){
				maxHeightCandidate = tailMap.firstKey() ;
			}
		}
		Set<Character> tallestCharacterChoices = recognize(line.tallest, histogram.getCharactersTallerThan(maxHeightCandidate-1,2));
		int maxHeight = histogram.getTrainingCharactersByHeight().firstKey();
		for (Character choice: tallestCharacterChoices){
			if(maxHeight < trainingMap.get(choice).height()){
				maxHeight = trainingMap.get(choice).height();
			}
		}
			
		CandidateBand band = new CandidateBand();
		band.minHeight = minHeight; 
		band.maxHeight = maxHeight;
		band.maxWidth = maxWidth;
		//System.out.println(band);
		return band;
	}

}
