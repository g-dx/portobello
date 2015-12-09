package com.gdx.portobello;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

public class JsonParserBenchmark
{
    @State(Scope.Thread)
    public static class Json
    {
        @Param({ })
        public String json;
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void benchmark()
    {
        // Parser benchmark
    }

    public static void main(String[] args) throws IOException, RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(JsonParserBenchmark.class.getSimpleName())
            .verbosity(VerboseMode.EXTRA)
            .forks(2)
            .resultFormat(ResultFormatType.TEXT)
            .verbosity(VerboseMode.EXTRA)
            .measurementIterations(5)
            .operationsPerInvocation(1)
            .warmupForks(1)
            .warmupIterations(5)
//            .addProfiler(StackProfiler.class)
            .build();

        new Runner(opt).run();
    }
}
