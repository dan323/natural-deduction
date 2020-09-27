package com.dan323.expresions;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalParserTest {

    private final ModalLogicParser<String> parser = new ModalLogicParser<>(Function.identity());

    @Test
    public void parseBox() {
        ModalOperation m = parser.evaluate("[]P");
        assertTrue(m instanceof Always);
        assertEquals("P",((Always)m).getElement().toString());
    }

    @Test
    public void parserLess(){
        ModalOperation m = parser.evaluate("i <= j");
        assertTrue(m instanceof LessEqual);
        assertEquals("i", ((LessEqual<String>)m).getLeft());
        assertEquals("j", ((LessEqual<String>)m).getRight());
    }
}
