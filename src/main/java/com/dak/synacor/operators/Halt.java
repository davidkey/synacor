package com.dak.synacor.operators;

import java.util.List;

import com.dak.synacor.OpCode;
import com.dak.synacor.Operation;

public class Halt extends Operation {

	public Halt(OpCode opCode) {
		super(opCode);
	}

	@Override
	public int process(List<Integer> program, int processCounter) {
		return -1;
	}

}
