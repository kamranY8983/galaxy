package com.kam.galaxy.codegen.template.directive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class PrintClassNameTest {

    public static Stream<Arguments> printClassNameArgumentProvider() {
        return Stream.of(
                Arguments.of(String.class, "String"),
                Arguments.of(Integer.class, "Integer"),
                Arguments.of(Map.class, "java.util.Map"),
                Arguments.of(List.class, "java.util.List"),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("printClassNameArgumentProvider")
    public void printClassNameTest(Class value, String expectedValue){
        PrintClassName printClassName = new PrintClassName();
        String generatedValue = printClassName.render(value);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
