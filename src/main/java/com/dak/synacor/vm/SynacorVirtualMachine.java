package com.dak.synacor.vm;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class SynacorVirtualMachine {
	private final int[] registers;
	private final Deque<Integer> stack;
	private final List<Integer> program;
	private final Deque<Character> charsDeque;
	private final Scanner sc;
	private final Map<OpCode, Function<Integer, Integer>> operations;


	public SynacorVirtualMachine(final List<Integer> program) {
		this.program = program;
		this.stack = new ArrayDeque<>();
		this.registers = new int[8];
		this.charsDeque = new ArrayDeque<>();
		this.sc = new Scanner(System.in);
		this.operations = Collections.unmodifiableMap(getOperations());

		// fill with noops until our address space is 2^15 (32768)
		if(program.size() < 32768) {
			program.addAll(Collections.nCopies(32768 - program.size(), OpCode.NOOP.getOpCodeInt()));
		}

		if(program.size() != 32768) {
			throw new RuntimeException("program invalid size! should be 32768 but is " + program.size());
		}
	}

	public void execute() {
		int index = 0;
		while((index = process(program, index)) >= 0);
	}
	
	private Map<OpCode, Function<Integer, Integer>> getOperations() {
		final Map<OpCode, Function<Integer, Integer>> operations = new HashMap<>(OpCode.values().length);
		
		operations.put(OpCode.HALT, i -> -1);
		
		operations.put(OpCode.SET, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.SET.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.PUSH, startingIndex -> {
			this.pushStack(getValue(program.get(startingIndex + 1)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.PUSH.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.POP, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(this.popStack()));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.POP.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.EQ, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) == getValue(program.get(startingIndex + 3)) ? 1 : 0);
			return getIndexOfNextOpCode(program, startingIndex + OpCode.EQ.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.GT, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) > getValue(program.get(startingIndex + 3)) ? 1 : 0); 		
			return getIndexOfNextOpCode(program, startingIndex + OpCode.GT.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.JMP, startingIndex -> process(program, getValue(program.get(startingIndex + 1))));
		
		operations.put(OpCode.JT, startingIndex -> getValue(program.get(startingIndex + 1)) != 0 
			? process(program, getValue(program.get(startingIndex + 2))) : getIndexOfNextOpCode(program, startingIndex + OpCode.JT.getNumOfArguments() + 1));
		
		operations.put(OpCode.JF, startingIndex -> getValue(program.get(startingIndex + 1)) == 0 
			? process(program, getValue(program.get(startingIndex + 2))) : getIndexOfNextOpCode(program, startingIndex + OpCode.JF.getNumOfArguments() + 1));
		
		operations.put(OpCode.ADD, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) + getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.ADD.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.MULT, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) * getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.MULT.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.MOD, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) % getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.MOD.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.AND, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) & getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.AND.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.OR, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) | getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.OR.getNumOfArguments() + 1);
		});
	
		operations.put(OpCode.NOT, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), ~getValue(program.get(startingIndex + 2)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.NOT.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.RMEM, startingIndex -> {
			this.setRegister(program.get(startingIndex + 1), program.get(getValue(program.get(startingIndex + 2))));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.RMEM.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.WMEM, startingIndex -> {
			program.set(getValue(program.get(startingIndex + 1)), getValue(program.get(startingIndex + 2)));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.WMEM.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.CALL, startingIndex -> {
			this.pushStack(getIndexOfNextOpCode(program, startingIndex + 2));
			return process(program, getValue(program.get(startingIndex + 1)));
		});
		
		operations.put(OpCode.RET, startingIndex -> process(program, getValue(this.popStack())));
		
		operations.put(OpCode.OUT, startingIndex -> {
			System.out.print(Character.toString((char)(int) getValue(program.get(startingIndex+1))));
			return getIndexOfNextOpCode(program, startingIndex + OpCode.OUT.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.IN, startingIndex -> {
			final int result = (int)getNextChar();
			final int target = program.get(startingIndex + 1);

			if(isRegisterReference(target)) {
				this.setRegister(target, result);
			} else {
				program.set(getValue(target), result);
			}

			return getIndexOfNextOpCode(program, startingIndex + OpCode.IN.getNumOfArguments() + 1);
		});
		
		operations.put(OpCode.NOOP, startingIndex -> getIndexOfNextOpCode(program, startingIndex + OpCode.NOOP.getNumOfArguments() + 1));
		
		return operations;
	}

	private int process(final List<Integer> program, final int startingIndex){
		return operations.get(OpCode.fromInt(program.get(startingIndex))).apply(startingIndex);
	}

	private char getNextChar() {
		if(charsDeque.isEmpty()) {
			final char[] charArray = sc.nextLine().toCharArray();

			for(int i = 0; i < charArray.length; i++) {
				charsDeque.push(charArray[i]);
			}

			charsDeque.push('\n');
		}

		return charsDeque.removeLast();
	}

	private int getValue(int x){
		if(isRegisterReference(x)){
			return this.getRegister(x);
		}

		return x;
	}

	private boolean isRegisterReference(final int candidate){
		return candidate >= 32768 && candidate <= 32775;
	}

	private int getIndexOfNextOpCode(final List<Integer> program, int startingIndex){
		for(int i = startingIndex; i < program.size(); i++){
			int opCode = program.get(i);

			if(OpCode.isOpCode(opCode)){
				return i;
			}
		}

		return -1;
	}

	private void setRegister(final int index, final int val){
		if(val < 0){
			registers[index % 32768] = 32768 + val;
		} else {
			registers[index % 32768] = val == 0 ? 0 : val % 32768;
		}
	}

	private int getRegister(final int index){
		return registers[index % 32768];
	}

	private int popStack(){
		return stack.pop();
	}

	private void pushStack(final int val){
		stack.push(val);
	}

	public String decompile(){
		final StringBuilder results = new StringBuilder();

		for(int i = 0; i < program.size(); i++){
			results.append(String.format("%05d", i)).append(": ");

			final int memoryBlock = program.get(i);

			if(OpCode.isOpCode(memoryBlock)){
				final OpCode op = OpCode.fromInt(memoryBlock);
				results.append(op.toString());

				if(OpCode.OUT.equals(op)){ // printing chars
					results.append(" '");
					char toOutput = (char)(int) program.get(i + 1);

					if(toOutput != '\n'){
						results.append(toOutput).append("' (").append((int) program.get(i + 1)).append(")");
					} else {
						results.append("\\n").append("' (").append((int) program.get(i + 1)).append(")");
					}
				} else {
					for(int j = 0; j < op.getNumOfArguments(); j++){
						final int arg = program.get(i + j + 1);
						results.append(" ");

						if(isRegisterReference(arg)){
							results.append("@");
						}

						results.append(arg);
					}
				}
				i += op.getNumOfArguments();
			} else {
				results.append("(not op) ").append(memoryBlock);
			}

			results.append(System.lineSeparator());
		}

		return results.toString();
	}
}
