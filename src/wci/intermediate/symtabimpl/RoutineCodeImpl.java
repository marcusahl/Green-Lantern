package wci.intermediate.symtabimpl;

import wci.intermediate.RoutineCode;

public enum RoutineCodeImpl implements RoutineCode {
	// For forwarded statements
	FORWARD, DECLARED,
	
	READ, READLN, WRITE, WRITELN, ABS, ARCTAN, CHR, COS, EOF, EOLN, 
	EXP, LN, ODD, ORD, PRED, ROUND, SIN, SQR, SQRT, SUCC, TRUNC;

	public String toString() {
		return super.toString().toLowerCase();
	}
	
}
