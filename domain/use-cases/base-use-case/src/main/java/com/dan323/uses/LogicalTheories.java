package com.dan323.uses;

import com.dan323.model.TheoryDto;

import java.util.List;

/**
 * The theories of one logic: named premise sets, such as the group axioms, that a proof can start from.
 */
public interface LogicalTheories {

    String logic();

    List<TheoryDto> theories();
}
