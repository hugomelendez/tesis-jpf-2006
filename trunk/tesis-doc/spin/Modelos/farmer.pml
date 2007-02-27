/* farmer.pml - template solution to the farmer problem */

/* possible positions for an object */
#define LEFT	0
#define RIGHT	1

/* initial positions of the wolf, goat, cabbage, and farmer */
byte wolf	= LEFT;
byte goat	= LEFT;
byte cabbage	= LEFT;
byte farmer	= LEFT;

bool bad = false;

#define print_state	printf("farmer: %d wolf: %d goat: %d cabbage: %d bad: %d\n", farmer, wolf, goat, cabbage, bad)

#define move_right(var) {		\
	d_step {			\
		var = RIGHT;		\
		farmer = RIGHT;		\
		printf("move ");	\
		printf( #var );		\
		printf(" RIGHT\n");	\
		print_state		\
	}				\
}

#define move_left(var) {		\
	d_step {			\
		var = LEFT;		\
		farmer = LEFT;		\
		printf("move ");	\
		printf( #var );		\
		printf(" LEFT\n");	\
		print_state		\
	}				\
}


active proctype farmer_moves()
{
top:
	/* conditions to cause things to be eaten */
	if
	:: /* XXX fill these in. in all cases bad should be set to true */
	:: else -> skip
	fi;

	/* stop if something has been eaten */
	if
	:: (bad == true) -> goto top
	:: else -> skip
	fi;

	/* move items across the river */
	if
	/* XXX fill this in. move items right or left with 
	 * move_right or move_left.  Note that you can move the
         * farmer alone with move_right(farmer) or move_left(farmer). 
	 */
	:: else -> assert(false) /* should always be an available move */
	fi;

	goto top
}

/* insert the never claim here */
