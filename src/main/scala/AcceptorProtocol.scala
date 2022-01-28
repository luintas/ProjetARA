package paxos

import peersim.edsim.EDProtocol
import peersim.core.Node
import peersim.config.Configuration
import paxos.Messages._
import peersim.transport.Transport
import peersim.core.Network

trait AcceptorProtocol {
  var roundNum: Long = 0; // Current round num
  var choosedValue: Long = 0; // Current accepted value
  var oldValue: List[Integer] = List[Integer](); // Last accepted value
  var oldRoundNum: Long = 0; // Store num of the last round in which we sent a Promise 

  def broadcast(host: Node, pid : Int, sendFunction: (Node, Node, Transport, Int) => Unit) {
    val tr: Transport = host.getProtocol(pid).asInstanceOf[Transport]
    for (i <- Range(0, Network.size())) {
      val dest: Node = Network.get(i);
      sendFunction(host, dest, tr, pid)
    }
  }
  def receivePrepare(host: Node,pid : Int, mess: Messages.Prepare) {
    //si n > a (num round)
    if (mess.roundNum > roundNum) {
      oldRoundNum = roundNum;
      roundNum = mess.roundNum
      broadcast(host,pid, sendPromise)
    } else {
      // sendReject
      broadcast(host,pid, sendReject)
    }

  }
  def receiveCommit(host: Node,pid : Int, mess: Messages.Commit) {
    // Commit are not acknowledged if they're from a previous round   
    if (mess.roundNum >= roundNum) {
      choosedValue = mess.choosedValue
      broadcast(host,pid, sendAccepted)
    }
  }
  def sendAccepted(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: Accepted =
      new Accepted(host.getID(), dest.getID(), pid, choosedValue, roundNum)
    tr.send(host, dest, mess, pid)
  }

  def sendReject(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: Reject =
      new Reject(host.getID(), dest.getID(), pid, roundNum)
    tr.send(host, dest, mess, pid)
  }

  def sendPromise(host: Node, dest: Node, tr: Transport, pid : Int) {
    val mess: Promises =
      new Promises(
        host.getID(),
        dest.getID(),
        pid,
        roundNum,
        choosedValue,
        oldRoundNum
      )
    tr.send(host, dest, mess, pid)
  }

}
