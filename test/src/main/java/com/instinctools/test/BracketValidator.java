package com.instinctools.test;

import java.util.List;
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

	public boolean validate(final List<Character> input) {
		for (char c : input) {
			if (isClosingBracket(c) && stack.isEmpty()) {
				// if closing bracket without opening
				return false;
			}
			if (isOpeningBracket(c)) {
				// add opening bracket to stack
				stack.push(c);
			}
			if (isClosingBracket(c)) {
				if (isPair(stack.peek(), c)) {
					// if closing bracket has pair, delete opening bracket from stack
					stack.pop();
				} else {
					return false;
				}
			}
		}
		return stack.isEmpty();
	}
}
