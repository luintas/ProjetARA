package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

class PaxosProtocol(val prefix: String) extends EDProtocol with  ProposerProtocol with AcceptorProtocol with LearnerProtocol {
  val tmp = prefix.split("\\.")
   val pid_transport: Int =
    Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);

    //There will probably be a problem with the pid and pid protocol
  // val mypid = Configuration.lookupPid(tmp.last)
  private val mypid: Int = Configuration.lookupPid(tmp.last);
  override def broadcast(host: Node, pid : Int, sendFunction: (Node, Node, Transport, Int) => Unit) {
    val tr: Transport = host.getProtocol(pid_transport).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr, mypid)
    }
  }

  def firstFindLeader(host: Node) {
    // println("PaxosProto host is "+host.getID())
    findLeader(host,mypid,host.getProtocol(pid_transport).asInstanceOf[Transport])
    // broadcast(host, pid_transport, sendCandidate)
  }

  override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    val tr: Transport = host.getProtocol(pid_transport).asInstanceOf[Transport]
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    event match {
      case mess: Prepare => receivePrepare(host, pid, mess)
      case mess: Commit  => receiveCommit(host, pid, mess)
      case mess: Promises     => receivePromise(host, pid, mess)
      case mess: StartMessage => receiveStartMessage(host, pid, mess)
      case mess: Reject => receiveReject(host, pid, mess)
      case mess: Accepted => receiveAccepted(host, mess)
      case mess: Ping => receivePing(host, mess, pid, tr)
      case mess: Pong => receivePong(host, mess, pid, tr)
      case mess: IamLeader => receiveIamLeader(host, mess, pid, tr)
      case mess: Candidate => receiveCandidate(host, mess, pid, tr)
      case mess: Ack => receiveAck(host, mess, pid, tr)
      case mess: RejectCandidate => receiveRejectCandidate(host,mess,pid,tr)
      case mess: Any =>
        throw new IllegalArgumentException(
          "Evenement inconnu pour ce protocole"
        );
    }
  }
  override def clone(): Object = {
    try {
      var ap: PaxosProtocol = super.clone().asInstanceOf[PaxosProtocol];
      return ap;
    } catch {
      case e: CloneNotSupportedException => ()
    }
    return null;
  }
}
object PaxosProtocol {
  val PAR_TRANSPORT = "transport";
}