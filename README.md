# Natural deduction
A system for [natural deduction](https://en.wikipedia.org/wiki/Natural_deduction)

## Status
[![Main Workflow](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml/badge.svg)](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dan323_natural-deduction&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=dan323_natural-deduction)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dan323_natural-deduction&metric=bugs)](https://sonarcloud.io/summary/new_code?id=dan323_natural-deduction)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dan323_natural-deduction&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dan323_natural-deduction)

## Logical language

The project is based on a common basic framework with plug-ins added to handle different logics. Right now, it can handle classical propositional logic
 and modal propositional logic with labelled states.
 
# Languages
## Classical
We use the language of [propositional calculus](https://en.wikipedia.org/wiki/Propositional_calculus) and rules
and classical rules for the natural deduction.

## Modal
We extend the language of propositions to [modal logic](https://en.wikipedia.org/wiki/Modal_logic). Then natural deduction is extended with labes.