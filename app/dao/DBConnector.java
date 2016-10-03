package dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class DBConnector {
	
	private static class DBHelper{
		private static final MongoClient CLIENT = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		public static final MongoDatabase DATABASE = CLIENT.getDatabase("word_scramble");
	}
	
	public static MongoDatabase getDatabase(){
		try {
			return DBHelper.DATABASE;
		} catch (Exception e) {
			throw new RuntimeException("Error connecting to database: " + e.getMessage());
		}
	}
}
