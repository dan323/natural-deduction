import com.dan323.expresions.clasical.*;
import com.dan323.expresions.exceptions.InvalidMapValuesException;
import com.dan323.expresions.modal.*;
import com.dan323.expresions.util.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author danco
 */
public class ClassicalTest {

    @Test
    public void classicalToStringTest() {
        ConstantClassic c = new ConstantClassic(true);
        VariableClassic v = new VariableClassic("P");
        ClassicalLogicOperation clo = new NegationClassic(new ConjuntionClassic(v, c));

        Assertions.assertEquals(clo.toString(), "- (" + v + " & " + c + ")");
    }

    @Test
    public void modalToStringTest(){
        ConstantModal c=new ConstantModal(false);
        VariableModal v=new VariableModal("P");
        ModalLogicalExpression clo= new Sometime(new ImplicationModal(new DisjunctionModal(v,c),new ConjuntionModal(c,v)));

        Assertions.assertEquals(clo.toString(),"<> ((("+v+") | ("+c+")) -> (("+c+") & ("+v+")))");
    }

    @Test
    public void classicalEvaluationTest() {
        ConstantClassic c = new ConstantClassic(true);
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("Q");
        ClassicalLogicOperation clo = new DisjunctionClassic(new NegationClassic(new ConjuntionClassic(v, w)), c);

        Map<String, Boolean> values = new HashMap<>();
        values.put("P",true);
        values.put("Q",false);
        Assertions.assertTrue(clo.evaluate(values));
        Assertions.assertFalse((new ImplicationClassic(clo,new NegationClassic(clo))).evaluate(values));
    }

    @Test
    public void insuficientValuesMap(){
        ConstantClassic c = new ConstantClassic(true);
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("Q");
        ClassicalLogicOperation clo = new ConjuntionClassic(new NegationClassic(new ConjuntionClassic(v, w)), c);

        Map<String, Boolean> values = new HashMap<>();
        values.put("Q",false);
        Assertions.assertThrows(InvalidMapValuesException.class,()->clo.evaluate(values));
    }

}
