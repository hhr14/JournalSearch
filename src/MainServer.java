import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
	String path = "JournalSearch/";
	private int fuzz_number = 2; // set w2c fuzz number.
	private String[] checkinputName = {"author_input", "title_input", "press_input", "time_from_input", "time_to_input"};
	private String[] fieldList = {"author", "title", "press", "time_from", "time_to"};
	private Analyzer analyzer;
	private IndexSearcher searcher;
	private IndexReader reader;
	String indexPath = "JournalSearch/index";
	private ScoreDoc[] nowHits;
	private String nowInput[];
	private String nowfuzz;
	private String nowQueryString;
	private Query nowQuery;
	public MainServer() throws IOException{
		super();
		analyzer = new IKAnalyzer(true);
		final Path index_path = Paths.get(indexPath);
		Directory directory = FSDirectory.open(index_path);
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		
		nowHits = null;
		nowInput = new String[checkinputName.length];
		nowfuzz = null;
		nowQueryString = null;
		nowQuery = null;
	}
	
	public String get_fuzz_w2c(String queryString, String fuzzString) throws IOException{
		if (fuzzString.equalsIgnoreCase(""))
			return queryString;
		Process p;
		String fuzz_number_str = String.valueOf(fuzz_number);
		String cmd = "./JournalSearch/data/distance JournalSearch/data/vectors.bin " + fuzzString + " " + fuzz_number_str;
		p = Runtime.getRuntime().exec(cmd);
		InputStream fis = p.getInputStream();
		InputStreamReader isr=new InputStreamReader(fis);
		BufferedReader br=new BufferedReader(isr);
		String line = null;
		String replaceString = "(" + fuzzString;
		while((line = br.readLine()) != null){
			line = line.trim();
			if (line.equalsIgnoreCase("-1"))
				return queryString;
			replaceString = replaceString + " OR " + line;
		}
		replaceString = replaceString + ")";
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
						finalString = "(" + finalString + ")" + timeString;
					}
					continue;
				}
					
				String keyword = null;
				if (check_input[k].equalsIgnoreCase("")){
					finalString += (" " + this.fieldList[k] + ":(");
					keyword = queryString;
				}
				else{
					finalString += (" AND " + this.fieldList[k] + ":(");
					keyword = check_input[k];
				}
				finalString += (keyword + ")");
			}
			QueryParser parser = new QueryParser("abstract", this.analyzer);
			System.out.println(finalString);
			try {
				query = parser.parse(finalString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			TopDocs topDocs = searcher.search(query, 100);
			hits = topDocs.scoreDocs;
			this.nowHits = hits;
			this.nowInput = check_input;
			this.nowfuzz = fuzz_input;
			this.nowQuery = query;
			page = "1";
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
		QueryParser parser = new QueryParser("abstract", m.analyzer);
		Query query = parser.parse("title:煤矿 year:[2010 TO 2010]^10");
//		Query q = IntPoint.newRangeQuery("year", 2010, 2010);
//		BooleanQuery.Builder  builder=new BooleanQuery.Builder();
//		builder.add(query, BooleanClause.Occur.MUST);
//	    builder.add(q, BooleanClause.Occur.MUST);
//	    BooleanQuery b = builder.build();
		TopDocs topDocs = m.searcher.search(query, 10);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0;i < hits.length;i ++){
			Document doc = m.searcher.doc(hits[i].doc);
			System.out.println("title:" + doc.get("title"));
			System.out.println("author:" + doc.get("author"));
			System.out.println("abstract:" + doc.get("abstract"));
			System.out.println("year" + doc.get("year"));
		}
		
		System.out.println("\n");
		String content = m.searcher.doc(hits[0].doc).get("year");
		System.out.println(content);
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));
		highlighter.setTextFragmenter(new SimpleFragmenter(100));
		TokenStream tokenstream = m.analyzer.tokenStream("", new StringReader(content));
		try {
			String res = highlighter.getBestFragment(tokenstream, content);
			System.out.println(res);
		} catch (InvalidTokenOffsetsException e) {
			e.printStackTrace();
		}
	}
}
