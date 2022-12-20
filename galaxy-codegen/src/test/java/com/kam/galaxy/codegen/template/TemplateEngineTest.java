package com.kam.galaxy.codegen.template;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
public class TemplateEngineTest {

    @ParameterizedTest
    @ValueSource(strings = {"Jedi Developer", ".123", "00.123", ""})
    public void templateEngineContentGenerationTest(String value){
        String content = TemplateEngine.INSTANCE.generate( Template.fromResources("templates/PrintTemplate.vm"), Map.of("value", value));
        Assertions.assertThat(content).isEqualTo(value);
    }

    @Test
    public void templateEngineNullContentGenerationTest(){
        String content = TemplateEngine.INSTANCE.generate( Template.fromResources("templates/PrintTemplate.vm"), Map.of());
        Assertions.assertThat(content).isEqualTo("null");
    }



}
