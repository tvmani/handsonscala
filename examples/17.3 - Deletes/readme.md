```bash
./mill -i sync.test
```

Diff from [17.2 - Pipelined](https://github.com/handsonscala/handsonscala/tree/master/examples/17.17.2%20-%20Pipelined):
```diff
diff --git a/17.2 - Pipelined/agent/src/Agent.scala b/17.3 - Deletes/agent/src/Agent.scala
index a7d17cc..7d81bb9 100644
--- a/17.2 - Pipelined/agent/src/Agent.scala	
+++ b/17.3 - Deletes/agent/src/Agent.scala	
@@ -1,4 +1,5 @@
 package sync
+import Rpc.subPathRw
 object Agent {
   def main(args: Array[String]): Unit = {
     val input = new java.io.DataInputStream(System.in)
@@ -9,6 +10,8 @@ object Agent {
         case Rpc.IsDir(path) => Shared.send(output, os.isDir(os.pwd / path))
         case Rpc.Exists(path) => Shared.send(output, os.exists(os.pwd / path))
         case Rpc.ReadBytes(path) => Shared.send(output, os.read.bytes(os.pwd / path))
+        case Rpc.Delete(path) => Shared.send(output, os.remove(os.pwd / path))
+        case Rpc.RemoteScan() => Shared.send(output, os.walk(os.pwd).map(_.subRelativeTo(os.pwd)))
         case Rpc.WriteOver(bytes, path) =>
           os.remove.all(os.pwd / path)
           Shared.send(output, os.write.over(os.pwd / path, bytes, createFolders = true))
diff --git a/17.2 - Pipelined/shared/src/Rpc.scala b/17.3 - Deletes/shared/src/Rpc.scala
index b87163d..ed06878 100644
--- a/17.2 - Pipelined/shared/src/Rpc.scala	
+++ b/17.3 - Deletes/shared/src/Rpc.scala	
@@ -13,8 +13,14 @@ object Rpc{
   case class ReadBytes(path: os.SubPath) extends Rpc
   implicit val readBytesRw: ReadWriter[ReadBytes] = macroRW
 
+  case class RemoteScan() extends Rpc
+  implicit val remoteScanRw: ReadWriter[RemoteScan] = macroRW
+
   case class WriteOver(src: Array[Byte], path: os.SubPath) extends Rpc
   implicit val writeOverRw: ReadWriter[WriteOver] = macroRW
 
+  case class Delete(path: os.SubPath) extends Rpc
+  implicit val deleteRw: ReadWriter[Delete] = macroRW
+
   implicit val RpcRw: ReadWriter[Rpc] = macroRW
 }
diff --git a/17.2 - Pipelined/sync/src/Sync.scala b/17.3 - Deletes/sync/src/Sync.scala
index 58984ea..f257e82 100644
--- a/17.2 - Pipelined/sync/src/Sync.scala	
+++ b/17.3 - Deletes/sync/src/Sync.scala	
@@ -1,4 +1,5 @@
 package sync
+import Rpc.subPathRw
 object Sync {
   def main(args: Array[String]): Unit = {
     val src = os.Path(args(0), os.pwd)
@@ -11,22 +12,29 @@ object Sync {
       () => Shared.receive[T](agent.stdout.data)
     }
     val subPaths = os.walk(src).map(_.subRelativeTo(src))
-    def pipelineCalls[T: upickle.default.Reader](rpcFor: os.SubPath => Option[Rpc]) = {
-      val buffer = collection.mutable.Buffer.empty[(os.RelPath, () => T)]
-      for (p <- subPaths; rpc <- rpcFor (p)) buffer.append((p, callAgent[T](rpc)))
+    val subPathSet = subPaths.toSet
+    def pipelineCalls[T: upickle.default.Reader](paths: Seq[os.SubPath])
+                                                (rpcFor: os.SubPath => Option[Rpc]) = {
+      val buffer = collection.mutable.Buffer.empty[(os.SubPath, () => T)]
+      for (p <- paths; rpc <- rpcFor (p)) buffer.append((p, callAgent[T](rpc)))
       buffer.map{case (k, v) => (k, v())}.toMap
     }
 
-    val existsMap = pipelineCalls[Boolean](p => Some(Rpc.Exists(p)))
-    val isDirMap = pipelineCalls[Boolean](p => Some(Rpc.IsDir(p)))
+    val existsMap = pipelineCalls[Boolean](subPaths)(p => Some(Rpc.Exists(p)))
+    val isDirMap = pipelineCalls[Boolean](subPaths)(p => Some(Rpc.IsDir(p)))
 
-    val readMap = pipelineCalls[Array[Byte]]{p =>
+    val readMap = pipelineCalls[Array[Byte]](subPaths){p =>
       if (existsMap(p) && !isDirMap(p)) Some(Rpc.ReadBytes(p))
       else None
     }
 
-    pipelineCalls[Unit]{ p =>
+    val remoteScanned = callAgent[Seq[os.SubPath]](Rpc.RemoteScan()).apply()
+
+    val allPaths = (subPaths ++ remoteScanned).distinct
+
+    pipelineCalls[Unit](allPaths){ p =>
       if (os.isDir(src / p)) None
+      else if (!subPathSet.contains(p)) Some(Rpc.Delete(p))
       else {
         val localBytes = os.read.bytes(src / p)
         if (readMap.get(p).exists(java.util.Arrays.equals(_, localBytes))) None
diff --git a/17.2 - Pipelined/sync/test/src/SyncTests.scala b/17.3 - Deletes/sync/test/src/SyncTests.scala
index 1582ddf..9e2ef3c 100644
--- a/17.2 - Pipelined/sync/test/src/SyncTests.scala	
+++ b/17.3 - Deletes/sync/test/src/SyncTests.scala	
@@ -28,6 +28,16 @@ object SyncTests extends TestSuite{
       println("SECOND VALIDATION")
       assert(os.read(dest / "folder1" / "hello.txt") == "hello")
       assert(os.read(dest /  "folder1" / "nested" / "world.txt") == "WORLD")
+
+      println("DELETE SRC FILE")
+      os.remove(src / "folder1" / "hello.txt")
+
+      println("DELETE SYNC")
+      Sync.main(Array(src.toString, dest.toString))
+
+      println("DELETE VALIDATION")
+      assert(!os.exists(dest / "folder1" / "hello.txt"))
+      assert(os.read(dest /  "folder1" / "nested" / "world.txt") == "WORLD")
     }
   }
 }
```