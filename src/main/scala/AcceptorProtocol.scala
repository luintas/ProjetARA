package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

case class AcceptorProtocol(val prefix: String) extends EDProtocol {
    val tmp = prefix.split("\\.")
    val pid_transport: Int =
        Configuration.getPid(prefix + "." + ProposerProtocol.PAR_TRANSPORT);
    private val mypid: Int = Configuration.lookupPid(tmp.last);
    private var roundNum : Int = 0;
    private var choosedValue: Int = 0;
    private var oldValue: List[Integer] = List[Integer]();

    override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    event match {
      case mess: Prepare => receivePrepare(host, mess)
      case mess: Commit => receiveCommit(host, mess)
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
    def receivePrepare(host: Node, mess: Messages.Prepare){
        //si n > a (num round)
        if(mess.roundNum> roundNum){

        }else{
            // sendReject
            broadcast(host, sendReject)
        }
        
    }

    def sendReject(host: Node, dest: Node, tr: Transport){
        val mess: Reject =
            new Reject(host.getID(), dest.getID(), mypid, roundNum)
        tr.send(host, dest, mess, mypid)
    }


}