package sync
object Sync {
  def main(args: Array[String]): Unit = {
    val src = os.Path(args(0), os.pwd)
    val dest = os.Path(args(1), os.pwd)
    println(s"Sync.main $src $dest")
    val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
    os.perms.set(agentExecutable, "rwx------")
    val agent = os.proc(agentExecutable).spawn(cwd = dest)

    sealed trait Msg
    case class ChangedPath(value: os.SubPath) extends Msg
    case class HashStatInfo(localHash: Option[Int], value: Rpc.StatInfo) extends Msg
    import castor.Context.Simple.global
    object SyncActor extends castor.SimpleActor[Msg]{
      def run(msg: Msg): Unit = {
        println("SyncActor handling: " + msg)
        msg match {
          case ChangedPath(value) => Shared.send(agent.stdin.data, Rpc.StatPath(value))
          case HashStatInfo(localHash, Rpc.StatInfo(p, remoteHash)) =>
            if (localHash != remoteHash && localHash.isDefined) {
              Shared.send(agent.stdin.data, Rpc.WriteOver(os.read.bytes(src / p), p))
            }
        }
      }
    }
    object HashActor extends castor.SimpleActor[Rpc.StatInfo]{
      def run(msg: Rpc.StatInfo): Unit = {
        println("HashActor handling: " + msg)
        val localHash = Shared.hashPath(src / msg.p)
        SyncActor.send(HashStatInfo(localHash, msg))
      }
    }
    println(s"Sync.main agentReader")
    val agentReader = new Thread(() => {
      while (agent.isAlive()) {
        HashActor.send(Shared.receive[Rpc.StatInfo](agent.stdout.data))
      }
    })
    agentReader.start()
    println(s"Sync.main watcher")
    val watcher = os.watch.watch(
      Seq(src),
      onEvent = _.foreach{p =>
        println(s"Sync.main watcher onEvent p " + p)
        SyncActor.send(ChangedPath(p.subRelativeTo(src)))
      }
    )

    Thread.sleep(999999)
  }
}
