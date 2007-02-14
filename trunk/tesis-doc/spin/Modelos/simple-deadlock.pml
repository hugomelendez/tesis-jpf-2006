byte x,y;

proctype Thread1()
{
 if 
 :: (x == 0) 
     -> {x++;
         if :: (y == 0)              
                -> y++; y--
         fi}
         x--;
 fi
}

proctype Thread2()
{
 if 
 :: (y == 0) 
     -> {y++;
         if :: (x == 0)              
                -> x++; x--
         fi}
         y--;
 fi
}

init {
  run Thread1();
  run Thread2();
}
