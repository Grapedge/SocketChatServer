import java.io.*
import java.net.ServerSocket
import java.net.Socket

class FileServer(port: Int, private val path: String) : ServerSocket(port) {
    // 客户端列表，不过我们用不到
    var clientList = mutableListOf<ClientHandler>()

    init {
        println("文件传输服务已启动")
    }

    fun start() {
        while (true) {
            val socket = accept()
            clientList.add(ClientHandler(socket, path, this).build())
        }
    }

    class ClientHandler(var client: Socket, private val path: String, private var target: FileServer) : Thread() {

        private var reader: DataInputStream = DataInputStream(client.getInputStream())
        private var writer: DataOutputStream = DataOutputStream(client.getOutputStream())

        init {
            println("客户端：$name 已经准备传输文件")
        }

        fun build(): ClientHandler {
            start()
            return this
        }

        override fun run() {
            val type = reader.readInt()
            when (type) {
                // 客户端想要发送文件到服务器
                0 -> {
                    saveFile()
                }
                // 客户端想要接收文件
                1 -> {
                    sendFile()
                }
            }
            // 本次传输结束
            reader.close()
            writer.close()
            client.close()
            target.clientList.remove(this)
        }

        private fun saveFile() {
            var fout: FileOutputStream? = null
            try {
                // 获取文件名
                val fileName = reader.readUTF()
                val fullPath = path + fileName
                val fileLength = reader.readLong()

                println("文件将存储至：$fullPath")
                println("开始接收$fileName")

                fout = FileOutputStream(File(fullPath))
                val bytes = ByteArray(1024)
                var length = reader.read(bytes, 0, bytes.size)
                while (length != -1) {
                    fout.write(bytes, 0, length)
                    fout.flush()
                    println("$fileName 已接收：$length，共有$fileLength")
                    length = reader.read(bytes, 0, bytes.size)
                }
                println("$fileName 接收完成")
                server.sendFile(fileName)
            } catch (e: Exception) {
                println(e.message)
            } finally {
                fout?.close()
            }
        }

        // 发送文件到客户端
        private fun sendFile() {
            // 读取文件名
            var fileInputStream: FileInputStream? = null
            try {
                val file = File(path + reader.readUTF())
                if (file.exists()) {
                    fileInputStream = FileInputStream(file)
                    // 写入文件长度
                    writer.writeLong(file.length())
                    writer.flush()

                    // 以 1 kb 为单位进行传输
                    val bytes = ByteArray(1024)
                    var length = fileInputStream.read(bytes, 0, bytes.size)
                    while (length != -1) {
                        writer.write(bytes)
                        writer.flush()
                        length = fileInputStream.read(bytes, 0, bytes.size)
                    }
                } else {
                    println("客户端：$name 想要传输的文件不存在")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                fileInputStream?.close()
            }
        }

    }
}

