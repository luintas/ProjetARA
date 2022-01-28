package paxos

import java.{util => ju}
import peersim.edsim.EDProtocol
import peersim.core.Network
import peersim.core.Node
import peersim.transport.Transport
import paxos.Messages

trait LearnerProtocol  {


    val history : ju.List[Long] = new ju.ArrayList //(numRound,Value)
    val nbNodes = Network.size()
    var lstReceived = List[(Long,Int)]();

    def receiveAccepted(host: Node, mess : Messages.Accepted) {
        //Find a way to keep every reception for a Round Number and to be able to increment the number of each received values
        // Once it reach more than 1/2 nbNodes Add said value to history
        lstReceived = ((mess.choosedValue,1)) ::  lstReceived
        if(lstReceived.length > nbNodes/2 ) {
            val mostReceivedValue = lstReceived.groupBy( _._1).maxBy(_._2.length)
            history.add(mostReceivedValue._1)}
        //Find what we will have to do according to the round we're in
    }
}
