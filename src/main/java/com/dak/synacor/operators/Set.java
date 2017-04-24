package com.dak.synacor.operators;

import java.util.List;

import com.dak.synacor.OpCode;
import com.dak.synacor.Operation;

public class Set extends Operation {

	public Set(OpCode opCode) {
		super(opCode);
	}

	@Override
	public int process(List<Integer> program, int processCounter) {
		//int[] instructions = new int[this.opCode.getNumOfArguments()];
		
		final int startArgument = processCounter + 1; // off by one??
		
		program.subList(startArgument, startArgument + opCode.getNumOfArguments());
		
		return -1;
	}

}
