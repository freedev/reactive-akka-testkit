package damore

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll

class TestSuite
  extends Step1
//    with Tools
    with BeforeAndAfterAll {

  val config = ConfigFactory.parseString("""
    akka.loglevel = "DEBUG"
    akka.actor.debug {
      receive = on
      lifecycle = on
    }
    """)

  implicit val system: ActorSystem = ActorSystem("KVStoreSuite")

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

