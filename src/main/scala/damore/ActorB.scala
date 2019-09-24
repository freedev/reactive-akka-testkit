package damore

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Status}
import akka.pattern.ask
import akka.pattern.{AskTimeoutException, Patterns, RetrySupport, ask}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object ActorB {

  sealed trait Operation
  case class StartChild() extends Operation
  case class MessageA2B() extends Operation
  case class MessageB2C() extends Operation
  case class OperationRetry(o: Operation, n:Int) extends Operation

  sealed trait OperationReply
  case class MessageA2B_Ack() extends OperationReply
  case class MessageA2B_Failed() extends OperationReply

  case class MessageB2C_Ack()  extends OperationReply

  def props(propsActorC: Props): Props = Props(classOf[ActorB], propsActorC)
}

class ActorB(propsActorC: Props) extends Actor  with ActorLogging {
  import ActorB._
  import context.dispatcher

  val actorC = context.actorOf(propsActorC)

  // Restart the ActorChildB child if it throws AskTimeoutException
  override val supervisorStrategy = OneForOneStrategy() {
    case _: NullPointerException     => {
      log.info("supervisorStrategy received NullPointerException")
      Restart
    }
    case _: AskTimeoutException      => {
      log.info("supervisorStrategy received AskTimeoutException")
      Restart
    }
    case _: Exception                => {
      log.info("supervisorStrategy received Exception")
      Restart
    }
    case x => {
      log.info("supervisorStrategy received " + x)
      Restart
    }
  }

  implicit val timeout = Timeout(4.seconds)

  def receive = {
//    case r:Status.Failure => {
//      log.info("ActorB - secondary received message: Status.Failure")
//      self ! MessageA2B()
//    }
    case r:MessageB2C_Ack => {
      log.info("ActorB - secondary received UNHANDLED message: MessageB2C_Ack")
    }
    case r:MessageA2B => {
      val client = context.sender()
      log.info("ActorB received message MessageA2B from client " + client)

      val actorChildB = context.actorOf(ActorChildB.props(self, actorC))
      val msg = StartChild()
      actorChildB ! msg

    }
    case r => {
      log.info("ActorB - received UNHANDLED message " + r)
    }
  }

}

