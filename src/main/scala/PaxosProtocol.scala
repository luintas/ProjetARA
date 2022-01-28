package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

class PaxosProtocol(val prefix: String) extends EDProtocol with  ProposerProtocol with AcceptorProtocol with LearnerProtocol {
  val tmp = prefix.split("\\.")
  //  val pid_transport: Int =
    // Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);

    //There will probably be a problem with the pid and pid protocol
  // val mypid = Configuration.lookupPid(tmp.last)
  private val mypid: Int = Configuration.lookupPid(tmp.last);

  override def broadcast(host: Node, pid : Int, sendFunction: (Node, Node, Transport, Int) => Unit) {
    val tr: Transport = host.getProtocol(pid).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr, pid)
    }
  }

  override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    event match {
      case mess: Prepare => receivePrepare(host, mypid, mess)
      case mess: Commit  => receiveCommit(host, mypid, mess)
      case mess: Promises     => receivePromise(host, mypid, mess)
      case mess: StartMessage => receiveStartMessage(host, mypid, mess)
      case mess: Reject => receiveReject(host, mypid, mess)
      case mess: Accepted => receiveAccepted(host, mess)
      case mess: Any =>
        throw new IllegalArgumentException(
          "Evenement inconnu pour ce protocole"
        );
    }
  }
}