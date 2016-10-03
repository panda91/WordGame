package dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;

import model.Word;

public class WordDAO {
	
	private MongoCollection<Document> wordCollection;
	
	public WordDAO(){
		this.wordCollection = DBConnector.getDatabase().getCollection("words");
	}
	
	public List<Word> getAllWords(){
		List<Word> words = new ArrayList<>();
		
		this.wordCollection.find().forEach(new Block<Document>() {

			@SuppressWarnings("unchecked")
			@Override
			public void apply(Document doc) {
				// TODO Auto-generated method stub
				Word word = new Word();
				
				word.setWord(doc.getString("word"));
				
				if(doc.get("dependents") instanceof List<?>){
					List<String> dependents = (List<String>) doc.get("dependents");
					
					word.setDependents(dependents);
				}
				
				words.add(word);
			}
		});
		return words;
	}

}
