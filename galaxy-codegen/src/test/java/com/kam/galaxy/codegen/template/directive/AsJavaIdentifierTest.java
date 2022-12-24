package com.kam.galaxy.codegen.template.directive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class AsJavaIdentifierTest {

    @ParameterizedTest
    @CsvSource({"Test,Test", "1test,_1test", "., _", "123,_123", "00.123,_00123",","})
    public void asJavaIdentifierTest(String value, String expectedValue){
        AsJavaIdentifier asJavaIdentifier = new AsJavaIdentifier();
        String generatedValue = asJavaIdentifier.render(value);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
