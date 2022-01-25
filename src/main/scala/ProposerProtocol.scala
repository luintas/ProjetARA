package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network
case class ProposerProtocol(val prefix: String) extends EDProtocol {
  val tmp = prefix.split("\\.")
  val pid_transport: Int =
    Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);
//   var maxsizelist: Int =
  // Configuration.getInt(prefix + "." + HelloProtocol.PAR_MAXSIZELIST);
  private val mypid: Int = Configuration.lookupPid(tmp.last);
  private var mylist: List[Integer] = List[Integer]();
  private var deja_dit_bonjour: Boolean = false;
  private var currentRoundNum: Int = 0;
  private var isLeader = false ;
  private var haveAleader = false;

  private val acceptorsCount = 0; //TODO Find a way to get the actual value
  private var promiseReceivedCount = 0;
  private var biggestValueReceived: (Long, Long) = (0, 0) // (nbRound,value)

  override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    event match {
      case mess: Promises     => receivePromise(host, mess)
      case mess: StartMessage => receiveStartMessage(host, mess)
      case mess: Reject => receiveReject(host, mess)
      case mess: Any =>
        throw new IllegalArgumentException(
          "Evenement inconnu pour ce protocole"
        );
    }
  }
  def broadcast(host: Node, sendFunction: (Node, Node, Transport) => Unit) {
    val tr: Transport = host.getProtocol(pid_transport).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr)
    }
  }
  def receiveStartMessage(host: Node, mess: StartMessage) {
    //TODO : Modify this part so it act in accordance to the Leader definition
    biggestValueReceived =
      (
        currentRoundNum,
        mess.clientValue
      ) // HELP : If there's an error check if the round number is right
    broadcast(host, sendPrepare)
  }
  def receivePromise(host: Node, mess: Messages.Promises) {
    promiseReceivedCount += 1
    if (mess.roundNum > biggestValueReceived._1) {
      biggestValueReceived = (mess.roundNum, mess.previousVal)
    }
    if (promiseReceivedCount > acceptorsCount / 2) {
      broadcast(host, sendCommit)
    }
  }
  def receiveReject(host : Node, mess : Messages.Reject) {
    currentRoundNum = currentRoundNum +1;
  }
  def sendPrepare(host: Node, dest: Node, tr: Transport) {
    val mess: Prepare =
      new Prepare(host.getID(), dest.getID(), mypid, currentRoundNum)
    tr.send(host, dest, mess, mypid)
  }
  def sendCommit(host: Node, dest: Node, tr: Transport) {
    val mess: Commit =
      new Commit(
        host.getID(),
        dest.getID(),
        mypid,
        biggestValueReceived._2,
        currentRoundNum
      )
    tr.send(host, dest, mess, mypid)
  }

}
object ProposerProtocol {
  val PAR_TRANSPORT = "transport";
  val PAR_MAXSIZELIST = "maxsizelist";
}
