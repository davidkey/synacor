package com.dak.synacor;

import java.util.LinkedList;

public class Stack{
	final LinkedList<Integer> elements;
	
	public Stack(){
		elements = new LinkedList<>();
	}
	
	public int pop(){
		return elements.pop();
	}
	
	public void push(int i){
		elements.push(i);
	}
	
	public boolean isEmpty(){
		return elements.isEmpty();
	}
	
	@Override
	public String toString(){
		return elements.toString();
	}
}