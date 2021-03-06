package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

trait ProposerProtocol {
  // val tmp = prefix.split("\\.")
  // val pid_transport: Int =
  // Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);
//   var maxsizelist: Int =
  // Configuration.getInt(prefix + "." + HelloProtocol.PAR_MAXSIZELIST);
  private var mylist: List[Integer] = List[Integer]();
  private var proposerCurrentRoundNum: Long = 0;
  private var amLeader = false;
  private var haveAleader = false;
  private var idLeader: Long = 0;
  private var TimetoWait: Long = 10;
  private val acceptorsCount: Int =
    Network.size; //TODO Find a way to get the actual value
  private var promiseReceivedCount = 0;
  private var biggestValueReceived: (Long, Long) = (0, 0) // (nbRound,value)
  private var nbAckReceived: Int = 0;
  private var nbRejectReceived: Int = 0;
  var leaderIDAndRoundNum : (Long,Long) = (-255,-255); //Acknowledged leaderID


  def broadcast(
      host: Node,
      pid: Int,
      sendFunction: (Node, Node, Transport, Int) => Unit
  ) {
    val tr: Transport = host.getProtocol(pid).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr, pid)
    }
  }
  def receiveStartMessage(host: Node, pid: Int, mess: StartMessage) {
    //TODO : Modify this part so it act in accordance to the Leader definition
    biggestValueReceived = (
      proposerCurrentRoundNum,
      mess.clientValue
    ) // HELP : If there's an error check if the round number is right
    broadcast(host, pid, sendPrepare)
  }
  def receivePromise(host: Node, pid: Int, mess: Messages.Promises) {
    promiseReceivedCount += 1
    if (mess.roundNum > biggestValueReceived._1) {
      biggestValueReceived = (mess.roundNum, mess.previousVal)
    }
    if (promiseReceivedCount > acceptorsCount / 2) {
      broadcast(host, pid, sendCommit)
    }
  }
  def receiveReject(host: Node, pid: Int, mess: Messages.Reject) {
    if (mess.roundNum >= proposerCurrentRoundNum) {
      proposerCurrentRoundNum = proposerCurrentRoundNum + 1;
      Thread.sleep(TimetoWait)
      TimetoWait += 10;
      broadcast(host, pid, sendPrepare)
    }
  }
  def sendPrepare(host: Node, dest: Node, tr: Transport, pid: Int) {
    val mess: Prepare =
      new Prepare(host.getID(), dest.getID(), pid, proposerCurrentRoundNum)
    tr.send(host, dest, mess, pid)
  }
  def sendCommit(host: Node, dest: Node, tr: Transport, pid: Int) {
    val mess: Commit =
      new Commit(
        host.getID(),
        dest.getID(),
        pid,
        biggestValueReceived._2,
        proposerCurrentRoundNum
      )
    tr.send(host, dest, mess, pid)
  }
  def findLeader(host: Node, pid: Int, tr: Transport) {
    leaderIDAndRoundNum = (proposerCurrentRoundNum,leaderIDAndRoundNum._2)
    if( ! haveAleader)
      broadcast(host, pid, sendCandidate)
  }
  def sendCandidate(host: Node, dest: Node, tr: Transport, pid: Int) {
    val mess: Candidate =
      new Candidate(
        host.getID(),
        dest.getID(),
        pid,
        proposerCurrentRoundNum
      )
    tr.send(host, dest, mess, pid)
  }
  def receiveCandidate(host: Node, mess: Messages.Candidate,pid : Int, tr : Transport) {
    if(mess.roundNum > leaderIDAndRoundNum._2){
      nbAckReceived = 0;
      proposerCurrentRoundNum = mess.roundNum
    }
    if(mess.roundNum > leaderIDAndRoundNum._2 || (mess.roundNum == leaderIDAndRoundNum._2 && leaderIDAndRoundNum._1 < 0 )  ){
      // println(host.getID  + " : idsrc is "+mess.idsrc + " iddest is "+mess.iddest)
      leaderIDAndRoundNum = (mess.idsrc,mess.roundNum)
      sendAck(host,Network.get(mess.idsrc.asInstanceOf[Int]),tr,pid)
    }
    else
      sendRejectCandidate(host,Network.get(mess.idsrc.asInstanceOf[Int]),tr,pid)
  }
  def sendRejectCandidate(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: RejectCandidate =
      new RejectCandidate(host.getID(), dest.getID(), pid,proposerCurrentRoundNum)
    tr.send(host, dest, mess, pid)
  }
  def sendAck(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: Ack =
      new Ack(host.getID(), dest.getID(), pid,proposerCurrentRoundNum)
    tr.send(host, dest, mess, pid)
  }
  def receiveAck(host: Node, mess: Messages.Ack, pid: Int, tr: Transport) {
    if (mess.roundNum >= proposerCurrentRoundNum){
      if (! haveAleader ) {
        nbAckReceived = nbAckReceived + 1
        // if(host.getID() == 3l) println("tot = "+(nbAckReceived + nbRejectReceived))
        if ((nbAckReceived == (acceptorsCount / 2)+1)) {
          broadcast(host, pid, sendIamLeader)
        }
        if(nbRejectReceived + nbAckReceived == acceptorsCount){
          proposerCurrentRoundNum +=1;
          nbAckReceived = 0;
          nbRejectReceived = 0
          findLeader(host,pid,tr)
        }
      }
    }
  }
  def receiveRejectCandidate(host: Node, mess: Messages.RejectCandidate, pid: Int, tr: Transport) {
    if (mess.roundNum >= proposerCurrentRoundNum ) {
      nbRejectReceived = nbRejectReceived + 1
      // if(host.getID() == 3l) println("tot = "+(nbAckReceived + nbRejectReceived))
      if(nbRejectReceived + nbAckReceived == acceptorsCount){
        proposerCurrentRoundNum +=1;
        nbAckReceived = 0;
        nbRejectReceived = 0
        findLeader(host,pid,tr)
      }
    }
  }
  def sendIamLeader(host: Node, dest: Node, tr: Transport, pid: Int){
    val mess: IamLeader =
      new IamLeader(
        host.getID(),
        dest.getID(),
        pid,
        proposerCurrentRoundNum
      )
    tr.send(host, dest, mess, pid)
  }
  def receiveIamLeader(host: Node, mess: Messages.IamLeader, pid: Int, tr: Transport){
    amLeader = host.getID() == mess.idsrc
    idLeader = mess.idsrc
    val str = host.getID +"/ " + proposerCurrentRoundNum+ " : " + ( if(amLeader)("I am the Leader ") else ("The leader is "+ idLeader))
    if(amLeader)  println(str)
    haveAleader = true;
  }
  def receivePing(host: Node, mess: Messages.Ping, pid: Int, tr: Transport) {
    val dest: Node = Network.get(mess.idsrc.asInstanceOf[Int]);
    val message: Pong = new Pong(
      host.getID(),
      dest.getID(),
      pid
    )
    tr.send(host, dest, message, pid)
  }

  def receivePong(host: Node, mess: Messages.Pong, pid: Int, tr: Transport) {
    val dest: Node = Network.get(mess.idsrc.asInstanceOf[Int]);
    val message: Ping = new Ping(
      host.getID(),
      dest.getID(),
      pid
    )
    tr.send(host, dest, message, pid)
  }

}
object ProposerProtocol {
  val PAR_TRANSPORT = "transport";
  val PAR_MAXSIZELIST = "maxsizelist";
}
