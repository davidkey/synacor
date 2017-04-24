package com.dak.synacor;

import java.util.List;

public abstract class Operation {

	protected final OpCode opCode;
	
	public Operation(final OpCode opCode) {
		this.opCode = opCode;
	}
	
	public abstract int process(final List<Integer> program, final int processCounter);
}
