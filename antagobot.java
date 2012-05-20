/* ************************************************
   AntagoBot
   	AUTHOR:	Jonathan Rothwell
	DATE:	September - December 2011

	A simple chatbot that deliberately attempts to
	be antagonistic towards the user.
 */

// IMPORTS
import java.io.*;
import java.util.*;

class antagobot
{
	
	public static Random gen = new Random(); // instantiate and seed the randomiser here--it should only happen once
	public static boolean DEBUG_MODE = false; // this is Debug Mode, enabled at the command line.
	public static final String niceName = "Paul"; // if the user enters this name, AntagoBot will err on the side of caution. NB: Only enter the name of someone who is truly lovely--i.e. funny, clever, handsome, brave, kind and it's a bloody wonder they haven't been knighted.

	public static void main (String[] param)
	{
/*
 * The main sequence of events in a conversation with AntagoBot is as follows:
 * 1. AntagoBot complains that you have woken him.
 * 2. AntagoBot demands your name.
 * 3. If AntagoBot knows you, it will say "hi again." If not and
 *    your name isn't the designated "nice name", AntagoBot will
 *    make a rude remark about it pulled from namejokes.conf.
 * 4. AntagoBot pulls a rude question from insults.conf.
 * 5. If you answer yes or no, AntagoBot makes a snide remark
 *    and moves to the next question. Otherwise it will keep 
 *    asking the original question until you answer yes or no.
 * 6. If you use a terminating word AntagoBot will again
 *    complain, and then terminate.
 */

		//First, though, do we want Debug Mode?
		try {
			if(param[0].equals("debug"))
				DEBUG_MODE = true;
			DEBUG("ANTAGOBOT starting in debug mode");
		}
		catch  (ArrayIndexOutOfBoundsException ex) {
			System.out.println("ANTAGOBOT starting in non-debug mode");
		}


		asays(randItem("grumbles.conf"));
		String name = luser();
		if(name.equalsIgnoreCase(niceName)) // just to be on the safe side
		{
			asays(niceName + "... that's a lovely name. Genuinely. Seriously.");
		}
		else if(doiknow(name)) {
			asays("Oh... it's you. Hi again.");
		}
		else // give them hell
		{
			writeOut("known.names", name);
			asays(name + "... " + randItem("namejokes.conf"));
		}

		String question, response;
		
		for(;;)	// main loop
		{
			question = rudeQuestion();
			asays("So, " + question);
			response = luser();
			if(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("yeah") || response.equalsIgnoreCase("yup")) {
				asays(randItem("yes-1att.conf"));
			}
			else if(response.equalsIgnoreCase("no") || response.equalsIgnoreCase("nah") || response.equalsIgnoreCase("nope")) {
				asays(randItem("no-1att.conf"));
			}
			else if(response.equalsIgnoreCase("bye") || response.equalsIgnoreCase("goodbye") || response.equalsIgnoreCase("quit") || response.equalsIgnoreCase("sod off")) {
				break;
			}
			else if(response.contains("?")) {
				asays("I'm asking the questions.");
			}
			else { // the user is being evasive, so keep pushing
			
				asays("Don't you understand me? I'm asking you " + question);
				for(;;)	{
					response = luser();
					if(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("yeah") || response.equalsIgnoreCase("yup"))
					{
						asays(randItem("yes-fatt.conf"));
						break;
					}
					else if(response.equalsIgnoreCase("no") || response.equalsIgnoreCase("nah") || response.equalsIgnoreCase("nope"))
					{
						asays(randItem("no-fatt.conf"));
						break;
					}
					else if(response.equalsIgnoreCase("bye") || response.equalsIgnoreCase("goodbye") || response.equalsIgnoreCase("quit"))
					{
						asays("Don't think you're getting out of this so easily. I won't leave until you answer me " + question);
					}
					else // This is the only block that doesn't break out of the loop and move on to another question.
					{
						asays(randItem("persist.conf") + " " + question);
					}
				}
			}
		}

		asays(valediction());

		// all is well, so terminate cleanly
		System.exit(0);
	} // END main()

	public static void DEBUG (String msg) {
		if(DEBUG_MODE)
			System.out.println("DEBUG: " + msg); // ...print a debug message.
	}


	/* void asays (String response)
	 * Prints AntagoBot's responses in the style "AntagoBot:\t(response)"
	 */
	public static void asays (String response)
	{
		System.out.println("AntagoBot:\t" + response);
	} // END asays()



	/* String luser()
	 * Gets user input from a formatted prompt.
	 */
	public static String luser ()
	{
		System.out.print("You:\t\t");
		return gets();
	} // END luser()



	/* String gets ()
	 * Gets a string from the terminal. Method exists
	 * for my own convenience and cleaner code.
	 * Should probably be moved to its own package at
	 * some point.
	 * Much like the gets() function in C, and hence
	 * possibly insecure.
	 */
	public static String gets ()
	{
		Scanner input = new Scanner(System.in);
		return input.nextLine();
	} // END gets()



	/* String rudeQuestion ()
	 * Asks a rude or invasive question randomly selected from insults.conf.
	 */
	public static String rudeQuestion ()
	{
		try
		{
			String questionToAsk = randomLine(loadLines("insults.conf"));
			return questionToAsk;
		}
		catch (IOException ex)
		{
			return "was it you who caused the IOException I just experienced?";
		}
	} // END rudeQuestion()



	/* String valediction ()
	 * Prints a rude farewell.
	 */
	public static String valediction ()
	{
		try
		{
			return randomLine(loadLines("valedictions.conf"));
		}
		catch (IOException ex)
		{
			return "You're running away. It's because you don't want me to berate you for that IOException I just encountered, isn't it? You horrible little human twonk!";
		}
	} // END valediction()

/********************************************
 * PLUMBING
 * These are the methods that do the actual gruntwork, i.e. fetching insults
 * from files, loading them into arrays, choosing one at random etc.
 */

/* String[] loadLines(fname)
 * Returns a string array of all insults from file fname. If not found, throws
 * an exception. Ignores all lines commencing with the # character.
 */
	public static String[] loadLines(String fname) throws IOException
	{
		List<String> lines = new ArrayList<String>();
		FileReader fin = new FileReader(fname);
		BufferedReader buffer = new BufferedReader(fin);
	
		String line = null;
		while((line = buffer.readLine()) != null)
		{
			if(line.length() == 0) // ignore blank lines
			{
				DEBUG("Ignoring blank line...");
			}
			else if(line.charAt(0) == '#') // ignore because it starts with # and is, therefore, a comment
			{
				DEBUG("Ignoring commented line: " + line);
			}
			else
			{
				DEBUG("Adding line: " + line);
				lines.add(line);
			}
		}
		buffer.close();
		fin.close();
		return lines.toArray(new String[lines.size()]);
	}

/* String randomLine(inArray[])
 * Returns a random string from inArray.
 */
	public static String randomLine(String[] inArray)
	{
		int rnd = gen.nextInt(inArray.length);
		return inArray[rnd];
	} // end randomLine(inArray)


/* String randItem(fileName)
 * Returns a random line from fileName, in event of an error returns a rude literal.
 */
	public static String randItem(String fileName)
	{
		try {
			return randomLine(loadLines(fileName));
		}
		catch (IOException ex) {
			return "Well, snap. You just caused an IOException. It's your fault. You smelly human footweasel.";
		}
	} // end randItem()

/* String [] quicksort(String haystack[])
 * Sorts haystack[] using Tony Hoare's quick sort algorithm
 * and returns the result as a string array.
 */
 	public static String [] quicksort(String haystack[]) {
 		if(DEBUG_MODE) {
 			String debugString = "Quicksorting the items: ";
 			for(String key : haystack) {
 				debugString = debugString + key + ", ";
 			}
 			DEBUG(debugString);
 		}


 		if(haystack.length <= 1) return haystack; // an array of 1 or 0 items is already sorted.

 		String pivot = haystack[0];

 		List<String> lessThan = new ArrayList<String>();
 		List<String> greaterThan = new ArrayList<String>();

 		for(String key : haystack) {
 			if(key.equals(pivot)) {
 				DEBUG("Pivot (" + key +","+pivot +") found, IGNORING...");
 			}
 			else if(after(key, pivot)) {
 				greaterThan.add(key);
 			}
 			else {
 				lessThan.add(key);
 			}
 		}

 		return conc(quicksort(lessThan.toArray(new String[lessThan.size()])), pivot, quicksort(greaterThan.toArray(new String[greaterThan.size()]))); // recurse, concatenate and return
 	} // end quicksort()

 	/* String[] conc(String A[], String P, String B[])
 	 * Concatenates String Array A, String P and String Array B.
 	 */
 	public static String [] conc(String A[], String P, String B[]) {
 		String [] R = new String[A.length + B.length + 1];

 		System.arraycopy(A, 0, R, 0, A.length);
 		String [] Pi = new String[1]; // you can't copy strings directly using arraycopy, you have to do this clunky thing
 		Pi[0] = P;
 		System.arraycopy(Pi, 0, R, A.length, 1);
 		System.arraycopy(B, 0, R, (A.length + 1), B.length);

 		return R;

 	} // end conc()

 	/* boolean after(String x, String y)
 	 * Returns true if x is alphabetically after y.
 	 * Otherwise false.
 	 */
 	public static boolean after(String x, String y) {
 		int result = x.compareTo(y);
 		if(result > 0) {
 			DEBUG(x+ " is after " + y);
	 		return true;
	 	}
 		else {
 			DEBUG(x+ " is before " + y);
	 		return false;
	 	}
 	} // end after()

 	/* boolean arrayContains(String needle, String haystack)
 	 * Linear search: true if haystack contains needle,
 	 * otherwise false.
 	 */
 	public static boolean arrayContains(String needle, String haystack[]) {
 		for(String s : haystack) {
 			if(s.equals(needle)) return true;
 		}
 		return false;
 	} // end arrayContains()

 	/* void WriteOut
 	 * Writes the line item followed by newline to fname.
 	 */
 	public static void writeOut(String fname, String item) {
 		try {
 			BufferedWriter fOut = new BufferedWriter(new FileWriter(fname, true));
 			fOut.write(item + "\n");
 			fOut.close();
 		}
 		catch (IOException e) {
 			asays("Unfortunately, I've just encountered an IOException. Which is a pity. I won't remember you at all next time you see me. So I won't remember the pain.");
 		}
 	} // end writeOut()

 	/* boolean doiknow
 	 * True if name is in known.names, otherwise false.
 	 */
 	public static boolean doiknow(String name) {
 		if(!(new File("known.names")).exists()) return false;
 		try {
 			DEBUG("known.names DOES exist");
 			String lines[] = loadLines("known.names");
 			String sortedLines[] = quicksort(lines);

 			if(DEBUG_MODE) {
 				String debugString = "Quicksorted array: ";
 				for(String key: sortedLines) {
 					debugString = debugString + key + ", ";
 				}
 				DEBUG(debugString);
 			}
 			if(arrayContains(name, sortedLines)) {
 				return true;		
 			} 
 		}
 		catch (IOException e) {
 			asays("Fozzing IOExceptions. Your file system is deliberately trying to destroy me, isn't it? You monster.");
 			return false;
 		}
 		return false;
 	} // end doiknow()

}
