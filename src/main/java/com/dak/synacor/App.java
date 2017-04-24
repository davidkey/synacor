package com.dak.synacor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class App {

	private static final int WATCHDOG_MAX_CYCLES = 100_000;

	public App() {
		// TODO Auto-generated constructor stub
	}

	/*private static final int[] memory = new int[32767];
	private static int[] registers = new int[8];
	private static final Stack stack = new Stack();*/

	private static final Set<Integer> OP_CODES_ENCOUNTERED = new HashSet<>();
	
	private static final String[] descriptions = {
			"halt",
			"set",
			"push",
			"pop",
			"eq",
			"gt", 
			"jmp",
			"jt",
			"jf",
			"add",
			"mult",
			"mod",
			"and",
			"or",
			"not",
			"rmem",
			"wmem",
			"call",
			"ret",
			"out",
			"in",
			"noop"
	};

	private static final int[] numArguments = {
			0,
			2,
			1,
			1,
			3,
			3,
			1,
			2,
			2,
			3,
			3,
			3,
			3,
			3,
			2,
			2,
			2,
			1,
			0,
			1,
			1,
			0
	};

	private static int cyclesCounter = 0;

	public static void main(String[] args) {
		System.out.println("Start");
		final long startTime = System.currentTimeMillis();

		try{
			new App().run();
		} catch (Exception e){
			e.printStackTrace();
		}

		System.out.println("End - cycles: " + cyclesCounter + " - time elapsed (ms): " + (System.currentTimeMillis() - startTime));
	}

	public void run() throws Exception{
		final List<Integer> program = reverseEndianess(getProgram());

		Arrays.stream(decompile(program).split(System.lineSeparator()))
		.limit(500)
		.forEach(System.out::println);
		 
		//System.out.println(decompile(program));

		int index = 0;
		do{
			index = process(program, index);
		} while(index >= 0 && cyclesCounter < WATCHDOG_MAX_CYCLES); // just making sure we're not stuck somewhere...

		if(cyclesCounter >= WATCHDOG_MAX_CYCLES-1){
			System.err.println("\nprogram halted by watchdog!");

			Thread.sleep(100);
		}

		System.err.println("Op codes encountered: " + OP_CODES_ENCOUNTERED);
	}

	private String decompile(final List<Integer> program){
		final StringBuilder results = new StringBuilder();

		for(int i = 0; i < program.size(); i++){
			results.append(String.format("%05d", i)).append(": ");

			int opCode = program.get(i);

			if(isOpCode(opCode)){
				results.append(descriptions[opCode]);

				if(opCode == 19){ // printing chars
					results.append(" '");
					char toOutput = (char)(int) program.get(i + 1);

					if(toOutput != '\n'){
						results.append(toOutput).append("' (").append((int) program.get(i + 1)).append(")");
					} else {
						results.append("\\n").append("' (").append((int) program.get(i + 1)).append(")");
					}
				} else {

					for(int j = 0; j < numArguments[opCode]; j++){
						final int arg = program.get(i + j + 1);
						results.append(" ");//.append(program.get(i + j + 1));
						
						if(isRegisterReference(arg)){
							results.append("@");
						}
						
						results.append(arg);
					}
				}

				i += numArguments[opCode];
			} else {
				results.append("(not op) ").append(opCode);
			}


			results.append(System.lineSeparator());
		}


		return results.toString();
	}

	private int process(final List<Integer> program, final int startingIndex){
		final int op = program.get(startingIndex);

		if(isOpCode(op)){
			OP_CODES_ENCOUNTERED.add(op);
			
			if(op != 19){
				System.out.println("\n[current instruction pointer: " + startingIndex + "] -- " + descriptions[op] + " -- " + VirtualMachine.getAllRegisters() + "\n");
			}
		}

		cyclesCounter++;
		
		switch(op){
		case 0: { // halt: 0
			return -1;
		}
		case 1: { // set: 1 a b
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 2: { // push: 2 a
			VirtualMachine.pushStack(getValue(program.get(startingIndex + 1)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 3: {// pop: 3 a
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(VirtualMachine.popStack()));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 4: {// eq: 4 a b c [set <a> to 1 if <b> is equal to <c>; set it to 0 otherwise]
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) == getValue(program.get(startingIndex + 3)) ? 1 : 0);
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 5: {// gt: 5 a b c [set <a> to 1 if <b> is greater than <c>; set it to 0 otherwise]
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) > getValue(program.get(startingIndex + 3)) ? 1 : 0); 		
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 6: { // jmp: 6 a [jump to <a>]
			return process(program, getValue(program.get(startingIndex + 1)));
		}
		case 7: { // jt: 7 a b	[if <a> is nonzero, jump to <b>]			
			if(getValue(program.get(startingIndex + 1)) != 0){
				return process(program, getValue(program.get(startingIndex + 2)));
			} else {
				return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
			}
		}
		case 8: { // jf: 8 a b [if <a> is zero, jump to <b>]
			if(getValue(program.get(startingIndex + 1)) == 0){
				return process(program, getValue(program.get(startingIndex + 2)));
			}

			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 9: { // add: 9 a b c [assign into <a> the sum of <b> and <c> (modulo 32768)]
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) + getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 10: { // mult: 10 a b c
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) * getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 11: { // mod: 11 a b c
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) % getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 12: { // and: 12 a b c
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) & getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 13: { // or: 13 a b c [stores into <a> the bitwise or of <b> and <c>]
			VirtualMachine.setRegister(program.get(startingIndex + 1), getValue(program.get(startingIndex + 2)) | getValue(program.get(startingIndex + 3)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 14: { // not: 14 a b
			VirtualMachine.setRegister(program.get(startingIndex + 1), ~getValue(program.get(startingIndex + 2)));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 15: { // rmem: 15 a b [read memory at address <b> and write it to <a>]			
			System.out.println("rmem @ address " + getValue(program.get(startingIndex + 2)) + " = " + program.get(getValue(program.get(startingIndex + 2))));
			VirtualMachine.setRegister(program.get(startingIndex + 1), program.get(getValue(program.get(startingIndex + 2))));
			
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 16: { // wmem: 16 a b [read memory at address <b> and write it to <a>]
			
			System.out.println("wmem to " + getValue(program.get(startingIndex + 1))  + " from address " + getValue(program.get(startingIndex + 2)) + " = " + program.get(getValue(program.get(startingIndex + 2))));
			System.out.println("value before: " + program.get(getValue(program.get(startingIndex + 1))));
			
			program.set(getValue(program.get(startingIndex + 1)), program.get(getValue(program.get(startingIndex + 2))));
			
			System.out.println("value after: " + program.get(getValue(program.get(startingIndex + 1))));

			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 17: { // call: 17 a [write the address of the next instruction to the stack and jump to <a>]
			final int nextInstructionAddress = getIndexOfNextOpCode(program, startingIndex + 2);
			VirtualMachine.pushStack(nextInstructionAddress);
			return process(program, getValue(program.get(startingIndex + 1)));
		}
		case 18: { // ret: 18 [remove the top element from the stack and jump to it; empty stack = halt]
			if(VirtualMachine.isStackEmpty()){
				return -1;
			}
			final int topElement = getValue(VirtualMachine.popStack());
			return process(program, topElement);
		}
		case 19: { // out: 19 a [write the character represented by ascii code <a> to the terminal]
			System.out.print(Character.toString((char)(int) getValue(program.get(startingIndex+1))));
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		case 20: { // in: 20 a
			throw new RuntimeException("not yet implemented");
		}
		case 21: { // noop: 21
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		default: { // invalid? just skip...
			if(isOpCode(program.get(startingIndex))){
				System.out.println("cycle: " + cyclesCounter + " / idx: " + startingIndex + " encounted unknown opcode: " + program.get(startingIndex) + " - here's the next few bits: " + program.subList(startingIndex, startingIndex+4));
			}
			return getIndexOfNextOpCode(program, startingIndex + numArguments[op] + 1);
		}
		}

		//return getIndexOfNextOpCode(program, startingIndex + 1); 
	}

	private int getValue(int x){
		if(isRegisterReference(x)){
			return VirtualMachine.getRegister(x);
		}

		return x;
	}

	private boolean isRegisterReference(final int candidate){
		return candidate >= 32768 && candidate <= 32775;
	}

	private boolean isOpCode(final int candidate){
		return candidate >= 0 && candidate <= 21;
	}

	private int getIndexOfNextOpCode(final List<Integer> program, int startingIndex){
		for(int i = startingIndex; i < program.size(); i++){
			int opCode = program.get(i);

			if(isOpCode(opCode)){
				return i;
			}
		}

		return -1; // end??
	}

	private byte[] getProgram() throws IOException, URISyntaxException{
		return Files.readAllBytes(Paths.get(this.getClass().getResource("/challenge.bin").toURI()));
	}

	private List<Integer> reverseEndianess(final byte[] input){
		final List<Integer> output = new ArrayList<>(input.length/2);

		for(int i = 0; i+1 < input.length; i+=2){
			int lower = input[i] < 0 ? 256 + input[i] : input[i];
			int upper = input[i+1] < 0 ? 256 + input[i+1] : input[i+1];
			int val = (upper << 8) + lower;

			output.add(val);
		}

		return output;
	}
}
