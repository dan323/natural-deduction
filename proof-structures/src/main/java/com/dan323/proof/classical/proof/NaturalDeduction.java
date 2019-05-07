package com.dan323.proof.classical.proof;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.clasical.*;
import com.dan323.proof.generic.Action;
import com.dan323.proof.Proof;
import com.dan323.proof.classical.*;
import com.dan323.proof.classical.complex.DeMorgan1;
import com.dan323.proof.classical.complex.OrE1;
import com.dan323.proof.classical.complex.OrE2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NaturalDeduction extends Proof {

    private List<ClassicalLogicOperation> goals;
    private List<ClassicalAction> actionsDone;
    private Map<Integer,Integer> usedForGoal;

    @Override
    public void setGoal(LogicOperation g){
        super.setGoal(g);
        goals=new ArrayList<>();
        actionsDone=new ArrayList<>();
        usedForGoal=new HashMap<>();
        goals.add((ClassicalLogicOperation)g);
    }

    @Override
    public boolean isValid(Action act) {
        return (act instanceof ClassicalAction);
    }

    public void automate(){
        boolean b=true;
        boolean c=true;
        while(c){
            while(b) {
                ClassicalAction intro=introRuleForGoal();
                if (intro!=null || (!getSteps().isEmpty() && goals.get(goals.size()-1).equals(getSteps().get(getSteps().size()-1).getStep()))){
                    updateGoals(intro);
                    if (goals.isEmpty()){
                        c=false;
                        b=false;
                    }
                    continue;
                }
                b=eliminateRules();
            }
            if (c){
                updateGoal();
            }
            b=true;
        }
    }

    private boolean eliminateRules() {
        ClassicalAction ca = lookForElimRules();
        if (ca != null) {
            ca.apply(this);
            return true;
        } else {
            return false;
        }
    }

    private void updateGoals(ClassicalAction intro) {
        if (intro!=null){
            intro.apply(this);
        }
        if (goals.size()>1 && goals.get(goals.size() - 1).equals(new ConstantClassic(false))) {
            ClassicalAction cla=new ClassicNotI();
            if (cla.isValid(this)){
                cla.apply(this);
                goals.remove(goals.size()-1);
            }
        }
        goals.remove(goals.size()-1);
        usedForGoal.remove(goals.size()+2);
    }

    private ClassicalAction introRuleForGoal(){
        if (getSteps().isEmpty()){
            return null;
        }
        ClassicalLogicOperation goal=goals.get(goals.size()-1);
        for(int i=0;i<getSteps().size();i++){
            if (getSteps().get(i).isValid() && goal.equals(getSteps().get(i).getStep()) && i+1<getSteps().size()){
                return new ClassicCopy(i+1);
            }
        }
        ClassicalAction sol=null;
        if (goal instanceof ConjuntionClassic){
            sol=introRuleForGoalConjuntion((BinaryOperationClassic)goal);
        }
        else if (goal instanceof ImplicationClassic){
            sol=introRuleForGoalImplication((BinaryOperationClassic)goal);
        }
        else if (goal instanceof DisjunctionClassic){
            sol=introRuleForGoalDisjunction((BinaryOperationClassic)goal);
        }
        else if (goal instanceof NegationClassic){
            sol=introRuleForGoalNegation((UnaryOperationClassic)goal);
        }
        else if (goal.equals(new ConstantClassic(false))){
            sol=introRuleForGoalContradiction();
        }
        return sol;
    }

    private ClassicalAction introRuleForGoalContradiction() {
        for(int i=0;i<getSteps().size();i++){
            if (getSteps().get(i).isValid() && getSteps().get(i).getStep() instanceof NegationClassic){
                ClassicalLogicOperation element= ((NegationClassic)getSteps().get(i).getStep()).getElement();
                for(int j=0;j<getSteps().size();j++){
                    if (getSteps().get(j).isValid() && getSteps().get(j).getStep().equals(element)){
                        return new ClassicFI(j+1,i+1);
                    }
                }
            }
        }
        return null;
    }

    private ClassicalAction introRuleForGoalNegation(UnaryOperationClassic goal) {
        ClassicalLogicOperation element=goal.getElement();
        int i=0;
        int assmsLevel=getSteps().get(getSteps().size()-1).getAssumptionLevel();
        while (getSteps().size()-1-i>=0 && getSteps().get(getSteps().size()-1-i).getAssumptionLevel()>=assmsLevel){
            i++;
        }
        if (getSteps().get(getSteps().size()-i).getStep().equals(element)) {
            for (int k = 0; k < getSteps().size(); k++) {
                if (getSteps().get(k).isValid() && getSteps().get(k).getStep().equals(new ConstantClassic(false))){
                    if (k + 1 < getSteps().size()) {
                        (new ClassicCopy(k+1)).apply(this);
                    }
                    return new ClassicNotI();
                }
            }
        }
        return null;
    }

    private ClassicalAction introRuleForGoalDisjunction(BinaryOperationClassic goal) {
        ClassicalLogicOperation left=(goal).getLeft();
        ClassicalLogicOperation right=(goal).getRight();
        for (int k = 0; k < getSteps().size(); k++) {
            if (getSteps().get(k).isValid()) {
                if (getSteps().get(k).equals(left)){
                    return new ClassicOrI1(k+1,right);
                }
                if (getSteps().get(k).equals(right)){
                    return new ClassicOrI2(k+1,left);
                }
            }
        }
        return null;
    }

    private ClassicalAction introRuleForGoalConjuntion(BinaryOperationClassic goal) {
        ClassicalLogicOperation left=(goal).getLeft();
        int a=-1;
        ClassicalLogicOperation right=(goal).getRight();
        int b=-1;
        for(int i=0;i<getSteps().size();i++){
            if (getSteps().get(i).isValid()){
                if (left.equals(getSteps().get(i).getStep())){
                    a=i+1;
                }
                if (right.equals(getSteps().get(i).getStep())){
                    b=i+1;
                }
            }
            if (a>0&&b>0){
                return new ClassicAndI(a,b);
            }
        }
        return null;
    }

    private ClassicalAction introRuleForGoalImplication(BinaryOperationClassic goal) {
        ClassicalLogicOperation left=(goal).getLeft();
        ClassicalLogicOperation right=(goal).getRight();
        int i=0;
        int assmsLevel=getSteps().get(getSteps().size()-1).getAssumptionLevel();
        while (getSteps().size()-1-i>=0 && getSteps().get(getSteps().size()-1-i).getAssumptionLevel()>=assmsLevel){
            i++;
        }
        if (getSteps().get(getSteps().size()-i).getStep().equals(left)) {
            for (int k = 0; k < getSteps().size(); k++) {
                if (getSteps().get(k).isValid() && getSteps().get(k).getStep().equals(right)){
                    if (k + 1 < getSteps().size() && !getSteps().get(getSteps().size()-1).getStep().equals(getSteps().get(k).getStep())) {
                        (new ClassicCopy(k+1)).apply(this);
                    }
                    return new ClassicDeductionTheorem();
                }
            }
        }
        return null;
    }

    private ClassicalAction lookForElimRules(){
        for(int i=0;i<getSteps().size();i++){
            if (getSteps().get(i).isValid()){
                ClassicalAction act=new DeMorgan1(i+1);
                act=checkSingleAction(act);
                if (act!=null){
                    return act;
                }
                act=new ClassicNotE(i+1);
                act=checkSingleAction(act);
                if (act!=null){
                    return act;
                }
                act=checkAdditionOfAndI(i);
                if (act!=null){
                    return act;
                }
                act=checkAdditionOfDisjIModPonens(i);
                if (act!=null){
                    return act;
                }
            }
        }
        return null;
    }

    private ClassicalAction checkAdditionOfAndI(int i) {
        ClassicalAction act=new ClassicAndE1(i+1);
        act=checkSingleAction(act);
        if (act!=null){
            return act;
        }
        act=new ClassicAndE2(i+1);
        act=checkSingleAction(act);
        if (act!=null){
            return act;
        }
        return null;
    }

    private ClassicalAction checkSingleAction(ClassicalAction act){
        if (!actionsDone.contains(act) && act.isValid(this)){
            actionsDone.add(act);
            return act;
        }
        return null;
    }

    private ClassicalAction checkAdditionOfDisjIModPonens(int i) {
        for(int j=0;j<getSteps().size();j++){
            if (getSteps().get(j).isValid()){
                ClassicalAction act=new ClassicModusPonens(i+1,j+1);
                act=checkSingleAction(act);
                if (act!=null){
                    return act;
                }
                act=new OrE1(i+1,j+1);
                act=checkSingleAction(act);
                if (act!=null){
                    return act;
                }
                act=new OrE2(i+1,j+1);
                act=checkSingleAction(act);
                if (act!=null){
                    return act;
                }
            }
        }
        return null;
    }

    private void updateGoal(){
        if (goals.get(goals.size()-1).equals(new ConstantClassic(false))){
            lastGoalFalse();
        }
        else{
            ClassicalLogicOperation goal=goals.get(goals.size()-1);
            if (goal instanceof ConjuntionClassic){
                updateGoalConjuntion((BinaryOperationClassic)goal);
            }
            else if (goal instanceof ImplicationClassic){
                updateGoalImplication((BinaryOperationClassic)goal);
            }
            else if (goal instanceof NegationClassic){
                updateGoalNegation((UnaryOperationClassic)goal);
            }
            else{
                updateGoalContradiction(goal);
            }
        }
    }

    private void lastGoalFalse() {
        int j=-1;
        for(int i=0;i<getSteps().size();i++){
            boolean b=true;
            if (!usedForGoal.containsValue(i) && getSteps().get(i).isValid()){
                LogicOperation log=getSteps().get(i).getStep();
                if (log instanceof NegationClassic){
                    goals.add(((UnaryOperationClassic)log).getElement());
                }
                else if (log instanceof DisjunctionClassic){
                    goals.add(new NegationClassic(((BinaryOperationClassic)log).getLeft()));
                }
                else if (log instanceof ImplicationClassic){
                    goals.add(((BinaryOperationClassic)log).getLeft());
                }
                else{
                    b=false;
                }
                if (b){
                    j=i;
                    break;
                }
            }
        }
        usedForGoal.put(goals.size(),j);
    }

    private void updateGoalContradiction(ClassicalLogicOperation goal) {
        goals.add(new NegationClassic(new NegationClassic(goal)));
        goals.add(new ConstantClassic(false));
        (new ClassicAssume(new NegationClassic(goal))).apply(this);
    }

    private void updateGoalNegation(UnaryOperationClassic goal) {
        goals.add(new ConstantClassic(false));
        (new ClassicAssume((goal).getElement())).apply(this);
    }

    private void updateGoalImplication(BinaryOperationClassic goal) {
        goals.add((goal).getRight());
        (new ClassicAssume((goal).getLeft())).apply(this);
    }

    private void updateGoalConjuntion(BinaryOperationClassic goal) {
        goals.add((goal).getLeft());
        goals.add((goal).getRight());
    }
}
