bit flag1;
byte mutex;

proctype P (bit i) {
	flag1 != 1;
	flag1 = 1;
	mutex++;
	printf("p (%d) entro !", i);
	mutex--;
	flag1 = 0;
}

proctype Monitor() {
	assert(mutex!=2);
	accept1:flag1=1;
}

init {
	atomic {
		run Monitor();
		run P(0);
		run P(1);
	}
}
