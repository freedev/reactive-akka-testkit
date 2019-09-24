package damore

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Status}
import akka.pattern.AskTimeoutException
import akka.util.Timeout

import scala.concurrent.duration._


class ActorC() extends Actor  with ActorLogging {
  import ActorB._

  def receive = {
    case r:MessageB2C => {
      val client = context.sender()
      log.info("ActorC received message MessageB2C from client " + client)

      client ! MessageB2C_Ack

      log.info("ActorC sent message MessageB2C_Ack to client " + client)
    }
    case r => {
      log.info("ActorC - received UNHANDLED message " + r)
    }
  }

}

