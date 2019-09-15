package kvstore

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern.{Patterns, RetrySupport}
import akka.util.Timeout

import scala.concurrent.duration._

object ActorB {

  sealed trait Operation
  case class MessageA2B() extends Operation
  case class MessageB2C() extends Operation

  sealed trait OperationReply
  case class MessageA2B_Ack() extends OperationReply
  case class MessageA2B_Failed() extends OperationReply

  case class MessageB2C_Ack()  extends OperationReply

  def props(persistenceProps: Props): Props = Props(new ActorB(persistenceProps))
}

class ActorB(actorCProps: Props) extends Actor {
  import ActorB._
  import context.dispatcher

  val log = Logging(context.system, this)

  val actorC = context.actorOf(actorCProps)

  def receive = {
    case r:MessageB2C_Ack => {
      log.info("ActorB - secondary received UNHANDLED message: MessageB2C_Ack")
    }
    case r:MessageA2B => {
      val client = context.sender()
      implicit val timeout = Timeout(100.milliseconds)
      log.info("ActorB received message MessageA2B from client " + client)
      implicit val scheduler=context.system.scheduler
      val p = MessageB2C()

      RetrySupport.retry(() => {
        log.info("ActorB - sent message MessageB2C to ActorC " + actorC)
        Patterns.ask(actorC, p, 100.millisecond)
      }, 10, 100.millisecond)
      .onSuccess({
        case p: MessageB2C_Ack => {
          log.info("ActorB - Received MessageB2C_Ack so now sending an MessageA2B_Ack to client " + client)
          client ! MessageA2B_Ack()
        }
      })

    }
    case r => {
      log.info("ActorB - received UNHANDLED message " + r)
    }
  }

}

