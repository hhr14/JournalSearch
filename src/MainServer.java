import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.search.BooleanClause;
public class MainServer extends HttpServlet{
	private int fuzz_number = 2; // set w2c fuzz number.
	private String[] checkinputName = {"author_input", "title_input", "press_input", "time_from_input", "time_to_input"};
	private String[] fieldList = {"author", "title", "press", "time_from", "time_to"};
	private List<String> option_str = Arrays.asList("ikanalyzer", "unigram","bigram", "trigram", "smartCN");
	private Analyzer analyzer;
	private IndexSearcher searcher;
	String indexPath = "JournalSearch/index/";
	String w2cPath = "JournalSearch/word2vec/output.txt";
	private ScoreDoc[] nowHits;
	private String nowInput[];
	private String nowfuzz;
	private String nowQueryString;
	private Query nowQuery;
	private int nowOption;
	private Map<String, String> w2c;
	public MainServer() throws IOException{
		super();
		analyzer = new IKAnalyzer(true);
		searcher = null;
		w2c = new HashMap<String, String>();
		this.readw2c();
//		System.out.println(this.w2c.get("计算机").get(1));
//		searcher = this.getSearcher(indexPath);
		
		nowHits = null;
		nowInput = new String[checkinputName.length];
		nowfuzz = null;
		nowQueryString = null;
		nowQuery = null;
		nowOption = 0;
	}
	
	public void readw2c(){
		FileReader reader;
		try {
			reader = new FileReader(this.w2cPath);
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			List<String> wordList = new ArrayList<String>();
			try {
				while((line = br.readLine()) != null){
					String wordindex = line.split(" ")[0];
					wordList.clear();
					String wordarray = "";
					for (int i = 0;i < this.fuzz_number;i ++){
						wordarray += (line.split(" ")[2 * (i+1)] + " ");
					}
					this.w2c.put(wordindex, wordarray);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public IndexSearcher getSearcher(String nowPath) throws IOException{
		Path index_path = Paths.get(nowPath);
		Directory directory = FSDirectory.open(index_path);
		IndexReader t_reader = DirectoryReader.open(directory);
		IndexSearcher t_searcher = new IndexSearcher(t_reader);
		return t_searcher;
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
	
	public String get_fuzz_w2c(String queryString, String fuzzString) throws IOException{
		if (fuzzString.equalsIgnoreCase(""))
			return queryString;
//		Process p;
//		String fuzz_number_str = String.valueOf(fuzz_number);
//		String cmd = "./JournalSearch/data/distance JournalSearch/data/vectors.bin " + fuzzString + " " + fuzz_number_str;
//		p = Runtime.getRuntime().exec(cmd);
//		InputStream fis = p.getInputStream();
//		InputStreamReader isr=new InputStreamReader(fis);
//		BufferedReader br=new BufferedReader(isr);
//		String line = null;
//		String replaceString = "(" + fuzzString;
//		while((line = br.readLine()) != null){
//			line = line.trim();
//			if (line.equalsIgnoreCase("-1"))
//				return queryString;
//			replaceString = replaceString + " OR " + line;
//		}
//		replaceString = replaceString + ")";
		String replaceString = "(" + fuzzString;
		String res = this.w2c.get(fuzzString);
		if (res == null)
			return queryString;
		for (int i = 0;i < this.fuzz_number;i ++){
			replaceString = replaceString + " OR " + res.split(" ")[i];
		}
		replaceString += ")";
		String result = queryString.replaceAll(fuzzString, replaceString);
		return result;
	}
	
	public String getHighLight(String content){
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(this.nowQuery));
		highlighter.setTextFragmenter(new SimpleFragmenter(100));
		TokenStream tokenstream = this.analyzer.tokenStream("", new StringReader(content));
		String res = "";
		try {
			res = highlighter.getBestFragment(tokenstream, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (res == null)
			return content;
		return res;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String page = request.getParameter("page");
		ScoreDoc[] hits = null;
		if (page == null){
			String analysisWay = request.getParameter("analysisSelect");
			this.analyzer = this.getAnalyzer(analysisWay);
			this.searcher = this.getSearcher(this.indexPath + analysisWay);
			Query query = null;
			String queryString = request.getParameter("query");
			this.nowQueryString = queryString;
			String[] check_input = new String[checkinputName.length];
			String fuzz_input = request.getParameter("fuzz_input");
			for (int k = 0;k < checkinputName.length;k ++){
				check_input[k] = request.getParameter(checkinputName[k]);
			}
			queryString = get_fuzz_w2c(queryString, fuzz_input);
			String finalString = queryString;
			String lastString = "";
			String timeString = "";
			for (int k = 0;k < this.fieldList.length;k ++){
				if (this.fieldList[k].equalsIgnoreCase("time_from")){
					if (!check_input[k].equalsIgnoreCase(""))
						timeString += (" AND year:[" + check_input[k] + " TO ");
					continue;
				}
				if (this.fieldList[k].equalsIgnoreCase("time_to")){
					if (!timeString.equalsIgnoreCase("") && !check_input[k].equalsIgnoreCase("")){
						timeString += (check_input[k] + "]");
						lastString += timeString;
					}
					continue;
				}
					
				String keyword = null;
				if (check_input[k].equalsIgnoreCase("")){
					if(!queryString.equalsIgnoreCase("")){
						finalString += (" " + this.fieldList[k] + ":(");
						keyword = queryString;
						finalString += (keyword + ")");
					}
				}
				else{
					lastString += (" AND " + this.fieldList[k] + ":(");
					keyword = check_input[k];
					lastString += (keyword + ")");
				}
			}
			if (!finalString.equalsIgnoreCase(""))
				finalString = "(" + finalString + ")" + lastString;
			else if (!lastString.equalsIgnoreCase(""))
				finalString = lastString.substring(5);
			System.out.println(finalString);
			if (!finalString.equalsIgnoreCase("")){
				QueryParser parser = new QueryParser("abstract", this.analyzer);
				try {
					query = parser.parse(finalString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
	
				TopDocs topDocs = searcher.search(query, 1000);
				hits = topDocs.scoreDocs;
				this.nowHits = hits;
				this.nowInput = check_input;
				this.nowfuzz = fuzz_input;
				this.nowQuery = query;
				this.nowOption = this.option_str.indexOf(analysisWay);
				page = "1";
			}
			else{
				request.getRequestDispatcher("/JournalSearch.jsp").forward(request, response);
				return;
			}
		}
		else
			hits = this.nowHits;
		int page_number = Integer.parseInt(page);
		String titleList[] = new String[10];
		String authorList[] = new String[10];
		String abstractList[] = new String[10];
		String pressList[] = new String[10];
		String yearList[] = new String[10];
		String totalLength = "5";
		int nowPos;
		int minPage;
		int totalPage = (int)Math.ceil((double)hits.length / (double)10);
		if (page_number <= 1) page_number = 1;
		if (page_number >= totalPage) page_number = totalPage;
		System.out.println("totalPage is:" + totalPage);
		System.out.println("nowPage is:" + page_number);
		if (hits.length <= 50)
			totalLength = String.valueOf(totalPage);
		if (page_number <= 2) nowPos = page_number;
		else if (totalPage - page_number <= 1) nowPos = Integer.parseInt(totalLength) - (totalPage - page_number);
		else nowPos = 3;
		minPage = page_number - nowPos + 1;
		int start = 10*(page_number - 1);
		System.out.println("hits length:" + String.valueOf(hits.length));
		System.out.println(page_number);
		if (hits.length != 0){
			for (int i = 10*(page_number - 1);i < Math.min(hits.length, 10*page_number);i ++){
				Document doc = searcher.doc(hits[i].doc);
				titleList[i - start] = this.getHighLight(doc.get("title"));
				authorList[i - start] = this.getHighLight(doc.get("author"));
				abstractList[i - start] = this.getHighLight(doc.get("abstract"));
				pressList[i - start] = this.getHighLight(doc.get("press"));
				yearList[i - start] = this.getHighLight(doc.get("year"));
			}
		}
		else {
			titleList = null;
			totalLength = null;
		}
		request.setAttribute("option", this.nowOption);
		request.setAttribute("isSearch", "true");
		request.setAttribute("totalLength", totalLength);
		request.setAttribute("nowPos", nowPos);
		request.setAttribute("minPage", minPage);
		for (int i = 0;i < this.nowInput.length;i ++)
			request.setAttribute(this.checkinputName[i], this.nowInput[i]);
		request.setAttribute("hitslength", hits.length);
		request.setAttribute("titleList", titleList);
		request.setAttribute("authorList", authorList);
		request.setAttribute("pressList", pressList);
		request.setAttribute("abstractList", abstractList);
		request.setAttribute("yearList", yearList);
		request.setAttribute("fuzz_input", this.nowfuzz);
		request.setAttribute("currentQuery", this.nowQueryString);
		request.setAttribute("currentPage", String.valueOf(page_number));
		request.getRequestDispatcher("/JournalSearch.jsp").forward(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		this.doGet(request, response);
	}
	
	public static void main(String args[]) throws IOException, ParseException{
		MainServer m = new MainServer();

//		QueryParser parser = new QueryParser("abstract", m.analyzer);
//		Query query = parser.parse("title:煤矿 year:[2010 TO 2010]^10");
////		Query q = IntPoint.newRangeQuery("year", 2010, 2010);
////		BooleanQuery.Builder  builder=new BooleanQuery.Builder();
////		builder.add(query, BooleanClause.Occur.MUST);
////	    builder.add(q, BooleanClause.Occur.MUST);
////	    BooleanQuery b = builder.build();
//		TopDocs topDocs = m.searcher.search(query, 10);
//		ScoreDoc[] hits = topDocs.scoreDocs;
//		for (int i = 0;i < hits.length;i ++){
//			Document doc = m.searcher.doc(hits[i].doc);
//			System.out.println("title:" + doc.get("title"));
//			System.out.println("author:" + doc.get("author"));
//			System.out.println("abstract:" + doc.get("abstract"));
//			System.out.println("year" + doc.get("year"));
//		}
//		
//		System.out.println("\n");
//		String content = m.searcher.doc(hits[0].doc).get("year");
//		System.out.println(content);
//		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
//		Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));
//		highlighter.setTextFragmenter(new SimpleFragmenter(100));
//		TokenStream tokenstream = m.analyzer.tokenStream("", new StringReader(content));
//		try {
//			String res = highlighter.getBestFragment(tokenstream, content);
//			System.out.println(res);
//		} catch (InvalidTokenOffsetsException e) {
//			e.printStackTrace();
//		}
		
	}
}
