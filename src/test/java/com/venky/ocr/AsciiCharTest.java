package com.venky.ocr;
import org.junit.Test;


public class AsciiCharTest {

	@Test 
	public void testGenerateCharacters() {
		for (int i = 32; (char)i <= '~'  ; i ++){
			System.out.print((char)i + " ");
		}
	}

}
