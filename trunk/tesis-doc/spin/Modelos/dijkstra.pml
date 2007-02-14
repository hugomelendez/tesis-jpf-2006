chan sema = [1] of {byte};
byte p=1,v;

active proctype dijkstra()
{
end:	do
	:: sema!p ->
accept:		sema?v;
	od;
progress:	skip;
}
