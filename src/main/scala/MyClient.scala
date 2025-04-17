import zio._
import zio.console._
import scalapb.zio_grpc._
import contact.contact._
//def managedChannel():ManagedChannel = {val metadata = new Metadata()
//val tenant = Metadata.Key.of("tenantid", Metadata.ASCII_STRING_MARSHALLER)
//val instance = Metadata.Key.of("instanceurl", Metadata.ASCII_STRING_MARSHALLER)
//val token = Metadata.Key.of("accesstoken", Metadata.ASCII_STRING_MARSHALLER)metadata.put(tenant, "bajheuyadjh")val instUrl= URI(https://codino--sprinttest.sandbox.my.salesforce.com/).resolve("/)
//metadata.put(instance, instUrl.toString)
//metadata.put(token, "svshjvhhhjv")
//
//  ManagedChannelBuilder.forAddress(host, port).forAddress("api.pubsub.salesforce.com",7443).intercept(MetadataUtils.newAttachHeadersInterceptor(metadata)).build()
//}
//
//"this the code in plain scala for ManagedChannel it is working fine but in ZMannageChannel is is not working"
object ContactClient extends App {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val clientLayer = ClientLayer.fromChannel(
      Managed.make(
        ZIO.effect(grpc.netty.NettyChannelBuilder.forAddress("localhost", 9000).usePlaintext().build())
      )(channel => ZIO.effect(channel.shutdown()))
    )

    val fetchMessages: ZIO[ZEnv with Client with Console, StatusException, Unit] = for {
      client <- ContactService.client
      request = FetchRequest("contact__e")
      responseStream <- client.fetch(request)
      _ <- responseStream.foreach { response =>
        response.events.foreach(msg => putStrLn(s"Received message: ${msg.content}"))
      }
    } yield ()

    fetchMessages.provideLayer(clientLayer).exitCode

  }
}
Step 1: Define the Authentication Logic
  The AuthInfo will now include a session timeout in seconds, which will dictate when the client should re-authenticate.
  Example Authentication Service
scala
case class AuthInfo(url: String, token: String, sessionTimeout: Int)

object AuthService {
  def fetchAuthInfo: ZIO[Any, Throwable, AuthInfo] = {
    // Simulate fetching auth info from an external server
    // Here we assume a session timeout of 600 seconds (10 minutes)
    ZIO.succeed(AuthInfo("localhost:9000", "your-auth-token", 600))
  }
}
Step 2: Create a Managed Channel
  The method to build a managed gRPC channel remains unchanged. It will still use the AuthInfo to create a new channel.
Example Managed Channel Logic
  scala
import io.grpc.ManagedChannelBuilder
import zio._

def managedChannel(authInfo: AuthInfo): Managed[Throwable, ManagedChannel] = {
  ZIO.effect(
    ManagedChannelBuilder.forTarget(authInfo.url)
      .usePlaintext()
      .intercept(new AuthInterceptor(authInfo.token))
      .build()
  ).toManaged(channel => ZIO.effect(channel.shutdown()))
}
Example Auth Interceptor
The interceptor to attach the authentication token remains unchanged:
  scala
import io.grpc._

class AuthInterceptor(token: String) extends ClientInterceptor {
  override def interceptCall[ReqT, RespT](
                                           call: ClientCall[ReqT, RespT],
                                           headers: Metadata
                                         ): ClientCall[ReqT, RespT] = {
    headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), token)
    call.start(new ClientCall.Listener[RespT]() {}, headers)
    call
  }
}
Step 3: Implement the Client with Session Timeout Handling
Now implement the client logic that fetches AuthInfo, creates a managed channel, and subscribes to events. The client will refresh the authentication and channel based on the session timeout received.
Complete Client Implementation
scala
import zio._
import zio.console._
import scalapb.zio_grpc._
import contact.contact._
import scala.concurrent.duration._

object ContactClient extends App {

  def fetchMessages(authInfo: AuthInfo): ZIO[ZEnv with Console with Client, StatusException, Unit] = {
    for {
      channel <- managedChannel(authInfo).use { clientChannel =>
        val client = ContactService.client(clientChannel)
        val request = FetchRequest("contact__e")
        for {
          responseStream <- client.fetch(request)
          _ <- responseStream.foreach { response =>
            response.events.foreach(msg => putStrLn(s"Received message: ${msg.content}"))
          }
        } yield ()
      }
    } yield ()
  }

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val program = for {
      authInfo <- AuthService.fetchAuthInfo // Initial fetch of auth info
      _ <- ZIO.repeatEffectWith(
        for {
          newAuthInfo <- AuthService.fetch getRandomNumberGMessageMe{RanodomNumberRequetstRanNumAuthInfo // Fetch new auth info before each subscription
          _ <- fetchMessages(newAuthInfo) // Subscribe to messages with new auth info
        } yield (),
        schedule => schedule.every(authInfo.sessionTimeout.seconds) // Use session timeout for re-login
      )
    } yield ()

    program.exitCode
  }
}




-----------------------------



import zio._
import zio.stream._
import zio.duration._

object AuthService {
  def fetchAuthInfo: ZIO[Any, Throwable, AuthInfo] = ???
}

def fetchMessages(authInfo: AuthInfo): ZIO[Any, Throwable, Unit] = ???

def runWithAuthSubscription: ZIO[Any, Throwable, Unit] = {
  for {
    initialAuthInfo <- AuthService.fetchAuthInfo // Initial fetch of auth info
    _ <- ZStream
      .repeatEffectWith(
        AuthService.fetchAuthInfo,
        Schedule.spaced(initialAuthInfo.sessionTimeout.seconds) // Use session timeout for re-login
      )
      .foreach { newAuthInfo =>
        fetchMessages(newAuthInfo) // Subscribe to messages with new auth info
      }
  } yield ()
}
