package com.venky.ocr;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
 
public class TextRecognitionTest {
	@Test
	public void testTestFile() throws IOException{
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/monospace.jpg"),8);
		System.out.println(out.toString());
		Assert.assertEquals("","! \" # $ % & ' ( ) * + , - . / 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F G H I J K L M N O P Q R S T U V W X Y Z [ \\ ] ^ _ ` a b c d e f g h i j k l m n o p q r s t u v w x y z { | } ~",out.toString().trim());
	}

	@Test
	public void testHistogram(){
		TextRecognizer monospace = new TextRecognizer();
		monospace.getHistogram().printHeightDistribution();
	}
	@Test
	public void testf() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/f.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","f",out.toString().trim());
	}
	@Test
	public void testa_z() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/a-z.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","abcdefghijklmnopqrstuvwxyz",out.toString().trim());
	}
	@Test
	public void testbeautiful() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/beautiful.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","beautiful font",out.toString().trim());
	}
	@Test
	public void testXdot() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/Xdot.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","X .",out.toString().trim());
	}

	@Test
	public void teststress() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/stress.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","Ubuntu is beautiful os.\ncopsuvwxyz",out.toString().trim());
	}
	@Test
	public void testUu() throws IOException {
		TextRecognizer monospace = new TextRecognizer();
		StringBuffer out = monospace.recognize(getClass().getResourceAsStream("/com/venky/ocr/Uu.jpg"));
		System.out.println(out.toString());
		Assert.assertEquals("","Uu",out.toString().trim());
	}
} 
