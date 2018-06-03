package com.dak.synacor.vm;

public enum OpCode {
	HALT(0, 0),
	SET(1, 2),
	PUSH(2, 1),
	POP(3, 1),
	EQ(4, 3),
	GT(5, 3),
	JMP(6, 1),
	JT(7, 2),
	JF(8, 2),
	ADD(9, 3),
	MULT(10, 3),
	MOD(11, 3),
	AND(12, 3),
	OR(13, 3),
	NOT(14, 2),
	RMEM(15, 2),
	WMEM(16, 2),
	CALL(17, 1),
	RET(18, 0),
	OUT(19, 1),
	IN(20, 1),
	NOOP(21, 0);
	
	private final int opCode;
	private final int numOfArguments;
	
	private OpCode(final int opCode, final int numOfArguments){
		this.opCode = opCode;
		this.numOfArguments = numOfArguments;
	}
	
	public int getOpCodeInt(){
		return this.opCode;
	}
	
	public int getNumOfArguments(){
		return this.numOfArguments;
	}
	
	public static Boolean isOpCode(final Integer opCodeInt) {
		return (opCodeInt > 0 && opCodeInt < OpCode.values().length);
	}
	
	public static OpCode fromInt(final Integer opCodeInt) {
		if(!isOpCode(opCodeInt)) {
			throw new RuntimeException("invalid op code " + opCodeInt);
		}
		
		return OpCode.values()[opCodeInt];
	}
}
