package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	private List<Word> wordList;
	private Word mainWord;
	private Word shuffleWord;
	private int point;

	public Result index() {
		point = 0;
		return ok(index.render());
	}
	
	public Result getWordList(){
		try {
			wordList = getAllWords();
			return ok(Json.toJson(wordList));
		} catch (Exception e) {
			// TODO: handle exception
			ObjectNode objNode = Json.newObject();
			objNode.put("message", e.getMessage());
			return internalServerError(objNode);
		}
	}

	public Result addWord() {
		JsonNode json = request().body().asJson();
		ObjectNode objNode = Json.newObject();

		if (json == null) {
			objNode.put("message", "Expecting json data");
			return badRequest(objNode);
		} else {
			String word = json.findPath("name").textValue();
			String relatives = json.findPath("relatives").textValue();

			if (word == null || relatives == null) {
				objNode.put("message", "Data is missing!");
				return badRequest(objNode);
			} else if (!word.equals("") && !word.equals(" ")) {
				word = word.trim().toLowerCase();
				String[] dependents = relatives.split(",");
				WordDAO wordDAO = new WordDAO();

				Word oldWord = wordDAO.findWord(word);
				Word newWord = new Word();
				newWord.setWord(word);

				Set<String> setDependents = new HashSet<>();
				for (String item : dependents) {
					if (!item.equals("") && !item.equalsIgnoreCase(" ")) {
						setDependents.add(item.trim().toLowerCase());
					} else {
						objNode.put("message", "Your relative words is invalid!");
						return badRequest(objNode);
					}
				}

				try {
					if (oldWord != null) {
						setDependents.addAll(oldWord.getDependents());
						oldWord.setDependents(new ArrayList<>(setDependents));

						wordDAO.updateWord(oldWord);
					} else {
						newWord.setDependents(new ArrayList<>(setDependents));

						wordDAO.insertWord(newWord);
					}
					wordList = getAllWords();

					objNode.put("status", "OK");
					objNode.putPOJO("words", Json.toJson(wordList));
					return ok(objNode);
				} catch (Exception e) {
					// TODO: handle exception
					objNode.put("message", e.getMessage());
					return internalServerError(objNode);
				}
			} else {
				objNode.put("message", "Word is missing!");
				return badRequest(objNode);
			}
		}
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

	public Result withdrawn() {
		List<String> dependents = shuffleWord.getDependents();
		List<String> answers = new ArrayList<>();

		for (String item : dependents) {
			if (!item.contains("*")) {
				answers.add(item);
			}
		}

		ObjectNode objNode = Json.newObject();
		objNode.put("point", point);
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
					List<String> dependents = mainWord.getDependents();
					int length = dependents.size();

					for (int i = 0; i < length; i++) {
						if (answerWord.equals(dependents.get(i))) {
							List<String> maskList = shuffleWord.getDependents();
							int maskLength = maskList.size();
							for (int j = 0; j < maskLength; j++) {
								if (maskList.get(j).contains("*") && maskList.get(j).length() == answerWord.length()) {
									maskList.set(j, answerWord);
									break;
								}
							}
							dependents.remove(i);

							if (dependents.isEmpty()) {
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

							objNode.put("point", ++point);

							return ok(objNode).as("application/json;charset=UTF-8");
						} else if ((i + 1) == length) {
							List<String> maskList = shuffleWord.getDependents();
							for (String item : maskList) {
								if (item.equals(answerWord)) {
									objNode.put("message", "You've already answered this word!");
									return notFound(objNode);
								}
							}
							objNode.put("point", --point);
							objNode.put("message", "Your answer is wrong!");
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
			wordList = getAllWords();
		}
		if (wordList != null && !wordList.isEmpty()) {
			int index = ThreadLocalRandom.current().nextInt(0, wordList.size());

			mainWord = wordList.get(index);

			wordList.remove(index);

		}
	}

	private List<Word> getAllWords() {
		try {
			WordDAO wordDao = new WordDAO();

			List<Word> wordList = wordDao.getAllWords();

			return wordList;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException("Unexpected error in getting data: " + e.getMessage());
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
