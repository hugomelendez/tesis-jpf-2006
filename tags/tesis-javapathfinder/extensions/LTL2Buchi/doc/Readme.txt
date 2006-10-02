***********************************************************************
***********************************************************************

This file contains usage information for the LTL2Buchi translator, 
implemented at NASA Ames research Center, by 
Dimitra Giannakopoulou and Flavio Lerda.
For any information or to report problems, please contact:

Dimitra Giannakopoulou: dimitra@email.arc.nasa.gov

***********************************************************************
***********************************************************************

==============
1. LTL SYNTAX
==============

Operators:

/\ and
&& and
\/ or
|| or
-> implies
U until
<> eventually
[] always
! not

Propositions could be any Java string that does not contain these operators 
- if you want operators in your string just include your string within quotes:"", 
for example "a||b".

Make sure to use parentheses when a formula could have ambiguous meaning.


======================
2. Running LTL2Buchi
======================

The main file is at: gov/nasa/ltl/trans/LTL2Buchi.You may run the translator 
directly from the jar file LTL2Buchi.jar, just make sure it is in your classpath.

For example:

java -classpath .;LTL2Buchi.jar gov/nasa/ltl/trans/LTL2Buchi


Usage information is output whenever the command you enter is not correct,
or whenever you run "gov/nasa/ltl/trans/LTL2Buchi -usage"

Here is the usage information you obtain:


*******  USAGE *******
java gov.nasa.ltl.trans.LTL2Buchi <options>
        options can be (in any order):
                 "-f <filename>.ltl" (read formula from file)
                 "-a ltl2buchi|ltl2aut" (set algorithm to be used)
                 "-norw" (no rewriting)
                 "-nobisim" (no bisimulation reduction)
                 "-nofsim" (no fair simulation reduction)
                 "-o fsp|promela|xml" (format of output; default is fsp)


* If you do not provide a filename (must end in .ltl) from which the formula 
is to be read, the translator will prompt you to enter a formula from the 
command line:

"Insert LTL formula:"

* By default, the algorithm to be run is LTL2Buchi as presented in:

Giannakopoulou, D. and Lerda, F. "From States to Transitions: Improving 
translation of LTL formulae to Buchi automata", FORTE 2002, LNCS 2529. 

LTL2Aut is based on:

Daniele, M., Giunchiglia, F., and Vardi, M.Y. "Improved Automata Generation 
for Linear Temporal Logic", CAV 1999, LNCS 1633.


* You may choose among the following formats for the resulting Buchi 
automaton (option -o):

fsp: Input to the LTSA tool - the LTSA actually automatically detects the
	presence of LTL2Buchi.jar and interfaces with the translator 
	directly - you will not need to do that manually.
	
promela: a promela never claim for SPIN - remember to enter 
	the negated formula in the translator, i.e., if you want to
	check "formula" in SPIN, enter "! formula" in the translator.
	
xml: in XML


In addition to the final automaton, the translator also reports on 
rewriting and the number of states and transitions of the automaton
obtained at each intermediate phase of the translation.


======================
3. Example properties
======================

[]a  		(1 state and 1 transition)
[](a-><>b) 	(2 states 5 transitions)
[]<>a -> []<>b 	(5 states 11 transitions)
[] (a && b) 	(1 state 1 transition)
<>[](!a) 	(2 states 3 transitions)
!([](a-><>b)) 	(2 states 3 transitions)

