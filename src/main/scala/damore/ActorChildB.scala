package damore

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._

object ActorChildB {
  def props(dest: ActorRef, msg : Any, id:Long): Props = Props(classOf[ActorChildB], dest, msg, id)
}

class MyRetryTimeoutException(msg:String) extends Exception(msg)

class ActorChildB(dest:ActorRef, msg:Any, id:Long)
  extends Actor
    with ActorLogging
    with Timers
{
  import ActorB._

  timers.startPeriodicTimer(id, TimerRetry(), 100.millis)
  dest ! msg

  log.info(s"ActorChildB - Started because of ActorB")

  var counter = 0
  var maxCounter = 10

  override def postStop(): Unit = {
    timers.cancel(id)
    super.postStop()
  }

  override def preStart(): Unit = {
    log.info("ActorChildB - preStart")
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    log.info(s"ActorChildB - Restarted because of ${reason.getMessage}")
  }

  def receive = LoggingReceive {
    case r:MessageB2C_Ack => {
      log.info("ActorChildB - Received MessageB2C_Ack from " + sender())
      context.parent ! r
      context.stop(self)
    }
    case r:TimerRetry => {
      log.info("ActorChildB - Received TimerRetry from " + sender())
      if (counter < maxCounter) {
        dest ! msg
        counter = counter + 1
      } else  {
        throw new MyRetryTimeoutException("Fine")
      }
    }
  }

}

