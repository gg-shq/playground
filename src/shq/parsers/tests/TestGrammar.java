package shq.parsers.tests;

import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.grammar.Rule;
import shq.parsers.lr.builders.clr.CLR;
import shq.parsers.lr.builders.clr.ClrGoto;
import shq.parsers.lr.builders.clr.ClrGotoSet;
import shq.parsers.grammar.Grammar;
import shq.parsers.grammar.GrammarBuilder;
import shq.parsers.grammar.Grammars;
import shq.parsers.lr.builders.lalr.LALR;
import shq.parsers.lr.builders.lalr.LalrGoto;
import shq.parsers.lr.builders.lalr.LalrGotoSet;
import shq.parsers.lr.builders.slr.SLR;
import shq.parsers.lr.builders.slr.SlrGoto;
import shq.parsers.lr.builders.slr.SlrGotoSet;
import shq.parsers.lr.lrparser.LrParser;
import shq.parsers.lr.lrparser.LrParserTable;
import shq.parsers.lr.lrparser.LrParserTablePrinter;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class TestGrammar {

    public static List<String> RULES1 = Arrays.asList(
        "S:E",
        "E:E+T",
        "E:T",
        "T:(E)",
        "T:x"
    );

    private static String RULES1_TEST = "((x+x)+x)+x" + Grammars.END_MARK;

    public static List<String> RULES2 = Arrays.asList(
        "S:E",
        "E:E+T",
        "E:T",
        "T:T*F",
        "T:F",
        "F:(E)",
        "F:x"
    );

    private static String RULES2_TEST = "((x)*x*x*x+x)+x*x" + Grammars.END_MARK;

    public static List<String> RULES3 = Arrays.asList(
        "E:S",
        "S:L=R",
        "S:R",
        "L:*R",
        "L:x",
        "R:L"
    );
    private static String RULES3_TEST = "x=***x" + Grammars.END_MARK;

    public static List<String> RULES4 = Arrays.asList(
        "E:S",
        "S:CC",
        "C:cC",
        "C:d"
    );

    private static String RULES4_TEST = "cccdcd" + Grammars.END_MARK;

    public static List<String> RULES5 = Arrays.asList(
        "E:S",
        "S:aAd",
        "S:bBd",
        "S:aBe",
        "S:bAe",
        "A:c",
        "B:c"
    );

    private static String RULES5_TEST = "acd" + Grammars.END_MARK;

    // from GLR paper: Graph-structured Stack and Natural Language Parsing Masaru Tomlta
    public static List<String> RULES6 = Arrays.asList(
        "E:S",
        "S:NV",
        "S:SP",
        "N:n",
        "N:dn",
        "N:NP",
        "P:pN",
        "V:vN"
    );
    // I saw a man on the bed in the apartment with a telescope
    private static String RULES6_TEST = "nvdnpdnpdnpdn" + Grammars.END_MARK;

    public static List<String> RULES7 = Arrays.asList(
        "E:S",
        "S:U+U",
        "S:U-U",
        "U:T",
        "U:+U",
        "U:-U",
        "T:x"
    );
    // I saw a man on the bed in the apartment with a telescope
    private static String RULES7_TEST = "+-x+x" + Grammars.END_MARK;

    public static void main(String[] args) {
        try {
            List<String> rules = RULES7;
            String rulesTest = RULES7_TEST;

            Grammar g = GrammarBuilder.parse(rules);

            SlrGoto slrGoto = SLR.buildGotoTable(g);

            System.out.println("SLR sets:");
            for (SlrGotoSet s : slrGoto.sets())
                System.out.println(s.prettyPrint());

            ClrGoto clrGoto = CLR.buildGotoTable(g);

            System.out.println("CLR sets:");
            for (ClrGotoSet s : clrGoto.sets())
                System.out.println(s.prettyPrint());

            LalrGoto lalrGoto = LALR.buildGotoTable(g);

            System.out.println("LALR sets:");
            for (LalrGotoSet s : lalrGoto.sets())
                System.out.println(s.prettyPrint());

            LrParserTable slrTable = null;
            try {
                slrTable = SLR.buildLrParserTable(g, slrGoto);
            }
            catch (GrammarNotSupportedException e) {
                e.printStackTrace();
            }

            LrParserTable clrTable = null;
            try {
                clrTable = CLR.buildLrParserTable(g, clrGoto);
            }
            catch (GrammarNotSupportedException e) {
                e.printStackTrace();
            }

            LrParserTable lalrTable = null;
            try {
                lalrTable = LALR.buildLrParserTable(g, lalrGoto);
            }
            catch (GrammarNotSupportedException e) {
                e.printStackTrace();
            }

            System.out.println("\nRules:");
            for (Rule r : g.rules())
                System.out.println(r);
            System.out.println("");

            if (slrTable != null)
                System.out.println("SLR table:\n" + LrParserTablePrinter.prettyPrint(slrTable));
            if (clrTable != null)
                System.out.println("CLR table:\n" + LrParserTablePrinter.prettyPrint(clrTable));
            if (lalrTable != null)
                System.out.println("LALR table:\n" + LrParserTablePrinter.prettyPrint(lalrTable));

            LrParser parser;
            LrParserTracker tracker;
            if (slrTable != null) {
                tracker = new LrParserTracker();
                parser = new LrParser(slrTable, state -> {
                    System.out.println("Error: " + state);
                }, tracker);

                System.out.println("SLR Parsing:");
                System.out.println(parser.push(rulesTest));
            }

            if (clrTable != null) {
                tracker = new LrParserTracker();
                parser = new LrParser(clrTable, state -> {
                    System.out.println("Error: " + state);
                }, tracker);

                System.out.println("CLR Parsing:");
                System.out.println(parser.push(rulesTest));
            }

            if (lalrTable != null) {
                tracker = new LrParserTracker();
                parser = new LrParser(lalrTable, state -> {
                    System.out.println("Error: " + state);
                }, tracker);

                System.out.println("LALR Parsing:");
                System.out.println(parser.push(rulesTest));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class LrParserTracker implements BiConsumer<LrParser.LrParserState, LrParserTable.Action> {
        private int stepCount = 0;

        public LrParserTracker() {
        }

        public int stepCount() {
            return stepCount;
        }

        @Override public void accept(LrParser.LrParserState state, LrParserTable.Action action) {
            System.out.println("State: " + state + "; action: " + action);
            if (action instanceof LrParserTable.AcceptAction)
                System.out.println("Steps: " + stepCount);
            else
                ++stepCount;
        }
    }
}
