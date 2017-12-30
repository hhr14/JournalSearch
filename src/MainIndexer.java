import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.NamedNodeMap;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

public class MainIndexer {
	private int docnumber = 0;
	private IndexWriter indexWriter;
	MainIndexer(String textPath, String indexPath, String analyzeway) throws IOException{
		final Path index_path = Paths.get(indexPath);
		Directory directory = FSDirectory.open(index_path);
		Analyzer analyzer = this.getAnalyzer(analyzeway);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		indexWriterConfig.setSimilarity(new BM25Similarity());
		indexWriter = new IndexWriter(directory, indexWriterConfig);
		this.readText(textPath);
	}
	public Analyzer getAnalyzer(String s){
		Analyzer analyzer;
		if (s.equalsIgnoreCase("unigram") || s.equalsIgnoreCase("bigram") || s.equalsIgnoreCase("trigram")){
			final int a;
			if (s.equalsIgnoreCase("unigram")) a = 1;
			else if (s.equalsIgnoreCase("bigram")) a = 2;
			else a = 3;
			analyzer = new Analyzer(){
				@Override
				protected TokenStreamComponents createComponents(String arg0) {
					// TODO Auto-generated method stub
					Tokenizer source = new NGramTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, a, a);
					TokenStream filter = new LowerCaseFilter(source); // filter may be changed....
					return new TokenStreamComponents(source, filter);
				}
			};
		}
		else if (s.equalsIgnoreCase("ikanalyzer")){
			analyzer = new IKAnalyzer(true);
		}
		else{
			analyzer = new SmartChineseAnalyzer();
		}
		return analyzer;
	}
	public Map<String, String> translation_init(){
		Map<String, String> translation = new HashMap<String, String>();
		translation.put("题名", "title");
		translation.put("英文篇名", "eng_title");
		translation.put("作者", "author");
		translation.put("英文作者", "eng_author");
		translation.put("单位", "work_place");
		translation.put("来源", "origin");
		translation.put("出版单位", "press");
		translation.put("关键词", "keyword");
		translation.put("英文关键词", "eng_keyword");
		translation.put("摘要", "abstract");
		translation.put("英文摘要", "eng_abstract");
		translation.put("年", "year");
		translation.put("专题名称", "subject_name");
		translation.put("分类名称", "class_name");
		translation.put("出版日期", "publish_date");
		translation.put("英文刊名", "eng_magazine_name");
		translation.put("基金", "fund");
		translation.put("REC", "REC");
		return translation;
	}
	public String toBlankString(Map<String, String>map, String key){
		if (!map.containsKey(key)){
			return "";
		}
		return map.get(key);
	}
	public void add_to_index(Map<String, String>map) throws IOException{
		if (map == null)
			return;
		Document document = new Document();
		document.add(new StringField("id", String.valueOf(docnumber), Field.Store.YES));
		document.add(new TextField("title", this.toBlankString(map, "title"), Field.Store.YES));
		document.add(new TextField("eng_title", this.toBlankString(map, "eng_title"), Field.Store.YES));
		document.add(new TextField("author", this.toBlankString(map, "author"), Field.Store.YES));
		document.add(new TextField("eng_author", this.toBlankString(map, "eng_author"), Field.Store.YES));
		document.add(new TextField("work_place", this.toBlankString(map, "work_place"), Field.Store.YES));
		document.add(new TextField("origin", this.toBlankString(map, "origin"), Field.Store.YES));
		document.add(new TextField("press", this.toBlankString(map, "press"), Field.Store.YES));
		document.add(new TextField("keyword", this.toBlankString(map, "keyword"), Field.Store.YES));
		document.add(new TextField("eng_keyword", this.toBlankString(map, "eng_keyword"), Field.Store.YES));
		document.add(new TextField("abstract", this.toBlankString(map, "abstract"), Field.Store.YES));
		document.add(new TextField("eng_abstract", this.toBlankString(map, "eng_abstract"), Field.Store.YES));
		document.add(new StringField("year", this.toBlankString(map, "year"), Field.Store.YES));
//		document.add(new TextField("year_str", this.toBlankString(map, "year"), Field.Store.YES));
		document.add(new TextField("subject_name", this.toBlankString(map, "subject_name"), Field.Store.YES));
		document.add(new TextField("class_name", this.toBlankString(map, "class_name"), Field.Store.YES));
		document.add(new TextField("publish_date", this.toBlankString(map, "publish_date"), Field.Store.YES));
		document.add(new TextField("eng_magazine_name", this.toBlankString(map, "eng_magazine_name"), Field.Store.YES));
		document.add(new TextField("fund", this.toBlankString(map, "fund"), Field.Store.YES));
		docnumber ++;
		indexWriter.addDocument(document);
	}
	public void readText(String filepath) throws IOException{
		File file = new File(filepath);
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String line = null;
		Map<String, String> translation = this.translation_init();
		Map<String, String> now_doc = null;
		String lastAttr = null;
		String chAttr = null;
		Pattern pattern = Pattern.compile("[^<>]+");
		int count = 0;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.charAt(0) != '<'){
				now_doc.put(lastAttr, now_doc.get(lastAttr) + line);
			}
			else{
				Matcher matcher = pattern.matcher(line);
				while(matcher.find()){
					chAttr = matcher.group(0);
					break;
				}
				if (translation.containsKey(chAttr)){
					String myAttr = translation.get(chAttr);
					if (myAttr.equalsIgnoreCase("REC")){
						count ++;
						if (count % 1000 == 0)
							System.out.println(count);
						this.add_to_index(now_doc);
						now_doc = new HashMap<String, String>();
						continue;
					}
					String content = line.split("=")[1];
					now_doc.put(myAttr, content);
					lastAttr = myAttr;
				}
			}
		}
		indexWriter.close();
		reader.close();
	}
	public static void main(String args[])
	throws Exception{
		String indexList[] = {"index/unigram", "index/bigram", "index/trigram", "index/ikanalyzer", "index/smartCN"};
		String analysisList[] = {"unigram", "bigram", "trigram", "ikanalyzer", "smartCN"};
		MainIndexer mainindexer = null;
		for (int i = 0;i < indexList.length;i ++)
			mainindexer = new MainIndexer("./data/CNKI_journal_v2.txt", indexList[i], analysisList[i]);
	}
}
