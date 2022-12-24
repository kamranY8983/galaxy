package com.kam.galaxy.codegen.template.directive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class DecapitalizeTest {

    @ParameterizedTest
    @CsvSource({"Test,test", "1test,1test", ".,.", "123,123", ","})
    public void decapitalizeTest(String value, String expectedValue){
        Decapitalize decapitalize = new Decapitalize();
        String generatedValue = decapitalize.render(value);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
