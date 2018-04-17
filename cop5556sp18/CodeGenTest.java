/**
 * Starter code with JUnit tests for code generation used in the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.CodeGenUtils.DynamicClassLoader;
import cop5556sp18.AST.Program;

public class CodeGenTest {

	// determines whether show prints anything
	static boolean doPrint = true;

	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	// determines whether a classfile is created
	static boolean doCreateFile = false;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// values passed to CodeGenerator constructor to control grading and debugging
	// output
	private boolean devel = true; // if true, print devel output
	private boolean grade = true; // if true, print grade output

	// private boolean devel = false;
	// private boolean grade = false;

	// sets the default width and height of newly created images. Should be small
	// enough to fit on screen.
	public static final int defaultWidth = 1024;
	public static final int defaultHeight = 1024;

	/**
	 * Generates bytecode for given input. Throws exceptions for Lexical, Syntax,
	 * and Type checking errors
	 * 
	 * @param input
	 *            String containing source code
	 * @return Generated bytecode
	 * @throws Exception
	 */
	byte[] genCode(String input) throws Exception {

		// scan, parse, and type check
		Scanner scanner = new Scanner(input);
		show(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeChecker v = new TypeChecker();
		program.visit(v, null);
		// show(program); //It may be useful useful to show this here if code generation
		// fails

		// generate code
		CodeGenerator cv = new CodeGenerator(devel, grade, null, defaultWidth, defaultHeight);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		show(program); // doing it here shows the values filled in during code gen
		// display the generated bytecode
		show(CodeGenUtils.bytecodeToString(bytecode));

		// write byte code to file
		if (doCreateFile) {
			String name = ((Program) program).progName;
			String classFileName = "bin/" + name + ".class";
			OutputStream output = new FileOutputStream(classFileName);
			output.write(bytecode);
			output.close();
			System.out.println("wrote classfile to " + classFileName);
		}

		// return generated classfile as byte array
		return bytecode;
	}

	/**
	 * Run main method in given class
	 * 
	 * @param className
	 * @param bytecode
	 * @param commandLineArgs
	 *            String array containing command line arguments, empty array if
	 *            none
	 * @throws +
	 * @throws Throwable
	 */
	void runCode(String className, byte[] bytecode, String[] commandLineArgs) throws Exception {
		RuntimeLog.initLog(); // initialize log used for grading.
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		@SuppressWarnings("rawtypes")
		Class[] argTypes = { commandLineArgs.getClass() };
		Method m = testClass.getMethod("main", argTypes);
		show("Output from " + m + ":"); // print name of method to be executed
		Object passedArgs[] = { commandLineArgs }; // create array containing params, in this case a single array.
		try {
			m.invoke(null, passedArgs);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				Exception ec = (Exception) e.getCause();
				throw ec;
			}
			throw e;
		}
	}

	/**
	 * When invoked from JUnit, Frames containing images will be shown and then
	 * immediately deleted. To prevent this behavior, waitForKey will pause until a
	 * key is pressed.
	 * 
	 * @throws IOException
	 */
	void waitForKey() throws IOException {
		System.out.println("enter any char to exit");
		System.in.read();
	}

	/**
	 * When invoked from JUnit, Frames containing images will be shown and then
	 * immediately deleted. To prevent this behavior, keepFrame will keep the frame
	 * visible for 5000 milliseconds.
	 * 
	 * @throws Exception
	 */
	void keepFrame() throws Exception {
		Thread.sleep(5000);
	}

	/**
	 * Since we are not doing any optimization, the compiler will still create a
	 * class with a main method and the JUnit test will execute it.
	 * 
	 * The only thing it will do is append the "entering main" and "leaving main"
	 * messages to the log.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String prog = "emptyProg";
		String input = prog + "{}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n " + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void integerLit() throws Exception {
		String prog = "intgegerLit";
		String input = prog + "{show 2.5**2;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test01() throws Exception {
		String prog = "intDeclare";
		String input = prog + "{int a;a:=2;float b; b := 4.0;show a;show b;show a**b;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test02() throws Exception {
		String prog = "testSleep";
		String input = prog + "{sleep(4000);} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		//assertEquals("entering main;3;leaving main;", RuntimeLog.globalLog.toString());
	}
	 

	@Test
	public void test03() throws Exception {
		String prog = "testFunctions";
		String input = prog + "{float f;f:=1.0;float s;s:=log(f);show s;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test04() throws Exception {
		String prog = "testFunctions";
		String input = prog + "{int x;x:=-1;int y;y:=abs(13-50);show x;show y;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test05() throws Exception {
		String prog = "testUnary";
		String input = prog + "{int a;a:=10;show(!a);} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test06() throws Exception {
		String prog = "testUnarybool";
		String input = prog + "{boolean a;a:=false;show(!a);} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test07() throws Exception {
		String prog = "testImage1";
		String input = prog + "{image a[200,300];show a;/*sleep(5000);*/} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		System.out.println("stopped for convenience");
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test08() throws Exception {
		String prog = "testFile";
		String input = prog + "{filename f;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test09() throws Exception {
		String prog = "testfilename2";
		String input = prog + "{image a;input a from @ 0;show(a);} ";
		byte[] bytecode = genCode(input);
		// String[] commandLineArgs = { "E:\\Back.JPG" };
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test10() throws Exception {
		String prog = "testLocation";
		String input = prog + "{int a;input a from @ 1;show a;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = { "3", "4" };
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test11() throws Exception {
		String prog = "testImage";
		String input = prog + "{image a;input a from @ 0;image b;b:=a;show(b);} ";
		byte[] bytecode = genCode(input);
		//String[] commandLineArgs = { "E:\\Back.JPG" };
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test12() throws Exception {
		String prog = "testscope";
		String input = prog + "{int a;int b;a:=10;b:=20;int c;c := a+b;int d;d:= a-b;show(c);show(d);} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test13() throws Exception {
		String prog = "testor";
		String input = prog + "{show true|false;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test14() throws Exception {
		String prog = "testNot";
		String input = prog + "{show !1; show !-1; show !0; show !-2;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test15() throws Exception {
		String prog = "testInt";
		String input = prog + "{int a; a := int(-3.7); show a; a := int(4); show a;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test16() throws Exception {
		String prog = "testFloat";
		String input = prog + "{float a; a := float(-3.7); show a; a := float(4); show a;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test17() throws Exception {
		String prog = "testAlpha";
		String input = prog + "{int a; a := 123456789; show alpha(a);\n a := -1; show alpha(a);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test18() throws Exception {
		String prog = "testColors";
		String input = prog + "{int a; a := 123456789;\n show red(a); show green(a); show blue(a);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test19() throws Exception {
		String prog = "testDimensions";
		String input = prog
				+ "{image b[512,256]; show width(b); show height(b);\nimage c; show width(c); show height(c);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; // create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test20() throws Exception {
		String prog = "testImageCopy";
		String input = prog
				+ "{image y[1000,1000]; image copy[1000,1000]; input y from @ 0 ; show y; copy := y; show copy;}";
		byte[] bytecode = genCode(input);
		// String[] commandLineArgs = { "E:\\Back.JPG" };
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test21() throws Exception {
		String prog = "testArgs";
		String input = prog
				+ "{int x; input x from @ 0 ; show x;\nfloat y; input y from @ 1; show y;\nboolean z; input z from @ 2; show z;\ninput z from @ 3; show z;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = { "2", "3.14", "false", "true" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test22() throws Exception {
		String prog = "testArgs2";
		String input = prog + "{int x; input x from @ 0; show (x); int y; input y from @ 0; show (y);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = { "2", "3.14", "false", "true" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test23() throws Exception {
		String prog = "testPolar";
		String input = prog + "{show(cart_x[polar_r[4,2],polar_a[4,2]]);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test24() throws Exception {
		String prog = "testlhspixel";
		String input = prog + "{image x;input x from @ 0;show(x);show(x[2,4]);x[2,4] := 5;show(x[2,4]);}";
		byte[] bytecode = genCode(input);
		//String[] commandLineArgs = {"E:\\Back.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test25() throws Exception {
		String prog = "testWrite";
		String input = prog + "{image x;input x from @ 0;show(x);filename y;input y from @ 1;write x to y;}";
		byte[] bytecode = genCode(input);
		// String[] commandLineArgs = {"E:\\Back.JPG", "E:\\Back0002.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG", "/cise/homes/rpoloju/Backwrite.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test26() throws Exception {
		String prog = "testPixelSelector";
		String input = prog + "{image x;input x from @ 0;show(x[3,4]);}";
		byte[] bytecode = genCode(input);
		// String[] commandLineArgs = {"E:\\Back.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test27() throws Exception {
		String prog = "testColors";
		String input = prog + "{image x;input x from @ 0;show(red(x[3,4]));}";
		byte[] bytecode = genCode(input);
		// String[] commandLineArgs = {"E:\\Back.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test28() throws Exception {
		String prog = "testIfInt";
		String input = prog + "{int x; x:=3; if (x == 0) {show(10);};show(20);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test31() throws Exception {
		String prog = "testIffloat";
		String input = prog + "{float x; x:=3.5; if (x <= 3.4) {show(10);};show(20);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test35() throws Exception {
		String prog = "testIfboolean";
		String input = prog + "{boolean x; x:=true; if (x < false) {show(10);};show(20);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test29() throws Exception {
		String prog = "testexpcond";
		String input = prog + "{int x;int y; x:=3; show x < 0 ? 4 : 5;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test30() throws Exception {
		String prog = "textPixelConst";
		String input = prog + "{image im[256,256];int x;int y;x:=10;y:=20;im[x,y]:=<<255,255,0,0>>;show(im[x,y]);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}

	@Test
	public void test38() throws Exception {
		String prog = "test2";
		String input = prog+ "{float x;x:=9.1;float y;y:=4.1;show(x-y);}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void test39() throws Exception {
		String prog = "testPixelPolar";
		String input = prog+ "{image x; input x from @ 0; x[0.5,1.0] := 0;show(x);sleep(4000);}";
		byte[] bytecode = genCode(input);
		//String[] commandLineArgs = {"E:\\Back.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void test40() throws Exception {
		String prog = "testWhileCond";
		String input = prog+ "{int x; x:=10;while (x > 0) {show(x); x:=x-1;};}";
		byte[] bytecode = genCode(input);
		//String[] commandLineArgs = {"E:\\Back.JPG"};
		String[] commandLineArgs = { "/cise/homes/rpoloju/Back.JPG" };
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		// assertEquals("entering main;3;leaving main;",
		// RuntimeLog.globalLog.toString());
	}
	

}