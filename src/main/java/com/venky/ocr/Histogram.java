package com.venky.ocr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.venky.ocr.TextRecognizer.CharacterRegion;

public class Histogram {
	private SortedMap<Integer, List<Character>> heightClassification = new TreeMap<Integer, List<Character>>(); 
	private SortedMap<Integer, List<Character>> widthClassification = new TreeMap<Integer, List<Character>>();
	public Histogram(Map<Character,CharacterRegion> trainingMap){
		for (Character c: trainingMap.keySet()){
			CharacterRegion reg = trainingMap.get(c);
			List<Character> sameHeightCharacters = heightClassification.get(reg.height());
			List<Character> sameWidthCharacters = widthClassification.get(reg.width());
			if (sameHeightCharacters == null){
				sameHeightCharacters = new ArrayList<Character>();
				heightClassification.put(reg.height(), sameHeightCharacters);
			}
			if (sameWidthCharacters == null){
				sameWidthCharacters = new ArrayList<Character>();
				widthClassification.put(reg.width(), sameWidthCharacters);
			}
			sameHeightCharacters.add(c);
			sameWidthCharacters.add(c);
		}
	}
	public void printHeightDistribution(){
		for (Integer height:heightClassification.keySet()){
			System.out.println(height +":" + heightClassification.get(height));
		}
	}
	public void printWidthDistribution(){		
		for (Integer width:widthClassification.keySet()){
			System.out.println(width +":" + widthClassification.get(width));
		}
	}
	
	public SortedMap<Integer, List<Character>> getTrainingCharactersByHeight(){
		return heightClassification;
	}
	
	public SortedMap<Integer, List<Character>> getTrainingCharactersByWidth(){
		return widthClassification;
	}
	
	public Collection<Character> getCharactersTallerThan(int height, int numBands){
		int numBandsRet = 0 ;
		List<Character> taller = new ArrayList<Character>();
		for (int h : heightClassification.tailMap(height+1).keySet() ){
			taller.addAll(heightClassification.get(h));
			numBandsRet ++ ;
			if (numBandsRet >= numBands){
				break;
			}
		}
		return taller;
	}

	public Collection<Character> getCharactersThickerThan(int width, int numBands){
		List<Character> thicker = new ArrayList<Character>();
		int numBandsRet = 0 ;
		for (int w : widthClassification.tailMap(width+1).keySet() ){
			thicker.addAll(widthClassification.get(w));
			numBandsRet ++ ;
			if (numBandsRet >= numBands){
				break;
			}
		}
		return thicker;
	}
	
}
