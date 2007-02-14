chan a = [0] of { short };
short x;

active proctype S() {
    do::
	a!3;
    od;
}

active proctype R() {
    do::
	a?3;
    od;
}

