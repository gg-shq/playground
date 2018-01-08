package shq.etc;

import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.lr.builders.GotoSet;
import shq.parsers.lr.lrparser.LrParserTable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Util {

    public static <T extends Set, I> T modifiableSingletonSet(I item, Supplier<T> setSupplier) {
        T set = setSupplier.get();
        set.add(item);
        return set;
    }

    public static <K, V> void putToMapAndThrowIfNotSame(Map<K, V> map, K key, V newValue) {

        map.merge(key, newValue, (oldVal, newVal) -> {

            if (oldVal.equals(newVal))
                return oldVal;

            System.out.println("Conflicting values for key " + key + ":"
                + "\nOld: " + oldVal
                + "\nNew: " + newVal);

            throw new RuntimeException("Conflicting values: [key=" + key + "; oldVal=" + oldVal + "; newVal=" + newVal + "]");
        });
    }
}
