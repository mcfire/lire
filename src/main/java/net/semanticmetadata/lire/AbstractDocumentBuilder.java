/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 04.05.13 11:18
 */

package net.semanticmetadata.lire;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.indexing.parallel.ImageInfo;
import net.semanticmetadata.lire.indexing.parallel.WorkItem;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * Abstract DocumentBuilder, which uses javax.imageio.ImageIO to create a BufferedImage
 * from an InputStream.
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:07:39
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class AbstractDocumentBuilder implements DocumentBuilder {
    /**
     * Creates a new Lucene document from an InputStream. The identifier can be used like an id
     * (e.g. the file hashFunctionsFileName or the url of the image). This is a simple implementation using
     * javax.imageio.ImageIO
     *
     * @param image      the image to index. Please note that
     * @param identifier an id for the image, for instance the filename or an URL.
     * @return a Lucene Document containing the indexed image.
     * @see javax.imageio.ImageIO
     */
    public Document createDocument(InputStream image, ImageInfo imageInfo) {
        assert (image != null);
        BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(image);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        Document doc = createDocument(bufferedImage, imageInfo);
        this.addImageInfoFields(doc, imageInfo);
        return doc;
    }
    
    public Document createDocument(BufferedImage image, String imageName) {
    	WorkItem imageInfo = new WorkItem(imageName, null, null, null, null);
    	
    	return this.createDocument(image, imageInfo);
    }
    
    public Document createDocument(InputStream image, String imageName) {
    	WorkItem imageInfo = new WorkItem(imageName, null, null, null, null);
    	
    	return this.createDocument(image, imageInfo);
    }
    
    protected void addImageInfoFields(Document doc, ImageInfo imageInfo) {
    	if (imageInfo == null) return;
    	
    	doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, imageInfo.getTitle(), Field.Store.YES));
    	doc.add(new TextField(DocumentBuilder.FIELD_NAME_DBID, imageInfo.getId(), Field.Store.YES));
    	doc.add(new TextField(DocumentBuilder.FIELD_NAME_TITLE, imageInfo.getTitle(), Field.Store.YES));
    	doc.add(new TextField(DocumentBuilder.FIELD_NAME_TAGS, imageInfo.getTags(), Field.Store.YES));
    	doc.add(new TextField(DocumentBuilder.FIELD_NAME_LOCATION, imageInfo.getLocation(), Field.Store.YES));
    	doc.add(new StringField(DocumentBuilder.FIELD_NAME_LNG, imageInfo.getLng(), Field.Store.YES));
    	doc.add(new StringField(DocumentBuilder.FIELD_NAME_LAT, imageInfo.getLat(), Field.Store.YES));
    }
}
