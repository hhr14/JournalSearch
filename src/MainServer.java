import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.QueryParser;
public class MainServer extends HttpServlet{
	String path = "JournalSearch/";
	public static final int PAGE_RESULT=10;
	public MainServer(){
		super();
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		this.doGet(request, response);
	}
}
