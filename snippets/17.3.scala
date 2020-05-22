import mill._, scalalib._
trait SyncModule extends ScalaModule {
  def scalaVersion = "2.13.1"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:1.1.0",
    ivy"com.lihaoyi::os-lib:0.7.0"
  )
}
object shared extends SyncModule
object sync extends SyncModule{
  def moduleDeps = Seq(shared)
}
object agent extends SyncModule{
  def moduleDeps = Seq(shared)
}