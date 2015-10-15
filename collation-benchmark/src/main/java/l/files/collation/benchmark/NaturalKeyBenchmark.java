package l.files.collation.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import l.files.collation.NaturalKey;

public class NaturalKeyBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        final com.ibm.icu.text.Collator icuCollator;
        final java.text.Collator jdkCollator;
        final List<java.text.CollationKey> jdkCollationKeys;
        final List<NaturalKey> naturalKeys;
        final List<String> strings;

        public BenchmarkState() {
            icuCollator = NaturalKey.collator(Locale.getDefault());
            jdkCollator = java.text.Collator.getInstance();
            final int n = 10000;
            strings = new ArrayList<>(n);
            jdkCollationKeys = new ArrayList<>(n);
            naturalKeys = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                final String string = i + ". " + i;
                strings.add(string);
                jdkCollationKeys.add(jdkCollator.getCollationKey(string));
                naturalKeys.add(NaturalKey.create(icuCollator, string));
            }
        }
    }

    @Benchmark
    public void sortByNaturalKey(BenchmarkState state) {
        Collections.sort(state.naturalKeys);
    }

    @Benchmark
    public void sortByJdkCollationKey(BenchmarkState state) {
        Collections.sort(state.jdkCollationKeys);
    }

    @Benchmark
    public void makeNaturalKeys(BenchmarkState state) {
        for (String string : state.strings) {
            NaturalKey.create(state.icuCollator, string);
        }
    }

    @Benchmark
    public void makeJdkCollationKeys(BenchmarkState state) {
        for (String string : state.strings) {
            state.jdkCollator.getCollationKey(string);
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(NaturalKeyBenchmark.class.getName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build();
        new Runner(options).run();
    }

}
