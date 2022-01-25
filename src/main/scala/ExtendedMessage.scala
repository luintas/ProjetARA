package paxos.Messages

import  ara.util.Message;
class Prepare (idsrc :Long, iddest : Long, pid : Int,val roundNum : Long) extends Message(idsrc,iddest,pid)
class Commit(idsrc :Long, iddest : Long, pid : Int,val choosedValue : Long, val roundNum : Long) extends Message(idsrc,iddest,pid)
//Should possibly change value if we're not sending numerical values
class Promises (idsrc :Long, iddest : Long, pid : Int, val roundNum : Long , val previousVal : Long, val previousRoundNum : Long) extends Message(idsrc,iddest,pid) 

class Accepted(idsrc :Long, iddest : Long, pid : Int,val choosedValue : Long, val roundNum : Long) extends Message(idsrc,iddest,pid)

class Reject (idsrc :Long, iddest : Long, pid : Int, val roundNum : Long ) extends Message(idsrc,iddest,pid) 
class StartMessage (idsrc :Long, iddest : Long, pid : Int, val roundNum : Long, val clientValue : Long ) extends Message(idsrc,iddest,pid) 