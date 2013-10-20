package com.venky.ocr;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {
	public static boolean isLineBlank(BufferedImage img, int y,int fromX,int toX){
		boolean blank = true;
		for (int x = fromX ; blank && x <= toX ; x++){
			int rgb = img.getRGB(x,y);
		    int red = ((rgb >> 16) & 0xff); 
		    int green = ((rgb >> 8) & 0xff); 
		    int blue = ((rgb ) & 0xff); 
		    if ((red | green |blue) == 0){
		    	blank = false;
		    }
		    x ++;
	    }
		return blank;
	}
	public static boolean isColumnBlank(BufferedImage img, int x,int fromY,int toY){
		boolean blank = true;
		for (int y = fromY ; blank && y <= toY ;y ++){
			int rgb = img.getRGB(x,y);
		    int red = ((rgb >> 16) & 0xff); 
		    int green = ((rgb >> 8) & 0xff); 
		    int blue = ((rgb ) & 0xff); 
		    if ((red | green |blue) == 0){
		    	blank = false;
		    }
		}
		return blank;
	}
	
	public static BufferedImage copy(BufferedImage src){
		ConvolveOp op = new ConvolveOp(new Kernel(1,1,new float[]{1.0f}),ConvolveOp.EDGE_NO_OP,defaultRenderingHints());
		return op.filter(src, null);
	}
	
	private static RenderingHints defaultRenderingHints(){
		Map<Key,Object> map = new HashMap<Key,Object>();
		
		map.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return new RenderingHints(map);
	}
	public static BufferedImage blur(BufferedImage src){
		float oneninth = 1.0f/9;
		float[] kernel = new float[]{oneninth,oneninth,oneninth,
								     oneninth,oneninth,oneninth,
								     oneninth,oneninth,oneninth,
				};

		ConvolveOp blur = new ConvolveOp(new Kernel(3, 3, kernel),ConvolveOp.EDGE_NO_OP,defaultRenderingHints());
		return blur.filter(src, null);
		
	}
	public static BufferedImage sharpen(BufferedImage src){
		float[] kernel = new float[]{0f,-1f, 0f,
									-1f, 5f, -1f,
									0f, -1f, 0f
				};
		ConvolveOp sharpen = new ConvolveOp(new Kernel(3, 3, kernel));
		return sharpen.filter(src, null);
	}
	public static BufferedImage scalefast(BufferedImage src,int targetW, int targetH){
		BufferedImage tmp = new BufferedImage(targetW, targetH,src.getType());
		Graphics2D g = tmp.createGraphics();
		g.setRenderingHints(defaultRenderingHints());
		g.drawImage(src, 0,0,targetW ,targetH,null);
		g.dispose();
		
		return tmp;
	}
	public static BufferedImage scale(BufferedImage src,int targetW, int targetH){
		
		int origW = src.getWidth(); 
		int origH = src.getHeight();
		
		BufferedImage dst = src; 
		double stepUpScale = 1.5;
		double stepDownScale = 1/stepUpScale;
		
		int w = origW ;
		int h = origH; 
		do {
			if (w > targetW){
				w = (int)(w * stepDownScale);
				if (w < targetW){
					w = targetW;
				}
			}
			if (h > targetH){
				h = (int)(h * stepDownScale);
				if (h < targetH){
					h = targetH;
				}
			}
			if (w < targetW){
				w = (int)(w * stepUpScale); 
				if (w > targetW){
					w = targetW;
				}
			}
			if (h < targetH){
				h = (int)(h * stepUpScale); 
				if (h > targetH){
					h =targetH;
				}
			}
			
			BufferedImage tmp = new BufferedImage(w, h,dst.getType());
			Graphics2D g = tmp.createGraphics();
			g.setRenderingHints(defaultRenderingHints());
			g.drawImage(dst, 0,0,w ,h,null);
			g.dispose();
			dst = tmp;
		}while (w != targetW || h != targetH);
		return dst;
	}
	
	public static BufferedImage threshold(BufferedImage src ,int minThreshold){
		BufferedImage img = copy(src);
		for (int x = 0 ;  x < img.getWidth(); x ++ ){
			for (int y = 0 ; y < img.getHeight() ; y ++){
				int rgb = img.getRGB(x, y);
				
				//int alpha = ((rgb >> 24) & 0xff); 
			    int red = ((rgb >> 16) & 0xff); 
			    int green = ((rgb >> 8) & 0xff); 
			    int blue = ((rgb ) & 0xff); 
			    int g = (red & green & blue) ;
			    if (g < minThreshold ){
			    	g = 0;
			    }else {
			    	g = 255;
			    }
			    
			    rgb =  (0 << 24) | (g << 16) | (g << 8) | g; 
			    img.setRGB(x, y, rgb);
			}
		}
		return img;
	}
	public static double distanceBetween(BufferedImage o1, BufferedImage o2){
		double m = 0 ;
		for (int x = 0 ; x < o1.getWidth() ; x++){
			for (int y = 0 ; y < o1.getHeight() ; y ++){
				try {
					int rgb1 = o1.getRGB(x, y); 
					int rgb2 = o2.getRGB(x, y);
					int alpha1 = ((rgb1 >> 24) & 0xff); 
					int alpha2 = ((rgb2 >> 24) & 0xff);
					int r1 = ((rgb1 >> 16) & 0xff);
					int r2 = ((rgb2 >> 16) & 0xff);
					int g1 = ((rgb1 >> 8) & 0xff);
					int g2 = ((rgb2 >> 8)  & 0xff);
					int b1 = (rgb1 & 0xff);
					int b2 = (rgb2 & 0xff);
					assert alpha1 == alpha2 ;
					assert r1 == 0 || r1 == 255 ;
					assert r2 == 0 || r2 == 255 ;
					assert g1 == 0 || g1 == 255 ;
					assert g2 == 0 || g2 == 255 ;
					assert b1 == 0 || b1 == 255 ;
					assert b2 == 0 || b2 == 255 ;
					
					m += (Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2) + Math.abs(alpha1 - alpha2));
					//m += Math.abs(rgb1 - rgb2);
				}catch (Exception ex){
					throw new RuntimeException("x="+x + "y=" + y + "("+ o1.getWidth() + "," + o2.getWidth() + ")" + "("+ o1.getHeight() + "," + o2.getHeight() + ")",ex);
				}
			}
		}
		return m;
	}
}
