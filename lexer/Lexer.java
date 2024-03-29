package lexer;

import java.util.Scanner;

public class Lexer {
  private boolean atEOF = false;
  private char ch;
  private SourceReader source;

  private int startPosition, endPosition, line;

  public Lexer(String sourceFile) throws Exception {
    new TokenType();
    source = new SourceReader(sourceFile);
    ch = source.read();
  }

  public Token newIdToken(String id, int startPosition, int endPosition, int line) {
    return new Token(startPosition, endPosition, line, Symbol.symbol(id, Tokens.Identifier));
  }

  public Token newNumberToken(String number, int startPosition, int endPosition, int line) {
    return new Token(startPosition, endPosition, line, Symbol.symbol(number, Tokens.INTeger));
  }

  public Token newDateLit(int startPosition, int endPosition, int line) {
    return new Token(startPosition, endPosition, line, Symbol.symbol("<date>", Tokens.INTeger));
  }

  public Token newNumberLit(int startPosition, int endPosition, int line) {
    return new Token(startPosition, endPosition, line, Symbol.symbol("<number>", Tokens.NumberLit));
  }

  public Token newDateLit(String number, int startPosition, int endPosition, int line) {
    return new Token(startPosition, endPosition, line, Symbol.symbol(number, Tokens.NumberLit));
  }

  public Token makeToken(String tokenString, int startPosition, int endPosition, int line) {
    if (tokenString.equals("//")) {
      try {
        int oldLine = source.getLineno();
        do {
          ch = source.read();
        } while (oldLine == source.getLineno());
      } catch (Exception e) {
        atEOF = true;
      }
      return nextToken();
    }

    Symbol symbol = Symbol.symbol(tokenString, Tokens.BogusToken);
    if (symbol == null) {
      System.out.println("******** illegal character: " + tokenString);
      atEOF = true;
      System.out.print("\n" + SourceReader.endLine);
      return nextToken();
    }

    return new Token(startPosition, endPosition, line, symbol);
  }

  public Token nextToken() {
    if (atEOF) {
      if (source != null) {
        source.close();
        source = null;
      }
      return null;
    }
    try {
      while (Character.isWhitespace(ch)) {
        ch = source.read();
      }
    } catch (Exception e) {
      atEOF = true;
      return nextToken();
    }

    startPosition = source.getPosition();
    endPosition = startPosition - 1;
    line = source.getLineno();

    if (Character.isJavaIdentifierStart(ch)) {
      String id = "";

      try {
        do {
          endPosition++;
          id += ch;
          ch = source.read();
        } while (Character.isJavaIdentifierPart(ch));
      } catch (Exception e) {
        atEOF = true;
      }
      return newIdToken(id, startPosition, endPosition, line);
    }

    if (Character.isDigit(ch)) {
      String number = "";
      try {
        do {
          endPosition++;
          number += ch;
          ch = source.read();
        } while (Character.isDigit(ch) || ch == '.' || ch == '-');
      } catch (Exception e) {
        atEOF = true;
      }

      if (number.contains("-")) {
        return newDateLit(startPosition, endPosition, line);
      } else if (number.contains(".")) {
        return newNumberLit(startPosition, endPosition, line);
      } else {
        return newNumberToken(number, startPosition, endPosition, line);
      }
    }

    String charOld = "" + ch;
    String op = charOld;
    Symbol sym;
    try {
      endPosition++;
      ch = source.read();
      op += ch;

      sym = Symbol.symbol(op, Tokens.BogusToken);
      if (sym == null) {
        return makeToken(charOld, startPosition, endPosition, line);
      }

      endPosition++;
      ch = source.read();

      return makeToken(op, startPosition, endPosition, line);
    } catch (Exception e) {
    }

    atEOF = true;
    if (startPosition == endPosition) {
      op = charOld;
    }

    return makeToken(op, startPosition, endPosition, line);
  }

  public static String evenString(Symbol token, int left, int right, int line, String sym) {
    String tokenString = (token != null) ? token.toString() : "null";
    String kindString = (token != null && token.getKind() != null) ? token.getKind().toString() : "null";

    return String.format("%-11s left: %-8s right: %-8s line: %-8s %s", tokenString, left, right, line, kindString);
  }

  public static void main(String args[]) {
    Token token;

    boolean exit = false;
    Scanner scanner = new Scanner(System.in);

    while (!exit) {

      System.out.println("Choose a file:");
      System.out.println("1. codegen.x");
      System.out.println("2. error.x");
      System.out.println("3. factorial.x");
      System.out.println("4. factorialErr.x");
      System.out.println("5. fib.x");
      System.out.println("6. scopes.x");
      System.out.println("7. simple.x");
      System.out.println("0. Exit");

      int choice = 0;

      try {
        choice = scanner.nextInt();
      } catch (Exception e) {
        System.out.println("Invalid input. Please enter a number.");
        System.out.println("-----------------\n");
        scanner.nextLine(); // Clear the input buffer
        continue; // Restart the loop
      }

      String[] fileNames = { "codegen.x", "error.x", "factorial.x", "factorialErr.x", "fib.x",
          "scopes.x",
          "simple.x", };

      if (choice >= 1 && choice <= fileNames.length) {
        try {
          Lexer newLexer = new Lexer("sample_files/" + fileNames[choice - 1]);
          token = newLexer.nextToken();

          while (token != null) {
            System.out.println(evenString(token.getSymbol(), token.getLeftPosition(), token.getRightPosition(),
                token.getLineFound(), token.getKind().toString()));
            token = newLexer.nextToken();
          }

          System.out.println("******** end of file ******* \n \n \n \n");
        } catch (Exception e) {
          System.out.println("******** exception *******" + e.toString());
        }
      } else if (choice == 0) {
        exit = true;
      } else {
        System.out.println("Invalid choice.");
        System.out.println("-----------------\n");
      }

    }
    scanner.close();
  }
}