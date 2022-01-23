package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
class ProposerProtocol(val prefix : String) extends EDProtocol {
  val tmp = prefix.split("\\.")
//   var pid_transport: Int =
    // Configuration.getPid(prefix + "." + HelloProtocol.PAR_TRANSPORT);
//   var maxsizelist: Int =
    // Configuration.getInt(prefix + "." + HelloProtocol.PAR_MAXSIZELIST);
  private val mypid: Int = Configuration.lookupPid(tmp.last);
  private var mylist: List[Integer] = List[Integer]();
  private var deja_dit_bonjour: Boolean = false;
  override def processEvent(host: Node, pid: Int, event: Object): Unit = {
    if (pid != mypid)
      throw new IllegalArgumentException(
        "Incoherence sur l'identifiant de protocole"
      );
    if (event.isInstanceOf[Promises]) {
      receivePromise(host, event.asInstanceOf[Promises])
    } else {
      throw new IllegalArgumentException("Evenement inconnu pour ce protocole");
    }
  }

  def receiveStartMessage(tmp : StartMessage ){
      //Envoyer un message Prepare à tout les Acceptors
      val lstAcceptors = List();//Put the actual list here
      lstAcceptors.foreach(sendPrepare(_))
  }
  def receivePromise(host: Node, mess : Messages.Promises){

  }

}
