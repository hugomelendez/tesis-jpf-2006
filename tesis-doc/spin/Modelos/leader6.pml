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

mtype = { one, two, winner };
chan q[N] = [L] of { mtype, byte};

bit InicioEjecucion;
bit MensajeNW;
bit MensajeW;


proctype node (chan in, out; byte mynumber)
{	bit Active = 1, know_winner = 0;
	byte nr, maximum = mynumber, neighbourR;

	xr in;
	xs out;

	printf("MSC: %d\n", mynumber);
	out!one(mynumber);

end:	
	do
	// esta guarda toma el msg winner y lo fwd al resto del ring
	:: in?winner, nr ->
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
			out!one, nr;
		fi
	od
}

init {
	byte proc;
	states(1,0,0);

	atomic {
		proc = 1;

			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++;
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++;
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++;
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++;
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
	}
}
