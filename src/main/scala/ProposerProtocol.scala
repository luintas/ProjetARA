package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

trait ProposerProtocol  {
  // val tmp = prefix.split("\\.")
  // val pid_transport: Int =
    // Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);
//   var maxsizelist: Int =
  // Configuration.getInt(prefix + "." + HelloProtocol.PAR_MAXSIZELIST);
  private var mylist: List[Integer] = List[Integer]();
  private var ProposerCurrentRoundNum: Int = 0;
  private var isLeader = false ;
  private var haveAleader = false;
  private var idMaster:Long = 0;
  private var TimetoWait : Long = 10;
  private val acceptorsCount = 0; //TODO Find a way to get the actual value
  private var promiseReceivedCount = 0;
  private var biggestValueReceived: (Long, Long) = (0, 0) // (nbRound,value)
  
  def broadcast(host: Node, pid : Int, sendFunction: (Node, Node, Transport, Int) => Unit) {
    val tr: Transport = host.getProtocol(pid).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr, pid)
    }
  }
  def receiveStartMessage(host: Node, pid : Int, mess: StartMessage) {
    //TODO : Modify this part so it act in accordance to the Leader definition
    biggestValueReceived =
      (
        ProposerCurrentRoundNum,
        mess.clientValue
      ) // HELP : If there's an error check if the round number is right
    broadcast(host, pid, sendPrepare)
  }
  def receivePromise(host: Node , pid : Int, mess: Messages.Promises) {
    promiseReceivedCount += 1
    if (mess.roundNum > biggestValueReceived._1) {
      biggestValueReceived = (mess.roundNum, mess.previousVal)
    }
    if (promiseReceivedCount > acceptorsCount / 2) {
      broadcast(host, pid, sendCommit)
    }
  }
  def receiveReject(host : Node, pid : Int, mess : Messages.Reject) {
    if(mess.roundNum >= ProposerCurrentRoundNum){
      ProposerCurrentRoundNum = ProposerCurrentRoundNum +1;
      Thread.sleep(TimetoWait)
      TimetoWait += 10;
      broadcast(host, pid,sendPrepare)
    }
  }
  def sendPrepare(host: Node, dest: Node, tr: Transport , pid : Int) {
    val mess: Prepare =
      new Prepare(host.getID(), dest.getID(), pid, ProposerCurrentRoundNum)
    tr.send(host, dest, mess, pid)
  }
  def sendCommit(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: Commit =
      new Commit(
        host.getID(),
        dest.getID(),
        pid,
        biggestValueReceived._2,
        ProposerCurrentRoundNum
      )
    tr.send(host, dest, mess, pid)
  }
  def findLeader(host : Node, dest: Node, tr: Transport) {
    
  }

  def receivePing(host: Node, mess: Messages.Ping, pid : Int, tr: Transport){
    val dest: Node = Network.get(mess.idsrc.asInstanceOf[Int]);
    val message: Pong = new Pong(
        host.getID(),
        dest.getID(),
        pid
        )
    tr.send(host, dest, message,pid)
  }

  def receivePong(host: Node, mess: Messages.Pong, pid : Int, tr: Transport ){
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
