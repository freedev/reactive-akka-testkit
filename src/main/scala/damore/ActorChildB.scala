package damore

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive
import akka.pattern.{AskTimeoutException, Patterns, PipeToSupport, ask}

import scala.concurrent.duration._

object ActorChildB {
  def props(parentActor:ActorRef, dest: ActorRef, msg : Any): Props = Props(classOf[ActorChildB], parentActor, dest, msg)
}

// class MyTimeoutException(dest: ActorRef, msg : Any) extends Exception

class ActorChildB(parentActor:ActorRef, dest:ActorRef, msg:Any) extends Actor with ActorLogging {
  import ActorB._

  log.info(s"ActorChildB - Started because of ActorB")

  var cancellableSchedule:Option[Cancellable] = None
  var counter = 0

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
      cancellableSchedule.foreach(c => c.cancel())
      parentActor ! r
    }
    case r:SendMessage => {
      log.info("ActorChildB - Received SendMessage from " + sender())
  //      sendMessage()
        sendScheduledMessage
      }
   }

  private def sendScheduledMessage(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.language.postfixOps
    cancellableSchedule = Option(context.system.scheduler.schedule(0 milliseconds, 50 milliseconds){
      sendMessage()
    })
  }

   private def sendMessage(): Unit = {
      log.info("ActorChildB - sendMessage " + msg.getClass.getName + " to " + dest)
      counter = counter + 1
      if (counter < 10) {
        dest ! msg
      } else  {
        throw new AskTimeoutException("Fine")
      }
   }
}

