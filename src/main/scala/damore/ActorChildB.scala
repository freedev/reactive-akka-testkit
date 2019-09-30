package damore

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.pattern.{AskTimeoutException}

import scala.concurrent.duration._

object ActorChildB {
  def props(parentActor:ActorRef, dest: ActorRef, msg : Any): Props = Props(classOf[ActorChildB], parentActor, dest, msg)
}

class ActorChildB(parentActor:ActorRef, dest:ActorRef, msg:Any) extends Actor with ActorLogging {
  import ActorB._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.postfixOps

  log.info(s"ActorChildB - Started because of ActorB")

  var maxCounter = 10

  override def preStart(): Unit = {
    log.info("ActorChildB - preStart")
//    sendMessage()
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    log.info(s"ActorChildB - Restarted because of ${reason.getMessage}")
  }

  def receive = LoggingReceive {
    case r:MessageB2C_Ack => {
      log.info("ActorChildB - Received MessageB2C_Ack from " + sender())
      parentActor ! r
      context.stop(self)
    }
    case r:SendMessage => {
      log.info("ActorChildB - Received SendMessage from " + sender())
        sendScheduledMessage
      }
   }

  private def sendScheduledMessage(): Unit = {
    context.system.scheduler.scheduleOnce(0 milliseconds){
      sendMessage(0)
    }
  }

   private def sendMessage(counter:Int): Unit = {
      log.info("ActorChildB - sendMessage " + msg.getClass.getName + " to " + dest)
      if (counter < maxCounter) {
        dest ! msg
        context.system.scheduler.scheduleOnce(50 milliseconds){
          sendMessage(counter + 1)
        }
      } else  {
        throw new AskTimeoutException("Fine")
      }
   }
}

