import com.dan323.expresions.modal.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class ModalTest {

    @Test
    public void modalToStringTest() {
        ConstantModal c = ConstantModal.FALSE;
        VariableModal v = new VariableModal("P");
        ModalLogicalExpression clo = new Sometime(new ImplicationModal(new DisjunctionModal(v, c), new ConjuntionModal(c, v)));

        Assertions.assertEquals(clo.toString(), "<> (((" + v + ") | (" + c + ")) -> ((" + c + ") & (" + v + ")))");
    }

    @Test
    public void modalToStringTest2() {
        ConstantModal c = ConstantModal.FALSE;
        VariableModal v = new VariableModal("P");
        ModalLogicalExpression clo = new Always(new ImplicationModal(new DisjunctionModal(v, c), new ConjuntionModal(c, v)));

        Assertions.assertEquals(clo.toString(), "[] (((" + v + ") | (" + c + ")) -> ((" + c + ") & (" + v + ")))");
    }

    @Test
    public void constantTest() {
        Assertions.assertTrue(ConstantModal.TRUE.getValue());
    }
}
