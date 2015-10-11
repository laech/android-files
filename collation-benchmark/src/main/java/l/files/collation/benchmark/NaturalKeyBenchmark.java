package l.files.collation.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.collation.NaturalKey;

public class NaturalKeyBenchmark {

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    final Collator collator;
    final List<CollationKey> collationKeys;
    final List<NaturalKey> naturalKeys;
    final List<String> strings;

    public BenchmarkState() {
      collator = Collator.getInstance();
      final int n = 10000;
      strings = new ArrayList<>(n);
      collationKeys = new ArrayList<>(n);
      naturalKeys = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        final String string = i + ". " + i;
        strings.add(string);
        collationKeys.add(collator.getCollationKey(string));
        naturalKeys.add(NaturalKey.create(collator, string));
      }
    }
  }

  @Benchmark
  public void naturalKey(BenchmarkState state) {
    Collections.sort(state.naturalKeys);
  }

  @Benchmark
  public void collationKey(BenchmarkState state) {
    Collections.sort(state.collationKeys);
  }

  @Benchmark
  public void makeNaturalKeys(BenchmarkState state) {
    for (String string : state.strings) {
      NaturalKey.create(state.collator, string);
    }
  }

  @Benchmark
  public void makeCollationKeys(BenchmarkState state) {
    for (String string : state.strings) {
      state.collator.getCollationKey(string);
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
