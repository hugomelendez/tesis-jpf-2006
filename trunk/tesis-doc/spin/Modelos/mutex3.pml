bit x, y;     /* signal entering/leaving the section  */
byte mutex;   /* # of procs in the critical section.  */

active proctype A() {
  x == 0;
  x = 1;
  mutex++;
  mutex--;
  x = 0;
}

active proctype B() {
    x==0;
    x = 1;
    mutex++;
    mutex--;
    x = 0;
}

/*
active proctype monitor() {
  assert(mutex != 2);
}
*/

init {
 accept2: (mutex != 2);
 accept3: ((x==1 && y==0) || (x==0 && y==1))
}
