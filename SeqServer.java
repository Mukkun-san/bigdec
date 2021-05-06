//
// The following shows the general form of a server that
// processes requests from different clients one-at-a-time.
// That is, it listens for a request from a client, processes
// that request, then looks for another client, and another,
// and another, etc.
//
// Note that the loop is an infinite loop ("while(true)"),
// so the program must be terminated manually (using Linux's
// CTRL-C command, for example). 
//
// "..." below stands for omitted processing statements that
// would be resolved with specific processing statements,
// depending on the desired server behavior.

import java.util.*;
import java.net.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SeqServer {

   public static final Integer BigDecScale = 5;

   public static boolean isBigDecimalNumber(String num) {
      try {
         new BigDecimal(num);
         return true; // if the String converted successfully to BigDecimal return true
      } catch (Exception e) {
         return false;
      }
   }

   public static String shuffle(String x, String y) {
      int xBefore = x.indexOf(".");
      int xAfter = x.length() - xBefore - 1;
      int yBefore = y.indexOf(".");
      int yAfter = y.length() - yBefore - 1;
      int diff;
      char[] zeros = new char[0];

      { // left padding with zeros
         diff = xBefore - yBefore;
         if (diff != 0) {
            zeros = new char[Math.abs(diff)];
            Arrays.fill(zeros, '0');
         }
         if (diff < 0) {
            x = new String(zeros) + x;
         } else if (diff > 0) {
            y = new String(zeros) + y;
         }
      }

      { // right padding with zeros
         diff = xAfter - yAfter;
         if (diff != 0) {
            zeros = new char[Math.abs(diff)];
            Arrays.fill(zeros, '0');
         }
         if (diff < 0) {
            x += new String(zeros);
         } else if (diff > 0) {
            y += new String(zeros);
         }
      }

      int dotIdx = x.indexOf('.');
      String shuffle = "";

      System.out.println(x);
      System.out.println(y);

      for (int i = 0; i < x.length(); i++) {
         if (i == dotIdx) {
            shuffle += '.';
            continue;
         }
         shuffle += (x.charAt(i)) + "" + (y.charAt(i)); // empty string used to force the compiler to use the
                                                        // String values of chars instead of ASCII values
      }
      return (new BigDecimal(shuffle).stripTrailingZeros().toString());
   }

   public static void main(String[] args) {

      SeqServer.isBigDecimalNumber("");

      ServerSocket serverSocket = null;
      Socket socket = null;
      int port;
      boolean listening = true; // assume serverSocket creation was OK
      boolean EXIT = false;

      final List<String> VALID_OPERATIONS = Arrays.asList(new String[] { "add", "sub", "mult", "shuffle" });
      final int DECIMAL_SCALE = 7;

      // get port # from command-line

      port = Integer.parseInt(args[0]);

      // try to create a server socket

      try {
         serverSocket = new ServerSocket(port);
      } catch (IOException e) {
         System.out.println(e);
         listening = false;
      }

      if (listening) // i.e., serverSocket successfully created
      {
         System.out.println("Server " + serverSocket.getInetAddress() + "---" + serverSocket.getLocalSocketAddress()
               + " listening to port:" + port);
         // continue to:
         //
         // (1) Listen for a client request
         // (2) Read data from the client
         // (3) Process the request: do calculation and return value
         //

         while (true) // main processing loop
         {
            try {

               // Listen for a connection request from a client

               socket = serverSocket.accept();

               // Establish the input and output streams on the socket

               PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
               BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

               String user_question = in.readLine().trim();
               if (user_question.toLowerCase().equals("quit")) {
                  EXIT = true;
               } else {
                  String num1 = in.readLine();
                  String num2 = in.readLine();

                  System.out.println(user_question);
                  System.out.println(num1);
                  System.out.println(num2);

                  if (!VALID_OPERATIONS.contains(user_question)) {
                     out.println("Error:  Unknown operation");
                  } else if (!isBigDecimalNumber(num1) && !isBigDecimalNumber(num2)) {
                     out.println("Error:  Both values: not numbers");
                  } else if (!isBigDecimalNumber(num1)) {
                     out.println("Error:  First value: not a number");
                  } else if (!isBigDecimalNumber(num2)) {
                     out.println("Error:  Second value: not a number");
                  } else {
                     BigDecimal dec1 = new BigDecimal(num1);
                     BigDecimal dec2 = new BigDecimal(num2);
                     String result;
                     switch (user_question) {
                     case "add":
                        result = dec1.add(dec2).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
                              .toString();
                        break;

                     case "sub":
                        result = dec1.subtract(dec2).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
                              .toString();
                        break;

                     case "mult":
                        result = dec1.multiply(dec2).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
                              .toString();
                        break;

                     case "shuffle":
                        result = shuffle(num1, num2);
                        break;

                     default:
                        result = "";
                        break;
                     }
                     out.println("Result:  " + result);
                  }
               }

               // Read data from the client, do calculation(s),
               // return data value(s)

               // close connection to client

               out.close();
               in.close();
               socket.close();

               if (EXIT) {
                  break;
               }

            } catch (IOException e) {
               System.out.println(e);
            }

         } // end while (main processing loop)

      } // end if listening
      System.out.println("Server Quitted");
   } // end main

} // end seqserver
