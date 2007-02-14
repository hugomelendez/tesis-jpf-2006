bool x;

proctype Thread1()
{
  do 
  :: x = !x; 
  od 
}

proctype Thread2()
{
  do 
  :: x = !x; 
  od; 
accept2: x=1;

}


init {
  x = false;
  run Thread1();
  run Thread2();
}
