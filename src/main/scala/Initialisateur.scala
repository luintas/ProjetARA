import peersim.core.Control
import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import paxos.PaxosProtocol
import collection.JavaConverters._

class Initialisateur(prefix: String) extends Control {
  val hellopid: Int =
    Configuration.getPid(prefix + "." + Initialisateur.PAR_PROTO_HELLO)
  override def execute(): Boolean = {
      val pid = 
    for (i <- Range(0, Network.size())) {
      val node: Node = Network.get(i);
      val paxos: PaxosProtocol =
        node.getProtocol(hellopid).asInstanceOf[PaxosProtocol];
      paxos.firstFindLeader(node);
    }
    return false;
  }

}
object Initialisateur {
  val PAR_PROTO_HELLO: String = "hellopid";
}
