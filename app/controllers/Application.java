package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dao.WordDAO;
import model.Word;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

	private static List<Word> wordList;
	private static Word mainWord;
	private static Word shuffleWord;

	public Result index() {
		return ok(index.render());
	}
	
	public Result addWord(){
		JsonNode json = request().body().asJson();
		ObjectNode objNode = Json.newObject();
		
		if (json == null) {
			objNode.put("message", "Expecting json data");
			return badRequest(objNode);
		}else{
			String word = json.findPath("word").textValue();
			String relatives = json.findPath("relatives").textValue();
			
			if(word == null || relatives == null){
				objNode.put("message", "Data is missing!");
				return badRequest(objNode);
			}
		}
		
		objNode.put("message", "Adding word is failed!");
		return badRequest(objNode);
	}

	public Result getWord() {

		try {
			this.getData();
		} catch (Exception e) {
			// TODO: handle exception
			return internalServerError(e.getMessage());
		}
		shuffleWord = this.getShuffleWord(mainWord);

		return ok(Json.toJson(shuffleWord)).as("application/json;charset=UTF-8");
	}
	
	public Result withdrawn(){
		List<String> dependents = shuffleWord.getDependents();
		List<String> answers = new ArrayList<>();
		
		for (String item : dependents) {
			if (!item.contains("*")) {
				answers.add(item);
			}
		}
		
		ObjectNode objNode = Json.newObject();
		objNode.putPOJO("missedWord", Json.toJson(mainWord));
		objNode.putPOJO("answers", Json.toJson(answers));
		
		return ok(objNode).as("application/json;charset=UTF-8");
	}

	public Result checkWord() {
		JsonNode json = request().body().asJson();
		ObjectNode objNode = Json.newObject();

		if (json == null) {
			objNode.put("message", "Expecting json data");
			return badRequest(objNode);
		} else {
			String answerWord = json.findPath("answerWord").textValue();

			if (answerWord == null) {
				objNode.put("message", "Missing properties");
				return badRequest("Missing properties");
			} else {
				if (mainWord != null) {
					int length = mainWord.getDependents().size();

					for (int i = 0; i < length; i++) {
						if (answerWord.equals(mainWord.getDependents().get(i))) {
							List<String> maskList = shuffleWord.getDependents();
							int maskLength = maskList.size();
							for (int j = 0; j < maskLength; j++) {
								if (maskList.get(j).contains("*") && maskList.get(j).length() == answerWord.length()) {
									maskList.set(j, answerWord);
									break;
								}
							}
							mainWord.getDependents().remove(i);

							if (mainWord.getDependents().isEmpty()) {
								try {
									this.getData();
									shuffleWord = this.getShuffleWord(mainWord);
									objNode.put("status", "reload");
									objNode.putPOJO("items", Json.toJson(shuffleWord));
								} catch (Exception e) {
									// TODO: handle exception
									objNode.put("message", e.getMessage());
									return internalServerError(objNode);
								}
							} else {
								objNode.put("status", "passed");
								objNode.putPOJO("items", Json.toJson(maskList));
							}

							return ok(objNode).as("application/json;charset=UTF-8");
						} else if ((i + 1) == length) {
							objNode.put("message", "Word doesn't exist");
							return notFound(objNode);
						}
					}
				} else {
					objNode.put("message", "Word is not initialized!");
					return badRequest(objNode);
				}
			}
		}
		objNode.put("message", "Something's wrong!");
		return badRequest(objNode);
	}

	private void getData() {
		if (wordList == null || wordList.isEmpty()) {
			try {
				WordDAO wordDao = new WordDAO();
				wordList = wordDao.getAllWords();
			} catch (Exception e) {
				// TODO: handle exception
				throw new RuntimeException("Unexpected error in getting data: " + e.getMessage());
			}
		}
		if(wordList != null && !wordList.isEmpty()){
			int index = ThreadLocalRandom.current().nextInt(0, wordList.size());

			mainWord = wordList.get(index);

			wordList.remove(index);

		}
	}

	private Word getShuffleWord(Word word) {
		Word result = new Word();
		if (word != null) {
			char[] items = word.getWord().toCharArray();
			int length = items.length;

			for (int i = length; i > 0; i--) {
				int rand = ThreadLocalRandom.current().nextInt(0, length);
				char temp = items[i - 1];
				items[i - 1] = items[rand];
				items[rand] = temp;
			}

			List<String> maskList = getMaskList(word.getDependents());

			result.setWord(new String(items));
			result.setDependents(maskList);
		}

		return result;
	}

	private List<String> getMaskList(List<String> arrays) {
		List<String> maskList = new ArrayList<>();

		if (arrays != null) {
			for (String s : arrays) {
				maskList.add(s.replaceAll(".", "*"));
			}
		}

		return maskList;
	}

}
