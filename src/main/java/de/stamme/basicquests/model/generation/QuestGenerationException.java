package de.stamme.basicquests.model.generation;

public class QuestGenerationException extends Exception {

	private static final long serialVersionUID = -2941555618699589814L;

	public String message;
	
	public QuestGenerationException(String message) {
		this.message = message;
	}
}
