mtype {MSG, ACK, HELLO, HOK, HERR};
                     
chan toS = [100] of {mtype, byte};
chan toR = [100] of {mtype, byte};

bit herr, comInit;
byte x, y;

proctype dador()
{

	toR!HELLO,1;

L0:	if
	:: toS ? HERR,x -> 
		herr = 1;
		goto L0;
	:: toS ? HOK,x ->
		comInit = 1;
		goto L1;
	fi;

goto fin;
L1:
	x = 0;
Ldo:
	do
	:: toR ! MSG, x ->
		toS ? ACK, y;
		if
			:: y == x -> x++;
		fi
	od;
fin: skip;
}

proctype rezibidor() {
	byte recvbit;
	toR ? HELLO, x;

	//toS ! HOK, x;

    do
    :: toR ? MSG, recvbit ->
        toS ! ACK, recvbit;
    od
}

init
{
    run dador();
    run rezibidor();
}
