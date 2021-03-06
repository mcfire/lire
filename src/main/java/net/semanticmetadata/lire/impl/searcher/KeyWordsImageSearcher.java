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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.impl.searcher;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import net.semanticmetadata.lire.indexing.parallel.ImageInfo;
import net.semanticmetadata.lire.indexing.parallel.WorkItem;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * @author mc
 *
 */
public class KeyWordsImageSearcher extends AbstractImageSearcher {
	
	//最大检索结果数量
    private int numMaxHits;
    //查询解析器
    MultiFieldQueryParser qp;

    public KeyWordsImageSearcher(int numMaxHits) {
    	this.numMaxHits = numMaxHits;
    	//构建支持中文分词的查询解析器
    	qp = new MultiFieldQueryParser(LuceneUtils.LUCENE_VERSION, 
    						new String[]{DocumentBuilder.FIELD_NAME_TITLE, 
    									DocumentBuilder.FIELD_NAME_TAGS,
    									DocumentBuilder.FIELD_NAME_LOCATION}, 
    									new IKAnalyzer());
    }
    
    public ImageSearchHits search(BufferedImage image, ImageInfo imageInfo, IndexReader reader) throws IOException {
    	
    	if (imageInfo.getTitle() == null || imageInfo.getTitle().length() == 0) return null;
    	
    	SimpleImageSearchHits sh = null;
    	//由Lucene的IndexReader创建IndexSearcher
        IndexSearcher isearcher = new IndexSearcher(reader);
        //图像的标题中存放的是用户的语音命令。
        String queryString = imageInfo.getTitle();
        Query tq = null;
        try {
        	//对语音关键词进行分词，返回查询对象
            tq = qp.parse(queryString);
            //进行文档查询
            TopDocs docs = isearcher.search(tq, numMaxHits);
            LinkedList<SimpleResult> res = new LinkedList<SimpleResult>();
            float maxDistance = 0;
            //将查询对象的类型进行转换，同时找到特征距离最近的文档
            for (int i = 0; i < docs.scoreDocs.length; i++) {
                float d = 1f / docs.scoreDocs[i].score;
                maxDistance = Math.max(d, maxDistance);
                SimpleResult sr = new SimpleResult(d, reader.document(docs.scoreDocs[i].doc), i);
                res.add(sr);
            }
            //构建查询结果
            sh = new SimpleImageSearchHits(res, maxDistance);
        } catch (ParseException e) {
            System.err.println(queryString);
            e.printStackTrace();
        }
        return sh;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
    	return this.search(image, null, reader);
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
    	ImageInfo imageInfo = new WorkItem(null, doc.get(DocumentBuilder.FIELD_NAME_TITLE), 
    					null, DocumentBuilder.FIELD_NAME_TAGS, null);
    	BufferedImage image = null;
        return this.search(image, imageInfo, reader);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
