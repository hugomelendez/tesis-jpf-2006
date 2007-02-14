mtype = { ack, nak, err, next, accept };

proctype transfer(chan in,out,chin,chout)
{	byte o, i;

	in?next(o);

	do
	:: chin?nak(i) ->
			out!accept(i);
			chout!ack(o)

	:: chin?ack(i) ->
			out!accept(i);
			in?next(o);
			chout!ack(o)

	:: chin?err(i) ->
			chout!nak(o)
	od
}

init
{	chan AtoB = [1] of { mtype, byte };
	chan BtoA = [1] of { mtype, byte };

	chan Ain  = [2] of { mtype, byte };
	chan Bin  = [2] of { mtype, byte };

	chan Aout = [2] of { mtype, byte };
	chan Bout = [2] of { mtype, byte };

	atomic {
	  run transfer(Ain,Aout, AtoB,BtoA);
	  run transfer(Bin,Bout, BtoA,AtoB)
	};

	AtoB!err(0)
}
