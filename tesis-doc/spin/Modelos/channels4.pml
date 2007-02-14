mtype {MSG, ACK, HELLO, HOK, HERR};
                     
chan toS = [100] of {mtype, byte};
chan toR = [100] of {mtype, byte};

bit herr, comInit;
byte x, y;

proctype dador()
{
	toR!MSG,1;
}

proctype rezibidor() {
	if :: toR ?HELLO, x ->
		goto accepto;
	  :: toR ?MSG, x ->
		goto noacepto;
		skip;
	fi;
goto finproc;
accepto:skip;
noacepto:skip;
finproc:skip;
}

init
{
    run dador();
    run rezibidor();
}
