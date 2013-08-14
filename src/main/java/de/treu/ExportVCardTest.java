package de.treu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportVCardTest {

	public static void main(String[] args) {
		File vcardFile = new File("/Users/treu/Desktop/htreu.vcf");
		VCard2ImageConverter export = new VCard2ImageConverter();
		try {
			byte[] png = export.convertVCard();
			FileOutputStream out = new FileOutputStream("/Users/treu/Desktop/vfc.png");
			out.write(png);
			out.flush();
			out.close();
		} catch (IOException e) {
			
		}
	}
}
