package com.kam.galaxy.codegen.template;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
public class TemplateTest {

    @Test
    public void loadFromResourceTest() {
        Template template = Template.fromResources("templates/Test.vm");
        Assertions.assertThat(template.name()).isEqualTo("templates/Test.vm");
        Assertions.assertThat(template.body()).isEqualTo("Test template Content!");
    }

}
