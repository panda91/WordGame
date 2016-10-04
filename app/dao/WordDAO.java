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
	
	public Word findWord(String name){
		Word word = new Word();
		
		this.wordCollection.find(new Document("word", name)).forEach(new Block<Document>() {

			@SuppressWarnings("unchecked")
			@Override
			public void apply(Document doc) {
				// TODO Auto-generated method stub
				word.setWord(doc.getString("word"));
				if(doc.get("dependents") instanceof List<?>){
					List<String> dependents = (List<String>) doc.get("dependents");
					
					word.setDependents(dependents);
				}
			}
		});
		
		return word;
	}
	
	public boolean updateWord(Word word){
		if(word != null){
			Document conditions = new Document("word", word.getWord());
			Document fields = new Document();
			
			if(word.getDependents() != null && !word.getDependents().isEmpty()){
				fields.append("$push", new Document("dependents", word.getDependents()));
				
				try {
					this.wordCollection.updateMany(conditions, fields);
					return true;
				} catch (Exception e) {
					// TODO: handle exception
					throw new RuntimeException("Error in updating: " + e.getMessage());
				}
			}
		}
		
		return false;
	}
	
	public boolean insertWord(Word word){
		if(word != null){
			Document fields = new Document("word", word.getWord());
			if(word.getDependents() != null && !word.getDependents().isEmpty()){
				fields.append("dependents", word.getDependents());
				
				try {
					this.wordCollection.insertOne(fields);
					return true;
				} catch (Exception e) {
					// TODO: handle exception
					throw new RuntimeException("Error in inserting: " + e.getMessage());
				}
			}
		}
		
		return false;
	}

}
