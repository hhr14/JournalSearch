import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

public class MainIndexer {
	public static void main(String args[])
	throws Exception{
		MainIndexer mainindexer = new MainIndexer();
		final Path path = Paths.get("index");
		Directory directory = FSDirectory.open(path);
		Analyzer analyzer = mainindexer.getAnalyzer("unigram");
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
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
			
		}
		else{
			
		}
		return analyzer;
	}
}
