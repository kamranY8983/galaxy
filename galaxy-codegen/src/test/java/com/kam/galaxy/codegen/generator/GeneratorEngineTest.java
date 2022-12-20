package com.kam.galaxy.codegen.generator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
public class GeneratorEngineTest {

    static Stream<Arguments> positiveArgumentsProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"--base-dir", "C:/"}),
                Arguments.of((Object) new String[]{"--source-base-dir", "C:/source"}),
                Arguments.of((Object) new String[]{"--test-base-dir", "C:/tests"}),
                Arguments.of((Object) new String[]{"--base-dir", "C:/", "--source-base-dir", "C:/source", "--test-base-dir", "C:/tests"})
        );
    }

    static Stream<Arguments> negativeArgumentsProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"--base-dir"}),
                Arguments.of((Object) new String[]{"--source-base-dir"}),
                Arguments.of((Object) new String[]{"--test-base-dir"})
        );
    }

    @ParameterizedTest
    @MethodSource("positiveArgumentsProvider")
    public void commandlineArgumentPositiveTest(String[] args) {
        GeneratorEngine generatorEngine = Mockito.mock(GeneratorEngine.class, Mockito.CALLS_REAL_METHODS);
        int statusCode = generatorEngine.generate(args);
        Assertions.assertThat(statusCode).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("negativeArgumentsProvider")
    public void commandlineArgumentNegativeTest(String[] args) {
        GeneratorEngine generatorEngine = Mockito.mock(GeneratorEngine.class, Mockito.CALLS_REAL_METHODS);
        int statusCode = generatorEngine.generate(args);
        Assertions.assertThat(statusCode).isNotEqualTo(0);
    }
}
