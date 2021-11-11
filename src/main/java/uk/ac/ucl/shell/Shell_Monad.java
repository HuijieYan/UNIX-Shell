// // package uk.ac.ucl.shell;

// <<<<<<< HEAD
// // import java.io.*;
// // import java.util.ArrayList;
// // import java.util.Scanner;

// // import uk.ac.ucl.shell.Parser.Monad;
// // import uk.ac.ucl.shell.Parser.ParserBuilder;
// // import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
// =======
// import java.io.*;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.Files;
// import java.util.ArrayList;
// import java.util.Scanner;

// import uk.ac.ucl.shell.Applications.Tools;
// import uk.ac.ucl.shell.Parser.Monad;
// import uk.ac.ucl.shell.Parser.ParserBuilder;
// import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
// >>>>>>> 8c7b3f1ed68a73e8e53b517b83968453d905a9a5

// // public class Shell_Monad {

// //     private static String currentDirectory = System.getProperty("user.dir");

// <<<<<<< HEAD
// //     //change to non-static at the moment
// //     public static void eval(String cmdline, OutputStream output) throws IOException {
// //         OutputStreamWriter writer = new OutputStreamWriter(output);
// =======
//     //change to non-static at the moment
//     public static void eval(String cmdline, OutputStream output) throws RuntimeException {
//         OutputStreamWriter writer = new OutputStreamWriter(output);
// >>>>>>> 8c7b3f1ed68a73e8e53b517b83968453d905a9a5

// //         // Using monad Parser
// //         ParserBuilder myParser = new ParserBuilder();
// //         Monad<ArrayList<ArrayList<ArrayList<String>>>> sat = myParser.lexCommand();
// //         MonadicValue<ArrayList<ArrayList<ArrayList<String>>>, String> result = sat.parse(cmdline);

// //         // System.out.println("result "+result.getValue());
// //         // System.out.println("input left "+result.getInputStream());

// //         //In seq
// //         for (ArrayList<ArrayList<String>> command:result.getValue()) {

// //             //System.out.println("Current Command -> "+command);

// //             //may meet null

// //             //dealing with pipe
// //             for (ArrayList<String> call: command) {
// //                 //System.out.println("Current call -> "+call);
// //                 String appName = call.get(0);
// //                 // tokens contain <app name> <arguments> where <arguments> is a list of argument
// //                 ArrayList<String> appArgs = new ArrayList<>(call.subList(1, call.size()));

// //                 //check globbing
// //                 //appArgs = ShellUtil.globbingChecker(appArgs, currentDirectory);


// //                 //change stream
// //                 ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
// //                 OutputStreamWriter bufferWriter = new OutputStreamWriter(bufferStream);
// //                 BufferedReader bufferedReader = new BufferedReader(new StringReader(bufferStream.toString()));

// <<<<<<< HEAD
// //                 ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, bufferWriter).createApp();

// //                 // keep track of directory
// //                 currentDirectory = myApp.exec(appArgs);


// //                 BufferedReader reader = new BufferedReader(new StringReader(bufferStream.toString()));
// //                 String line;
// //                 while ((line = reader.readLine()) != null){
// //                     writer.write(line);
// //                     writer.write(System.getProperty("line.separator"));
// //                     writer.flush();
// //                 }
// =======
//                 ArrayList<String> inputAndOutputFile = ShellUtil.checkRedirection(appArgs);
//                 if(inputAndOutputFile.get(0) != null){
//                     try {
//                         bufferedReader = Files.newBufferedReader(Tools.getPath(currentDirectory, inputAndOutputFile.get(0)), StandardCharsets.UTF_8);
//                     }catch (IOException e){
//                         throw new RuntimeException("can not open the input redirection file: " + inputAndOutputFile.get(0));
//                     }
//                 }

//                 ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, bufferWriter).createApp();
//                 // keep track of directory
//                 currentDirectory = myApp.exec(appArgs);

//                 if(inputAndOutputFile.get(1) != null){
//                     try {
//                         FileWriter outputFile = new FileWriter(inputAndOutputFile.get(1));
//                         outputFile.write(bufferStream.toString());
//                         outputFile.flush();
//                         outputFile.close();
//                     }catch (IOException e){
//                         throw new RuntimeException("fail to write to the output redirection file: " + inputAndOutputFile.get(1));
//                     }
//                 }else {
//                     try {
//                         writer.write(bufferStream.toString());
//                         writer.flush();
//                     }catch (IOException e){
//                         throw new RuntimeException("fail to print to the shell command line");
//                     }
//                 }
// >>>>>>> 8c7b3f1ed68a73e8e53b517b83968453d905a9a5

// //             }
// //         }
// //     }
    
// //     public static void main(String[] args) {
// //         if (args.length > 0) {
// //             if (args.length != 2) {
// //                 System.out.println("COMP0010 shell: wrong number of arguments");
// //                 return;
// //             }
// //             if (!args[0].equals("-c")) {
// //                 System.out.println("COMP0010 shell: " + args[0] + ": unexpected argument");
// //             }
// //             try {
// //                 Shell_Monad.eval(args[1], System.out);
// //             } catch (Exception e) {
// //                 System.out.println("COMP0010 shell: " + e.getMessage());
// //             }
// //         } else {
// //             try (Scanner input = new Scanner(System.in)) {
// //                 while (true) {
// //                     String prompt = currentDirectory + "> ";
// //                     System.out.print(prompt);
// //                     try {
// //                         String cmdline = input.nextLine();
// //                         Shell_Monad.eval(cmdline, System.out);
// //                     } catch (Exception e) {
// //                         System.out.println("COMP0010 shell: " + e.getMessage());
// //                     }
// //                 }
// //             }
// //         }
// //     }

// // }
