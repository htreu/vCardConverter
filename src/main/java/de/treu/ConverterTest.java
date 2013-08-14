package de.treu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.treu.VCard2ImageConverter.ExportType;

public class ConverterTest {

	public static void main(String[] args) {
		File vcardFile = new File("/Users/treu/Desktop/htreu.vcf");
		FileOutputStream out = null;
		FileInputStream is = null;
		try {
			is = new FileInputStream(vcardFile);
			byte[] png = new VCard2ImageConverter().convertVCard(is, ExportType.PNG, 900, 450);
			out = new FileOutputStream("/Users/treu/Desktop/vfc.png");
			out.write(png);
			out.flush();
			out.close();
		} catch (IOException e) {
			
		} finally {
			if (out != null) {
				try {out.close();} catch (IOException e) {};
			}
			if (is != null) {
				try {is.close();} catch (IOException e) {};
			}
		}
	}
}
