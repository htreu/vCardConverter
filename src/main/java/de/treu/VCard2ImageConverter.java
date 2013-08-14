package de.treu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.exceptions.VCardParseException;
import net.sourceforge.cardme.vcard.types.AdrType;
import net.sourceforge.cardme.vcard.types.EmailType;
import net.sourceforge.cardme.vcard.types.ImppType;
import net.sourceforge.cardme.vcard.types.PhotoType;
import net.sourceforge.cardme.vcard.types.TelType;

public class VCard2ImageConverter {
	
	/**
	 * Converts a vCard byte[] to an image of type {@link ExportType} with the given width and 
	 * height. Black font on white background.
	 * 
	 * @param vcard the byte[] representing the vCard.
	 * @param type the {@link ExportType} (PNG or JPG).
	 * @param width the width of the resulting image.
	 * @param height the height of the resulting image.
	 * @return a byte[] with the vCard information, encoded in a {@link ExportType} format.
	 * @throws IOException
	 */
	public byte[] convertVCard(byte[] vcard, ExportType type, int width, int height) 
			throws IOException 
	{
		return convertVCard(new ByteArrayInputStream(vcard), type, width, height);
	}
	
	/**
	 * Converts a vCard byte[] to an image of type {@link ExportType} with the given width and 
	 * height. Black font on white background.
	 * 
	 * @param is the {@link InputStream} representing the vCard.
	 * @param type the {@link ExportType} (PNG or JPG).
	 * @param width the width of the resulting image.
	 * @param height the height of the resulting image.
	 * @return a byte[] with the vCard information, encoded in a {@link ExportType} format.
	 * @throws IOException
	 */
	public byte[] convertVCard(InputStream is, ExportType type, int width, int height) 
			throws IOException 
	{
		VCard card = getVCard(is);
		List<String> lines = getVCardText(card);
		byte[] photo = getVCardPhoto(card);
		return renderToImage(lines, photo, type, width, height);
	}

	private byte[] renderToImage(List<String> lines, byte[] photo, ExportType type, int width, 
			int height) throws IOException
	{
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g = image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);

		Font f = g.getFont().deriveFont(25f);
		g.setFont(f);
		g.setColor(Color.BLACK);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int offset = 0;
		for (String line : lines) {
			g.drawString(line, 50, (50 + offset));
			offset += 40;
		}
		
		if (photo != null) {
			BufferedImage i = ImageIO.read(new ByteArrayInputStream(photo));
			g.drawImage(i, new RescaleOp(1f, 1f, null), 650, 20);
		}
		
		g.dispose();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, type.getType(), out);

		return out.toByteArray();
	}
	
	private VCard getVCard(InputStream is) {
		ExtendedVCardEngine engine = new ExtendedVCardEngine();
		try {
			return engine.parse(is);
		} catch (IOException e) {
			throw new IllegalStateException("Error parsing or accessing the vcf card." , e);
		} catch (VCardParseException e) {
			throw new IllegalStateException("Error parsing or accessing the vcf card." , e);
		}
	}

	private List<String> getVCardText(VCard card) {
		List<String> lines = new ArrayList<String>();
		// if (card.getAgents() != null) for (AgentType t :
		// card.getAgents()) System.out.println("agent: " + t.toString());
		// if (card.getCategories() != null)
		// System.out.println("categories: " +
		// card.getCategories().toString());
		// if (card.getExtendedTypes() != null) for (ExtendedType t :
		// card.getExtendedTypes()) System.out.println("extended: " +
		// t.toString());
		// if (card.getKeys() != null) for (KeyType t : card.getKeys())
		// System.out.println("keys: " + t.toString());
		// if (card.getLables() != null) for (LabelType t :
		// card.getLables()) System.out.println("labels: " + t.toString());
		// if (card.getLogos() != null) for (LogoType t : card.getLogos())
		// System.out.println("logo: " + t.toString());
		lines.add("Name: " + getName(card));
		lines.add("Address: " + getAddress(card));
		if (card.getOrg() != null)
			lines.add("Organisation: " + card.getOrg().getOrgName());

		if (card.getTels() != null)
			for (TelType t : card.getTels())
				lines.add("Tel: " + t.getTelephone());
		if (card.getEmails() != null)
			for (EmailType t : card.getEmails())
				lines.add("E-Mail: " + t.getEmail());
		if (card.getIMPPs() != null)
			for (ImppType t : card.getIMPPs())
				lines.add("Messenger: " + t.getUri());
		
		return lines;
	}
	
	private byte[] getVCardPhoto(VCard card) {
		if (card.getPhotos() != null)
			for (PhotoType t : card.getPhotos()) {
				return t.getBinaryData();
			}
		
		return null;
	}

	/**
	 * Returns one of the names from the VCard. The priority is "FN", "N", "Name", "Nicknames".
	 * @param card the vCard object.
	 * @return the name in the vCard
	 */
	private static String getName(VCard card) {
		if (card.getFN() != null)
			return card.getFN().getFormattedName();
		if (card.getN() != null)
			return card.getN().getGivenName();
		if (card.getName() != null)
			return card.getName().getName();
		if (card.getNicknames() != null)
			return listToString(card.getNicknames().getNicknames());
		return "";
	}

	/**
	 * Returns a concatenated String of all addresses in the vCard.
	 * @param card the vCard object.
	 * @return a concatenated String of all addresses in the vCard.
	 */
	private static String getAddress(VCard card) {
		StringBuilder sb = new StringBuilder();
		if (card.getAdrs() != null)
			for (AdrType t : card.getAdrs()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(t.getStreetAddress()).append(", ")
						.append(t.getPostalCode()).append(" ")
						.append(t.getLocality());
			}

		return sb.toString();
	}

	private static String listToString(List<String> list) {
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	
	/**
	 * Export type for the resulting image. Options are JPG and PNG.
	 *  
	 * @author treu
	 *
	 */
	public static enum ExportType {
		
		JPG("jpg"),
		PNG("png");
		
		public String getType() {
			return type;
		}
		
		private String type;
		
		private ExportType(String type) {
			this.type = type;
		}
	}

}
