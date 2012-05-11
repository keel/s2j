/**
 * 
 */
package com.k99k.smali;

import java.util.HashMap;

/**
 * 静态辅助类
 * @author keel
 *
 */
public class StaticUtil {

	
	public static final String NEWLINE = "\r\n";
	public static final String COMM = "#";
	public static final String TAB = "	";
	
	public static final HashMap<String,String> basicType = new HashMap<String, String>();
	
	static{
		basicType.put("int", "Integer");
		basicType.put("boolean", "Boolean");
		basicType.put("long", "Long");
		basicType.put("byte", "Byte");
		basicType.put("float", "Float");
		basicType.put("double", "Double");
		basicType.put("void", "void");
		//basicType.put("java.lang.String", "java.lang.String");
	}
	
	/**
	 * 缩进,最大10个,如:TABS[2]表示2个缩进
	 */
	public static final String[] TABS = new String[]{
		"	",
		"		",
		"			",
		"				",
		"					",
		"						",
		"							",
		"								",
		"									",
		"										"
	};
	
	public static final String SCOPE_PUBLIC = "public";
	public static final String SCOPE_PRIVATE = "private";
	public static final String SCOPE_PROTECTED = "protected";
	public static final String SCOPE_STATIC = "static";
	public static final String SCOPE_FINAL = "final";
	public static final String SCOPE_CONSTRUCTOR = "constructor";
	public static final String SCOPE_ABSTRACT = "abstract";
	public static final String SCOPE_ANNOTATION = "annotation";
	public static final String SCOPE_BRIDGE = "bridge";
	public static final String SCOPE_DECLARED_SYNCHRONIZED = "declared-synchronized";
	public static final String SCOPE_NATIVE = "native";
	public static final String SCOPE_STRICTFP = "strictfp";
	public static final String SCOPE_SYNCHRONIZED = "synchronized";
	public static final String SCOPE_SYNTHETIC = "synthetic";
	public static final String SCOPE_SYSTEM = "system";
	public static final String SCOPE_TRANSIENT = "transient";
	public static final String SCOPE_VARARGS = "varargs";
	public static final String SCOPE_VOLATILE = "volatile";
	public static final String SCOPE_VOID = "void";
	
	public static final String TYPE_INTERFACE = "interface";
	public static final String TYPE_CLASS = ".class";
	public static final String TYPE_SUPER = ".super";
	public static final String TYPE_SOURCE = ".source";
	public static final String TYPE_FIELD = ".field";
	public static final String TYPE_METHOD = ".method";
	public static final String TYPE_END_METHOD = ".end method";
	public static final String TYPE_IMPLEMENTS = ".implements";
	
	public static final String TYPE_LOCALS = ".locals";
	public static final String TYPE_PROLOGUE = ".prologue";
	public static final String TYPE_PARAMETER = ".parameter";
	public static final String TYPE_LINE = ".line";
	public static final String TYPE_LOCAL = ".local";
	public static final String TYPE_END = ".end";
	public static final String TYPE_RESTART = ".restart";
	public static final String TYPE_ARRAY_DATA = ".array-data";
	public static final String TYPE_CATCH = ".catch";
	public static final String TYPE_CATCHALL = ".catchall";
	public static final String TYPE_ENUM = ".enum";
	public static final String TYPE_EPILOGUE = ".epilogue";
	public static final String TYPE_PACKED_SWITCH = ".packed-switch";
	public static final String TYPE_REGISTERS = ".registers";
	public static final String TYPE_SPARSE_SWITCH = ".sparse-switch";
	public static final String TYPE_ANNOTATION = ".annotation";
	
	/*
	 add-double
add-double/2addr
add-float
add-float/2addr
add-int
add-int/2addr
add-int/lit16
add-int/lit8
add-long
add-long/2addr
aget
aget-boolean
aget-byte
aget-char
aget-object
aget-short
aget-wide
and-int
and-int/2addr
and-int/lit16
and-int/lit8
and-long
and-long/2addr
aput
aput-boolean
aput-byte
aput-char
aput-object
aput-short
aput-wide
array-length
check-cast
cmp-long
cmpg-double
cmpg-float
cmpl-double
cmpl-float
const
const-class
const-string
const-string-jumbo
const-wide
const-wide/16
const-wide/32
const-wide/high16
const/16
const/4
const/high16
div-double
div-double/2addr
div-float
div-float/2addr
div-int
div-int/2addr
div-int/lit16
div-int/lit8
div-long
div-long/2addr
double-to-float
double-to-int
double-to-long
execute-inline
fill-array-data
filled-new-array
filled-new-array/range
float-to-double
float-to-int
float-to-long
goto
goto/16
goto/32
if-eq
if-eqz
if-ge
if-gez
if-gt
if-gtz
if-le
if-lez
if-lt
if-ltz
if-ne
if-nez
iget
iget-boolean
iget-byte
iget-char
iget-object
iget-object-quick
iget-quick
iget-short
iget-wide
iget-wide-quick
instance-of
int-to-byte
int-to-char
int-to-double
int-to-float
int-to-long
int-to-short
invoke-direct
invoke-direct-empty
invoke-direct/range
invoke-interface
invoke-interface/range
invoke-static
invoke-static/range
invoke-super
invoke-super-quick
invoke-super-quick/range
invoke-super/range
invoke-virtual
invoke-virtual-quick
invoke-virtual-quick/range
invoke-virtual/range
iput
iput-boolean
iput-byte
iput-char
iput-object
iput-object-quick
iput-quick
iput-short
iput-wide
iput-wide-quick
long-to-double
long-to-float
long-to-int
monitor-enter
monitor-exit
move
move-exception
move-object
move-object/16
move-object/from16
move-result
move-result-object
move-result-wide
move-wide
move-wide/16
move-wide/from16
move/16
move/from16
mul-double
mul-double/2addr
mul-float
mul-float/2addr
mul-int
mul-int/2addr
mul-int/lit8
mul-int/lit16
mul-long
mul-long/2addr
neg-double
neg-float
neg-int
neg-long
new-array
new-instance
nop
not-int
not-long
or-int
or-int/2addr
or-int/lit16
or-int/lit8
or-long
or-long/2addr
rem-double
rem-double/2addr
rem-float
rem-float/2addr
rem-int
rem-int/2addr
rem-int/lit16
rem-int/lit8
rem-long
rem-long/2addr
return
return-object
return-void
return-wide
sget
sget-boolean
sget-byte
sget-char
sget-object
sget-short
sget-wide
shl-int
shl-int/2addr
shl-int/lit8
shl-long
shl-long/2addr
shr-int
shr-int/2addr
shr-int/lit8
shr-long
shr-long/2addr
sparse-switch
sput
sput-boolean
sput-byte
sput-char
sput-object
sput-short
sput-wide
sub-double
sub-double/2addr
sub-float
sub-float/2addr
sub-int
sub-int/2addr
sub-int/lit16
sub-int/lit8
sub-long
sub-long/2addr
throw
ushr-int
ushr-int/2addr
ushr-int/lit8
ushr-long
ushr-long/2addr
xor-int
xor-int/2addr
xor-int/lit16
xor-int/lit8
xor-long
xor-long/2addr


	 */
	

}
