package damore

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorLogging, ActorRef, OneForOneStrategy, Props, Status, SupervisorStrategy}
import akka.pattern.{AskTimeoutException, CircuitBreaker, Patterns}
import akka.util.Timeout
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._

object ActorChildB {
  def props(actorB: ActorRef, actorC: ActorRef): Props = Props(classOf[ActorChildB], actorB, actorC)
}

class ActorChildB(actorB: ActorRef, actorC: ActorRef) extends Actor with ActorLogging {
  import ActorB._

  log.info(s"ActorChildB - Started because of ActorB")

  override def preStart(): Unit = {
    log.info("ActorChildB - preStart")
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    log.info(s"Restarted because of ${reason.getMessage}")
  }

  def receive = {
    case r:StartChild => {
      log.info("received message " + r)
      val supervisor = context.sender()
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val scheduler=context.system.scheduler
      implicit val askTimeout = Timeout(50.millisecond)
      //    log.info("ActorChildB - before preStart asking MessageB2C to actorC")
      val p = MessageB2C()
      val future =  actorC ? p
      //    log.info("ActorChildB - after preStart asking MessageB2C to actorC")
      future recover {
        case e: AskTimeoutException => {
          log.info("Failure detected with AskTimeoutException")
          supervisor ! Status.Failure(e)
        }
        case _ => {
          log.info("Failure detected")
        }
      }
    }
//    case r => {
//      log.info("ActorChildB - received UNHANDLED message " + r)
//    }
  }

  private def sendMessage(o: Operation, retryN: Int, client : ActorRef) = {


//     val future = Patterns.askWithReplyTo(actorC, actorRef -> o, 50)
//
//    future onSuccess ({
//      case p: MessageB2C_Ack => {
//        log.info("ActorB - Received MessageB2C_Ack so now sending an MessageA2B_Ack to client " + client)
//        client ! MessageA2B_Ack()
//      }
//    })
//    future recover {
//      case e: AskTimeoutException =>
//        if (retryN < 6)
//          self ! OperationRetry(o, retryN)
//    }
  }
}

