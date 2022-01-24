package paxos

import java.{util => ju}
import peersim.edsim.EDProtocol
import peersim.core.Network
import peersim.core.Node
import peersim.transport.Transport

case class LearnerProtocol() extends EDProtocol {


    val history : ju.List[(Long,Long)] = new ju.ArrayList //(numRound,Value)
    val nbNodes = Network.size()

    def receiveAccept(host: Node, dest: Node, tr: Transport) {
        //Find a way to keep every reception for a Round Number and to be able to increment the number of each received values
        // Once it reach more than 1/2 nbNodes Add said value to history  
    }

    override def processEvent(x$1: Node, x$2: Int, x$3: Object): Unit = {}
}
