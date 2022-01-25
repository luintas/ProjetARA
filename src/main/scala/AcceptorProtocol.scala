package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

case class AcceptorProtocol(val nbFaute: Int) extends EDProtocol {
    private var roundNum : Int = 0;
    private var choosedValue: Int = 0;
    private var oldValue: List[Integer] = List[Integer]();

    override def processEvent(host: Node, pid: Int, event: Object): Unit = {

    }

    def receivePrepare(host: Node, mess: Messages.Promises){

    }


}