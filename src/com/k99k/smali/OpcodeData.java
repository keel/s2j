package com.k99k.smali;

import java.util.HashMap;
import java.util.Map;

public class OpcodeData
{
	public static final byte TYPE_UNUSED    = -1;
	public static final byte TYPE_OTHER     = 0;
	public static final byte TYPE_MOVE      = 1;
	public static final byte TYPE_RETURN    = 2;
	public static final byte TYPE_CONST     = 3;
	public static final byte TYPE_NEW       = 4;
	public static final byte TYPE_GOTO      = 5;
	public static final byte TYPE_CMP       = 6;
	public static final byte TYPE_CONDITION = 7;
	public static final byte TYPE_GET       = 8;
	public static final byte TYPE_PUT       = 9;
	public static final byte TYPE_INVOKE    = 10;
	public static final byte TYPE_MATH      = 11;
	
	public static final OpcodeData UNDEFINED_OPCODE_DATA = new OpcodeData("", 0, TYPE_UNUSED); 
	
	private static Map<String, OpcodeData> data = new HashMap<String, OpcodeData>();
	private String name = "";
	private int opcode = 0;
	private byte type = TYPE_UNUSED;
	
	static
	{
		putOpcodeData(new OpcodeData("nop", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("move", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move/from16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move/16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-wide", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-wide/from16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-wide/16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-object", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-object/from16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-object/16", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-result", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-result-wide", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-result-object", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("move-exception", 0, TYPE_MOVE));
		putOpcodeData(new OpcodeData("return-void", 0, TYPE_RETURN));
		putOpcodeData(new OpcodeData("return", 0, TYPE_RETURN));
		putOpcodeData(new OpcodeData("return-wide", 0, TYPE_RETURN));
		putOpcodeData(new OpcodeData("return-object", 0, TYPE_RETURN));
		putOpcodeData(new OpcodeData("const/4", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const/16", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const/high16", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-wide/16", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-wide/32", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-wide", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-wide/high16", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-string", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-string-jumbo", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("const-class", 0, TYPE_CONST));
		putOpcodeData(new OpcodeData("monitor-enter", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("monitor-exit", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("check-cast", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("instance-of", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("array-length", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("new-instance", 0, TYPE_NEW));
		putOpcodeData(new OpcodeData("new-array", 0, TYPE_NEW));
		putOpcodeData(new OpcodeData("filled-new-array", 0, TYPE_NEW));
		putOpcodeData(new OpcodeData("filled-new-array-range", 0, TYPE_NEW));
		putOpcodeData(new OpcodeData("fill-array-data", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("throw", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("goto", 0, TYPE_GOTO));
		putOpcodeData(new OpcodeData("goto/16", 0, TYPE_GOTO));
		putOpcodeData(new OpcodeData("goto/32", 0, TYPE_GOTO));
		putOpcodeData(new OpcodeData("packed-switch", 0, TYPE_UNUSED));
		putOpcodeData(new OpcodeData("sparse-switch", 0, TYPE_UNUSED));
		putOpcodeData(new OpcodeData("cmpl-float", 0, TYPE_CMP));
		putOpcodeData(new OpcodeData("cmpg-float", 0, TYPE_CMP));
		putOpcodeData(new OpcodeData("cmpl-double", 0, TYPE_CMP));
		putOpcodeData(new OpcodeData("cmpg-double", 0, TYPE_CMP));
		putOpcodeData(new OpcodeData("cmp-long", 0, TYPE_CMP));
		putOpcodeData(new OpcodeData("if-eq", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-ne", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-lt", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-ge", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-gt", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-le", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-eqz", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-nez", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-ltz", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-gez", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-gtz", 0, TYPE_CONDITION));
		putOpcodeData(new OpcodeData("if-lez", 0, TYPE_CONDITION));
	    /* 3e-43 unused */
		putOpcodeData(new OpcodeData("aget", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-wide", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-object", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-boolean", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-byte", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-char", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aget-short", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("aput", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-wide", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-object", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-boolean", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-byte", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-char", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("aput-short", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iget", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-wide", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-object", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-boolean", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-byte", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-char", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-short", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iput", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-wide", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-object", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-boolean", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-byte", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-char", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-short", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sget", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-wide", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-object", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-boolean", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-byte", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-char", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sget-short", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sput", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-wide", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-object", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-boolean", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-byte", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-char", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sput-short", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("invoke-virtual", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-super", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-direct", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-static", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-interface", 0, TYPE_INVOKE));
	    /* 73 unused */
		putOpcodeData(new OpcodeData("invoke-virtual/range", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-super/range", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-direct/range", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-static/range", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-interface/range", 0, TYPE_INVOKE));
	    /* 79-7a unused */
		putOpcodeData(new OpcodeData("neg-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("not-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("neg-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("not-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("neg-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("neg-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("long-to-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("long-to-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("long-to-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("float-to-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("float-to-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("float-to-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("double-to-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("double-to-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("double-to-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-byte", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-char", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("int-to-short", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shl-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shr-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("ushr-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shl-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shr-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("ushr-long", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-float", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-double", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shl-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shr-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("ushr-int/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shl-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shr-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("ushr-long/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-float/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-float/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-float/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-float/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-float/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-double/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("sub-double/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-double/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-double/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-double/2addr", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rsub-int", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-int/lit16", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("add-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rsub-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("mul-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("div-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("rem-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("and-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("or-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("xor-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shl-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("shr-int/lit8", 0, TYPE_MATH));
		putOpcodeData(new OpcodeData("ushr-int/lit8", 0, TYPE_MATH));
	    /* e3-e7 unused */
	    /*
	     * The rest of these are either generated by dexopt for optimized
	     * code, or inserted by the VM at runtime.  They are never generated
	     * by "dx".
	     */
		putOpcodeData(new OpcodeData("iget-wide-volatile", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iput-wide-volatile", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("sget-wide-volatile", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("sput-wide-volatile", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("breakpoint", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("throw-verification-error", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("execute-inline", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("execute-inline-range", 0, TYPE_OTHER));
		putOpcodeData(new OpcodeData("invoke-direct-empty", 0, TYPE_INVOKE));
	    /* f1 unused (OP_INVOKE_DIRECT_EMPTY_RANGE?) */
		putOpcodeData(new OpcodeData("iget-quick", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-wide-quick", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iget-object-quick", 0, TYPE_GET));
		putOpcodeData(new OpcodeData("iput-quick", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-wide-quick", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("iput-object-quick", 0, TYPE_PUT));
		putOpcodeData(new OpcodeData("invoke-virtual-quick", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-virtual-quick-range", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-super-quick", 0, TYPE_INVOKE));
		putOpcodeData(new OpcodeData("invoke-super-quick-range", 0, TYPE_INVOKE));
	    /* fc unused (OP_INVOKE_DIRECT_QUICK?) */
	    /* fd unused (OP_INVOKE_DIRECT_QUICK_RANGE?) */
	    /* fe unused (OP_INVOKE_INTERFACE_QUICK?) */
	    /* ff unused (OP_INVOKE_INTERFACE_QUICK_RANGE?) */
	}
	
	private OpcodeData(String name, int opcode, byte type)
	{
		this.name = name;
		this.opcode = opcode;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public int getOpcode()
	{
		return opcode;
	}
	
	public int getType()
	{
		return type;
	}

	public static OpcodeData getOpcodeData(String name)
	{
		if(name == null) return null;
		OpcodeData retVal = data.get(name.toLowerCase());
		if(retVal == null) retVal = UNDEFINED_OPCODE_DATA;
		return retVal;
	}
	
	private static void putOpcodeData(OpcodeData opcodeData)
	{
		data.put(opcodeData.getName(), opcodeData);
	}
}
