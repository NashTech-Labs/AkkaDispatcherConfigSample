package com.knoldus.dispatcherExmple

import akka.actor.Actor
import akka.actor.Props
import akka.pattern._
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory

/**
 * @author GirishBharti
 */
case class Count(line: String)
case class Print(line: String)

class WordCountActor extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)
  
  def receive = {
    case Count(line: String) => {
      logger.debug("WordCountActor called")
      logger.debug("Thread name for counter actor::::::::::::::: " + Thread.currentThread().getName)
      sender ! line.length()
      val printer = context.actorOf(Props[PrinterActor].withDispatcher("my-thread-pool-dispatcher"), "printerActor")
      val actorpath = printer.path
      logger.debug("PrinterActor path ::::: " + actorpath)
      printer ! Print(line)
    }
    case _ => logger.debug("Oops..!! I did'nt understand the message..!!")
  }

}

class PrinterActor extends Actor{
  val logger = LoggerFactory.getLogger(this.getClass)
  
  def receive ={
    case Print(file: String) => {
      logger.debug("PrinterActor called")
      logger.debug("Thread name for printer actor::::::::::::::: " + Thread.currentThread().getName)
      val linesToPrint = file.split(" ").take(300).mkString(" ")
      logger.debug("First 300 words from the file you entered is :::: " + linesToPrint)
    }
    case _ => logger.debug("Oops..!! I did'nt understand the message..!!")
  }
  
}

object DispatcherExampleMain extends App {
  val logger = LoggerFactory.getLogger(this.getClass)
  
  logger.debug("Hello, You are in Dispatcher example")
  Thread.sleep(2000)
  logger.debug("You are going to see how to use dispatcher here")
  Thread.sleep(2000)

  implicit val timeOut = Timeout(10 seconds)
  
  //Defining actor system here
  val system = ActorSystem("wordprocessor")

  //getting data from text file, Change th name of the file according to your system
  val file = scala.io.Source.fromFile("./SampleFile/fileExample").getLines.mkString

  /**
   * First way to use dispatchers with conf file
   */
  val wordCounterActor = system.actorOf(Props[WordCountActor], "wordCounter")

  val actorpath = wordCounterActor.path

  logger.debug("WordCountActor path :::::::::::: " + actorpath)

  val res = wordCounterActor ? Count(file)

  res.map { count =>
    logger.debug("TOTAL CHARACTERS COUNT IN FILE ::::: " + count.asInstanceOf[Int])
    Thread.sleep(2000)
    system.shutdown()
  }

}
