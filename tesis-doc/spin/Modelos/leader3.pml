/* Dolev, Klawe & Rodeh for leader election in unidirectional ring
 * `An O(n log n) unidirectional distributed algorithm for extrema
 * finding in a circle,'  J. of Algs, Vol 3. (1982), pp. 245-260
 */

#define N	5	/* nr of processes (use 5 for demos) */
#define I	3	/* node given the smallest number    */
#define L	10	/* size of buffer  (>= 2*N) */

mtype = { one, two, winner };
chan q[N] = [L] of { mtype, byte};

byte nr_leaders = 0;
byte gWinner;
byte terminados = 0;

proctype node (chan in, out; byte mynumber)
{	bit Active = 1, know_winner = 0;
	byte nr, maximum = mynumber, neighbourR;

	xr in;
	xs out;

	printf("MSC: %d\n", mynumber);
	out!one(mynumber);

	do
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
				out!one, nr;
			:: nr == mynumber ->
				/* Raynal p.39:  max is greatest number */
				assert(nr == N);
				out!winner, mynumber;

				// seteamos el ganador 
				gWinner = mynumber;

				printf("MSC: LEADER\n");
				nr_leaders++;
				assert(nr_leaders == 1);
				break;
			:: nr < mynumber ->
				out!one, mynumber;
			fi
		:: else ->
			out!one, nr;
		fi
	od;

	terminados++;
}


init {
	byte proc;
	atomic {
		proc = 1;
		do
		:: proc <= N ->
			/*
			asigna a cada nodo un nro del a sgte manera: 3,2,1,5,4
			a cada nodo le asigna dos canales, in y out, de manera q el proc i tiene como out lo q el proc i+1 tiene como in y asi arma el anillo unidirecional
			*/
			run node (q[proc-1], q[proc%N], (N+I-proc)%N+1);
			proc++
		:: proc > N ->
			break;
		od
	}
}
