```bash
./mill -i sync.test
```

Diff from [17.1 - FileSyncer](https://github.com/handsonscala/handsonscala/tree/master/examples/17.17.1%20-%20FileSyncer):
```diff
diff --git a/17.1 - FileSyncer/sync/src/Sync.scala b/17.2 - Pipelined/sync/src/Sync.scala
index 30e1a48..58984ea 100644
--- a/17.1 - FileSyncer/sync/src/Sync.scala	
+++ b/17.2 - Pipelined/sync/src/Sync.scala	
@@ -6,29 +6,31 @@ object Sync {
     val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
     os.perms.set(agentExecutable, "rwx------")
     val agent = os.proc(agentExecutable).spawn(cwd = dest)
-    def callAgent[T: upickle.default.Reader](rpc: Rpc): T = {
+    def callAgent[T: upickle.default.Reader](rpc: Rpc): () => T = {
       Shared.send(agent.stdin.data, rpc)
-      Shared.receive[T](agent.stdout.data)
+      () => Shared.receive[T](agent.stdout.data)
     }
-    for (srcSubPath <- os.walk(src)) {
-      val subPath = srcSubPath.subRelativeTo(src)
-      val destSubPath = dest / subPath
-      (os.isDir(srcSubPath), callAgent[Boolean](Rpc.IsDir(subPath))) match {
-        case (false, true) =>
-          callAgent[Unit](Rpc.WriteOver(os.read.bytes(srcSubPath), subPath))
-        case (true, false) =>
-          for (p <- os.walk(srcSubPath) if os.isFile(p)) {
-            callAgent[Unit](Rpc.WriteOver(os.read.bytes(p), p.subRelativeTo(src)))
+    val subPaths = os.walk(src).map(_.subRelativeTo(src))
+    def pipelineCalls[T: upickle.default.Reader](rpcFor: os.SubPath => Option[Rpc]) = {
+      val buffer = collection.mutable.Buffer.empty[(os.RelPath, () => T)]
+      for (p <- subPaths; rpc <- rpcFor (p)) buffer.append((p, callAgent[T](rpc)))
+      buffer.map{case (k, v) => (k, v())}.toMap
     }
-        case (false, false)
-          if !callAgent[Boolean](Rpc.Exists(subPath))
-            || !os.read.bytes(srcSubPath).sameElements(
-            callAgent[Array[Byte]](Rpc.ReadBytes(subPath))
-          ) =>
 
-          callAgent[Unit](Rpc.WriteOver(os.read.bytes(srcSubPath), subPath))
+    val existsMap = pipelineCalls[Boolean](p => Some(Rpc.Exists(p)))
+    val isDirMap = pipelineCalls[Boolean](p => Some(Rpc.IsDir(p)))
 
-        case _ => // do nothing
+    val readMap = pipelineCalls[Array[Byte]]{p =>
+      if (existsMap(p) && !isDirMap(p)) Some(Rpc.ReadBytes(p))
+      else None
+    }
+
+    pipelineCalls[Unit]{ p =>
+      if (os.isDir(src / p)) None
+      else {
+        val localBytes = os.read.bytes(src / p)
+        if (readMap.get(p).exists(java.util.Arrays.equals(_, localBytes))) None
+        else Some(Rpc.WriteOver(localBytes, p))
       }
     }
   }
```