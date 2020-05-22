```bash
./mill -i sync.test
```

Diff from [18.2 - Pipelined](https://github.com/handsonscala/handsonscala/tree/master/examples/18.18.2%20-%20Pipelined):
```diff
diff --git a/18.2 - Pipelined/sync/src/Sync.scala b/18.4 - ForkJoinHashing/sync/src/Sync.scala
index 50a7850..f4626fe 100644
--- a/18.2 - Pipelined/sync/src/Sync.scala	
+++ b/18.4 - ForkJoinHashing/sync/src/Sync.scala	
@@ -1,4 +1,5 @@
 package sync
+import scala.concurrent.Future
 object Sync {
   def main(args: Array[String]): Unit = {
     val src = os.Path(args(0), os.pwd)
@@ -24,17 +25,36 @@ object Sync {
         }
       }
     }
-    object HashActor extends castor.SimpleActor[Rpc.StatInfo]{
-      def run(msg: Rpc.StatInfo): Unit = {
-        println("HashActor handling: " + msg)
-        val localHash = Shared.hashPath(src / msg.p)
-        SyncActor.send(HashStatInfo(localHash, msg))
+    sealed trait HashActorMsg
+    case class SingleStatInfo(value: Rpc.StatInfo) extends HashActorMsg
+    case class HashComplete(values: Seq[HashStatInfo]) extends HashActorMsg
+    object HashActor extends castor.StateMachineActor[HashActorMsg]{
+      def initialState = Idle()
+      case class Buffering(msgs: Map[os.SubPath, Option[Int]]) extends State({
+        case SingleStatInfo(value) =>
+          Buffering(msgs + (value.p -> value.fileHash))
+
+        case HashComplete(values) =>
+          values.foreach(SyncActor.send(_))
+          if (msgs.isEmpty) Idle()
+          else processBuffered(msgs)
+      })
+      case class Idle() extends State({
+        case SingleStatInfo(statInfo) => processBuffered(Map(statInfo.p -> statInfo.fileHash))
+      })
+      def processBuffered(msgs: Map[os.SubPath, Option[Int]]) = {
+        val futures = for((p, fileHash) <- msgs) yield Future {
+          HashStatInfo(Shared.hashPath(src / p), Rpc.StatInfo(p, fileHash))
+        }
+
+        this.sendAsync(Future.sequence(futures.toSeq).map(HashComplete(_)))
+        Buffering(Map())
       }
     }
     println(s"Sync.main agentReader")
     val agentReader = new Thread(() => {
       while (agent.isAlive()) {
-        HashActor.send(Shared.receive[Rpc.StatInfo](agent.stdout.data))
+        HashActor.send(SingleStatInfo(Shared.receive[Rpc.StatInfo](agent.stdout.data)))
       }
     })
     agentReader.start()
```