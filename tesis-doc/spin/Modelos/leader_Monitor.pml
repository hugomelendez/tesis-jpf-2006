/* Dolev, Klawe & Rodeh for leader election in unidirectional ring
 * `An O(n log n) unidirectional distributed algorithm for extrema
 * finding in a circle,'  J. of Algs, Vol 3. (1982), pp. 245-260
 */

#define N	5	/* nr of processes (use 5 for demos) */
#define I	3	/* node given the smallest number    */
#define L	10	/* size of buffer  (>= 2*N) */

#define ie InicioEjecucion==1
#define nw MensajeNW==1
#define w MensajeW==1

#define states(newIE,newNW,newW) atomic{ InicioEjecucion = newIE; MensajeNW = newNW; MensajeW = newW;}
#define clearstates() states(0,0,0)

bit InicioEjecucion;
bit MensajeNW;
bit MensajeW;


mtype = { one, two, winner };
chan q[N] = [L] of { mtype, byte};

byte gWinner;

proctype node (chan in, out; byte mynumber)
{	bit Active = 1, know_winner = 0;
	byte nr, maximum = mynumber, neighbourR;

	xr in;
	xs out;

	printf("MSC: %d\n", mynumber);
	out!one(mynumber);
skip;
end:	do
	:: in?winner, nr ->
		states(0,0,1);
		out!winner, nr;
		break;
	:: in?one(nr) ->
		if
		:: Active ->
			if 
			:: nr > mynumber ->
				printf("MSC: LOST\n");
				Active = 0;
				states(0,1,0);
				out!one, nr;
			:: nr == mynumber ->
				assert(nr == N);
				states(0,0,1);
				out!winner, mynumber;
				printf("MSC: LEADER\n");
				break;
			:: nr < mynumber ->
				states(0,1,0);
				out!one, mynumber;
			fi
		:: else ->
			states(0,1,0);
			out!one, nr;
		fi
	od
}

active proctype monitor (){
T0_init:
    if
    :: ie -> goto L_NW_INICIADO
    :: goto T0_init
    fi;

L_NW_INICIADO:
    if
    :: (w) -> goto trampa
    :: (nw) -> goto L_NW1
    :: goto L_NW_INICIADO
    fi;
L_NW1:
    if
    :: (w) -> goto accept_all
    :: goto L_NW1
    fi;

trampa:
    do
    :: true -> skip
    od;

L_WIN:
accept_all:
    skip;
}

init {
	byte proc;
	states(1,0,0);
	atomic {
		proc = 1;
		do
		:: proc <= N ->
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++
		:: proc > N ->
			break
		od
	}
}
