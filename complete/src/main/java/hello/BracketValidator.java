package hello;

import java.util.Stack;

public class BracketValidator {

	private Stack<Character> stack = new Stack<Character>();

	private boolean isOpeningBracket(final char bracket) {
		return "({[".indexOf(bracket) != -1;
	}

	private boolean isClosingBracket(final char bracket) {
		return ")}]".indexOf(bracket) != -1;
	}

	private boolean isPair(final char opening, final char closing) {
		return opening == '(' && closing == ')' || opening == '[' && closing == ']' || opening == '{' && closing == '}';
	}

	public boolean validate(final String input) {
		for (char c : input.toCharArray()) {
			if (isClosingBracket(c) && stack.isEmpty()) {
				return false;
			}
			if (isOpeningBracket(c)) {
				stack.push(c);
			}
			if (isClosingBracket(c)) {
				if (isPair(stack.peek(), c)) {
					stack.pop();
				} else {
					return false;
				}
			}
		}
		return stack.isEmpty();
	}
}
