Optical Character Recognition 
==============================
This package contains a very simple basic ocr package. 
It works by analyzing an image to look for horizontal and vertical white lines(gaps between lines and characters).
Then based on a training file (an image file which has all characters to be recognized in the desired font to be recognized),
the TextRecognizer tries to find the best matched character in the training file. 

It works only when your scanned files are scanned perfectly horizontally. Code is too simplistic to detect slants. 
For more complex needs, you may want to use tessaract kind of tools. 
