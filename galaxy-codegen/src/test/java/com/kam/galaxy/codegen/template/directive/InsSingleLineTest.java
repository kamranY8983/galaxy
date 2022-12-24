package com.kam.galaxy.codegen.template.directive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class InsSingleLineTest {

    public static Stream<Arguments> inSingleLineArguments() {
        return Stream.of(
                Arguments.of("Test","Test"),
                Arguments.of("Test\n Value", "Test Value"),
                Arguments.of(".","."),
                Arguments.of("123","123"),
                Arguments.of("","")
        );
    }

    @ParameterizedTest
    @MethodSource("inSingleLineArguments")
    public void inSingleLineTest(String value, String expectedValue){
        InSingleLine inSingleLine = new InSingleLine();
        String generatedValue = inSingleLine.render(value);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
