package com.dak.synacor;

import java.util.Arrays;

public class VirtualMachine {
	private static final int[] memory = new int[32767];
	private static int[] registers = new int[8];
	private static final Stack stack = new Stack();
	
	
	private VirtualMachine() {
		// TODO Auto-generated constructor stub
	}

	public static void setMemory(final int index, final int val){
		memory[index] = val;
	}
	
	public static int getMemory(final int index){
		return memory[index];
	}
	
	public static void setRegister(final int index, final int val){
		if(val < 0){
			registers[index % 32768] = 32768 + val;
		} else {
			registers[index % 32768] = val == 0 ? 0 : val % 32768;
		}
	}
	
	public static int getRegister(final int index){
		return registers[index % 32768];
	}
	
	public static int popStack(){
		return stack.pop();
	}
	
	public static void pushStack(final int val){
		stack.push(val);
	}
	
	public static boolean isStackEmpty(){
		return stack.isEmpty();
	}
	
	public static String getAllRegisters(){
		return Arrays.toString(registers);
	}
	
	public static String getStackString(){
		return stack.toString();
	}
}
