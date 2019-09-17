package damore

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.{AskTimeoutException, Patterns, RetrySupport}
import akka.util.Timeout

import scala.concurrent.Future
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

  def retry(actorRef: ActorRef, message: Any, maxAttempts: Int): Future[Any] = {
     retry(actorRef, message, maxAttempts, 1)
  }

  def retry(actorRef: ActorRef, message: Any, maxAttempts: Int, attempt: Int): Future[Any] = {
    log.info("ActorB - sent message MessageB2C to ActorC " + actorC)
    val future = Patterns.ask(actorRef, message, 50.millisecond) recover {
      case e: AskTimeoutException =>
        if (attempt <= maxAttempts) retry(actorRef, message, maxAttempts, attempt + 1)
        else None // Return default result according to your requirement, if actor is non-reachable.
    }
    future
  }

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

      //Return a new future that will retry up to 10 times
//      val retried = akka.pattern.retry(() => attempt(actorC), 10, 100 milliseconds)

//      val retried = RetrySupport.retry(() => {
//        log.info("ActorB - sent message MessageB2C to ActorC " + actorC)
//        Patterns.ask(actorC, p, 50.millisecond)
//      }, 6, 0.millisecond)
      retry(actorC, p, 10) onSuccess({
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

