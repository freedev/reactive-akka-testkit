package damore

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive

import scala.concurrent.duration._

object ActorChildB {
  def props(parentActor:ActorRef, dest: ActorRef, msg : Any): Props = Props(classOf[ActorChildB], parentActor, dest, msg)
}

class MyRetryTimeoutException(msg:String) extends Exception(msg)

class ActorChildB(parentActor:ActorRef, dest:ActorRef, msg:Any) extends Actor with ActorLogging {
  import ActorB._


  log.info(s"ActorChildB - Started because of ActorB")

  var cancellableSchedule : Option[Cancellable] = None

  var counter = 0
  var maxCounter = 10

  override def postStop(): Unit = {
    cancellableSchedule.foreach(c => c.cancel())
    super.postStop()
  }

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
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.language.postfixOps
    cancellableSchedule =  Option(context.system.scheduler.schedule(0 milliseconds, 50 millisecond){
      sendMessage()
    })
  }

   private def sendMessage(): Unit = {
      log.info("ActorChildB - sendMessage " + msg.getClass.getName + " to " + dest)
      if (counter < maxCounter) {
        dest ! msg
        counter = counter + 1
      } else  {
        throw new MyRetryTimeoutException("Fine")
      }
   }
}

