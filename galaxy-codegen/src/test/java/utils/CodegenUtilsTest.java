package utils;

import com.kam.galaxy.codegen.utils.CodegenUtils;
import com.thoughtworks.qdox.parser.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 23-Dec-2022
 */
public class CodegenUtilsTest {

    public static Stream<Arguments> enchancedParseExceptionTest() {
        return Stream.of(
                Arguments.of("Line1\nLine2\nLine3",2,3),
                Arguments.of("Line1\nLine2\nLine3\nLine4\nLine5",4,2),
                Arguments.of("Line1\nLine2\nLine3\nLine4\nLine5",1,2),
                Arguments.of("Line1\nLine2\nLine3\nLine4\nLine5",5,3),
                Arguments.of("Line1\n \nLine3\n \nLine5",3,2)
        );
    }

    @ParameterizedTest
    @MethodSource("enchancedParseExceptionTest")
    public void test(String content, int lineNo, int column){
        String[] linesOfContent = content.split("\n");
        String lineAboveErrorLine = null;
        String belowLine = null;
        String errorLine = linesOfContent[lineNo - 1];
        if(lineNo > 1){
            lineAboveErrorLine = linesOfContent[lineNo - 2];
        }
        if(lineNo < linesOfContent.length){
            belowLine = linesOfContent.length == lineNo ? "" : linesOfContent[lineNo];
        }

        ParseException parseException = new ParseException("Parsing Exception", lineNo, column);
        ParseException enchancedParseException = CodegenUtils.enhancedParseException(content, parseException);
        String[] messageLines = enchancedParseException.getMessage().split("\n");
        int messageLineNo = 0;
        Assertions.assertThat(messageLines[messageLineNo++]).isEqualTo("Parsing Exception @[%s,%s] in ".formatted(lineNo, column));
        if(lineAboveErrorLine != null) Assertions.assertThat(messageLines[messageLineNo++]).contains(lineAboveErrorLine);
        Assertions.assertThat(messageLines[messageLineNo++]).contains(errorLine);
        Assertions.assertThat(messageLines[messageLineNo]).contains("^");
        Assertions.assertThat(messageLines[messageLineNo++].indexOf("^")).isEqualTo(column);
        Assertions.assertThat(messageLines[messageLineNo++].indexOf("|")).isEqualTo(column);
        Assertions.assertThat(messageLines[messageLineNo++]).isEqualTo("-".repeat(column + 1));
        if(belowLine != null) Assertions.assertThat(messageLines[messageLineNo++]).contains(belowLine);
    }
}
