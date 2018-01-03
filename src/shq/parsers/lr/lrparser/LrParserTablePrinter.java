package shq.parsers.lr.lrparser;

import dnl.utils.text.table.TextTable;
import shq.parsers.grammar.Grammar;
import shq.parsers.grammar.Grammars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LrParserTablePrinter {

    public static String prettyPrint(LrParserTable table) throws IOException {

        StringBuffer sb = new StringBuffer("States: ");
        sb.append(table.states().size()).append("\n");

        Grammar grammar = table.grammar();

        int colCount = grammar.terminals().size() + grammar.nonterminals().size() + 2;

        String[] colNames = new String[colCount];

        int col = 0;

        colNames[col++] = "STATE";

        for (char t : grammar.terminals()) {
            colNames[col++] = Grammars.prettySymbol(t);
        }

        colNames[col++] = Character.toString(Grammars.END_MARK);

        for (char nt : grammar.nonterminals()) {
            colNames[col++] = Grammars.prettySymbol(nt);
        }

        Set<Character> terminalsPlusEndMark = new LinkedHashSet<>(grammar.terminals());
        terminalsPlusEndMark.add(Grammars.END_MARK);

        Object[][] tableData = new Object[table.actions().size()][colCount];

        int row = 0;

        for (String state : table.actions().keySet()) {
            col = 0;

            tableData[row][col++] = state;

            Map<Character, LrParserTable.Action> actions = table.actions().get(state);

            for (char t : terminalsPlusEndMark) {
                LrParserTable.Action action = actions.get(t);
                String val = (action == null) ? "" : action.toString();
                tableData[row][col++] = val;
            }

            for (char nt : grammar.nonterminals()) {
                LrParserTable.Action action = actions.get(nt);
                String val = (action == null) ? "" : action.toString();
                tableData[row][col++] = val;
            }

            row++;
        }

        TextTable tt = new TextTable(colNames, tableData);

        try (ByteArrayOutputStream bufStream = new ByteArrayOutputStream()) {
            tt.printTable(new PrintStream(bufStream), 0);
            sb.append(bufStream.toString());
        }

        return sb.toString();
    }
}
