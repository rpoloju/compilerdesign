/**
* JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.SimpleParser;
import cop5556sp18.Scanner;
import cop5556sp18.SimpleParser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	// creates and returns a parser for the given input.
	private SimpleParser makeParser(String input) throws LexicalException {
		// show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
		// show(scanner); // Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}

	/**
	 * Simple test case with an empty program. This throws an exception because it
	 * lacks an identifier and a block. The test case passes because it expects an
	 * exception
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty1() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string.
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	/**
	 * Smallest legal program.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testSmallest1() throws LexicalException, SyntaxException {
		String input = "b{}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	// This test should pass in your complete parser. It will fail in the starter
	// code.
	// Of course, you would want a better error message.
	@Test
	public void testDec011() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "block1 {int c;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test11() throws LexicalException, SyntaxException {
		String input = "b*"; // The input is the empty string.
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	@Test
	public void orTest() throws LexicalException, SyntaxException {
		String input = "abc {image xy[23 , 44]; int x; x := 10;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void makeRedImage() throws LexicalException, SyntaxException {
		String input = "makeRedImage{image im[256,256];int x;int y;x:=0;y:=0;while(x<width(im)) {y:=0;while(y<height(im)) {im[x,y]:=<<255,255,0,0>>;y:=y+1;};x:=x+1;};show im;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testPolarR2() throws LexicalException, SyntaxException {
		String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testSamples() throws LexicalException, SyntaxException {
		String input = "samples{image bird; input bird from @0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};show bird2;sleep(4000);}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testDemo1() throws LexicalException, SyntaxException {
		String input = "demo1{image h;input h from @0;show h; sleep(4000); image g[width(h),height(h)];int x;x:=0;while(x<width(g)){int y;y:=0;while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testError1() throws LexicalException, SyntaxException {
		String input = "block1{while{}}"; // The input is the empty string.
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}

	}

	@Test
	public void test01() throws LexicalException, SyntaxException {
		String input = "xyz{image xy[sin(80),cos(atan(abs(cart_x(width(20)))))];}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test02() throws LexicalException, SyntaxException {
		String input = "abc{int ab;ab := (20 | 30 & 40 ** 50;}";
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test03() throws LexicalException, SyntaxException {
		String input = "abc{int ab;ab := (20 | 30 & 40 ** 50);}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test1() throws LexicalException, SyntaxException {
		String input = "true{}";
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	@Test
	public void test2() throws LexicalException, SyntaxException {
		String input = "crazy{" + "int a;" + "image b;" + "image c [23, 89];" + "input abc from @ 23;" + "while(true) {"
				+ "};" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test3() throws LexicalException, SyntaxException {
		String input = "hello{" + "int b;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test4() throws LexicalException, SyntaxException {
		String input = "hello{" + "image pix[43,43];" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test5() throws LexicalException, SyntaxException {
		String input = "hello{" + "image pix;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test6() throws LexicalException, SyntaxException {
		String input = "hello{" + "input abc from @ 2;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test7() throws LexicalException, SyntaxException {
		String input = "yo   {" + "image pix[yo,yo];" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test8() throws LexicalException, SyntaxException {
		String input = "hey{" + "while(true) {" + "};" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test9() throws LexicalException, SyntaxException {
		String input = "b {" + "int a;" + "a := 90;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test10() throws LexicalException, SyntaxException {
		String input = "demo1{" + "image h;" + "input h from @ 0;" + "show h;" + "sleep(4000);"
				+ "image g[width(h), height(h)];" + "int x;" + "x := 0;" + "while(x<width(g)){" + "int y;" + "y:=0;"
				+ "while(y<height(g)){" + "g[x,y] := h[y,x];" + "y := y+1;" + "};" + "x := x+1;" + "};" + "show g;"
				+ "sleep(4000);" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void random() throws LexicalException, SyntaxException {
		String input = " demo{ " + "image h;" + "input h from @ 0;" + "show h;" + "sleep(4000); "
				+ "image g[width(h), height(h)];" + "int x;" + "x := <<343, 433, 3434,434 >>;" + "x := 0;"
				+ "while (x < width(g)){" + "int y;" + "y := 0;" + "while (y < height(g)){" + "g[x,y] := h[y,x];"
				+ "y:=y+1;" + "};" + "x:=x+1;" + "};" + "show g;" + "sleep(4000);" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void random1() throws LexicalException, SyntaxException {
		String input = " makeredimage{ " + "image im[256,256];" + "int x;" + "int y;" + "x:=0;" + "y:= 0; "
				+ "while (x < width(im)){" + "y := 0;" + "while (y < height(im)){" + "im[x,y] := <<255,255,0,0 >>;"
				+ "y:=y+1;" + "};" + "x:=x+1;" + "};" + "show im;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void random2() throws LexicalException, SyntaxException {
		String input = " polarR2{ " + "image im[256,256];" + "int x;" + "x:=0;" + "while (x < width(im)){" + "int y;"
				+ "y := 0;" + "while (y < height(im)){" + "float p;" + "p:=polar_R[x,y];" + "int r;" + "r := int(p)%Z;"
				+ "im[x,y] := <<Z,255,0,r >>;" + "y:=y+1;" + "};" + "x:=x+1;" + "};" + "show im;" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void random3() throws LexicalException, SyntaxException {
		String input = " samples{ " + "image bird;" + "input bird from @0;" + "show bird;" + "sleep(4000);"
				+ "image bird2[width(bird),height(bird)];" + "int x; x :=0;" + "while (x < width(bird2)){" + "int y;"
				+ "y := 0;" + "while (y < height(bird2)){" + "blue(bird2[x,y]) :=  red(bird[x,y]);"
				+ "green(bird2[x,y]) :=  blue(bird[x,y]);" + "alpha(bird2[x,y]) :=  Z;"
				+ "red(bird2[x,y]) :=  green(bird[x,y]);" + "y:=y+1;" + "};" + "x:=x+1;" + "};"
				+ "show bird2; sleep(4000);" + "}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testDemo11() throws LexicalException, SyntaxException {
		String input = "demo1{" + "image h;" + "input h from @0;" + "show h;" + "sleep(4000);"
				+ "image g[width     (h),height(h)];" + "int x;" + "x:=0;" + "while(x<width(g)){" + "int y;" + "y:=0;"
				+ "while(y<height(g)){" + "g[x,y]:=h[y,x];" + "y:=y+1;};" + "x:=x+1;};" + "show g;" + "sleep(4000);}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void makeRedImage1() throws LexicalException, SyntaxException {
		String input = "makeRedImage{image im[256,256];int x;int y;x:=0;y:=0;while(x<width(im)) {y:=0;while(y<height(im)) {im[x,y]:=<<255,255,0,0>>;y:=y+1;};x:=x+1;};show     im;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testPolarR21() throws LexicalException, SyntaxException {
		String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;     y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,     0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testSamples1() throws LexicalException, SyntaxException {
		String input = "samples{image bird; input bird from @0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+       1;};show bird2;sleep(4000);}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testSample() throws LexicalException, SyntaxException {
		String input = "abc{input abc from @ 456; write abc to a758; red (a567 [345,456]) := 85467; while(true) {if(false){show testcases;sleep 345.;};};red (abc [345,876.54]):= (5678.89 ? 345:false); "
				+ "	show  <<!abc23[92,ty789[345,true]],+default_height,cart_y(45)*log(.32)+.123 + cart_x(48)*sin(45)+.456 >= 35 == 45 & 32.96 ? ab2:true,89>>;}";
		SimpleParser parser = makeParser(input);
		parser.parse();

	}
}
