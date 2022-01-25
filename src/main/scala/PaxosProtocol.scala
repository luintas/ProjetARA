package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

class PaxosProtocol(val prefix: String) extends ProposerProtocol(prefix) with AcceptorProtocol with LearnerProtocol {
  val tmp = prefix.split("\\.")
  val pid_transport: Int =
    Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);
  private val mypid: Int = Configuration.lookupPid(tmp.last);
  private var roundNum: Long = 0;
  private var choosedValue: Long = 0;
  private var oldValue: List[Integer] = List[Integer]();
  private var oldRoundNum: Long = 0;
  private var mylist: List[Integer] = List[Integer]();
  private var deja_dit_bonjour: Boolean = false;
  private var currentRoundNum: Int = 0;
  private var isLeader = false;
  private var haveAleader = false;
  private var TimetoWait: Long = 10;

  private val acceptorsCount = 0; //TODO Find a way to get the actual value
  private var promiseReceivedCount = 0;
  private var biggestValueReceived: (Long, Long) = (0, 0) // (nbRound,value)

  override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    event match {
      case mess: Prepare => receivePrepare(host, mess)
      case mess: Commit  => receiveCommit(host, mess)
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
  def receivePrepare(host: Node, mess: Messages.Prepare) {
    //si n > a (num round)
    if (mess.roundNum > roundNum) {
      oldRoundNum = roundNum;
      roundNum = mess.roundNum
      broadcast(host, sendPromise)
    } else {
      // sendReject
      broadcast(host, sendReject)
    }

  }
  def receiveCommit(host: Node, mess: Messages.Commit) {
    if (mess.roundNum >= roundNum) {
      choosedValue = mess.choosedValue
      broadcast(host, sendAccepted)
    }
  }
  def sendAccepted(host: Node, dest: Node, tr: Transport) {
    val mess: Accepted =
      new Accepted(host.getID(), dest.getID(), mypid, choosedValue, roundNum)
    tr.send(host, dest, mess, mypid)
  }

  def sendReject(host: Node, dest: Node, tr: Transport) {
    val mess: Reject =
      new Reject(host.getID(), dest.getID(), mypid, roundNum)
    tr.send(host, dest, mess, mypid)
  }

  def sendPromise(host: Node, dest: Node, tr: Transport) {
    val mess: Promises =
      new Promises(
        host.getID(),
        dest.getID(),
        mypid,
        roundNum,
        choosedValue,
        oldRoundNum
      )
    tr.send(host, dest, mess, mypid)
  }
}
