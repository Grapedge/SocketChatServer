import java.io.*
import java.lang.StringBuilder
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.math.abs

// 全局常量
const val PORT = 3366
const val FILE_PORT = PORT + 1
const val FILE_PATH = "D:\\\\GrapesChat\\ServerFile\\"

var server = Server(PORT)
var fileServer = FileServer(FILE_PORT, FILE_PATH)

fun main(args : Array<String>) {
    // 主服务线程
    Thread {
        server.start()
    }.start()
    // 文件服务线程
    Thread {
        fileServer.start()
    }.start()

}

// 服务器
class Server(port: Int) : ServerSocket(port) {

    // 客户端列表
    var clientList = mutableListOf<ClientHandler>()
    var random = Random()

    init {
        println("服务器已启动")
    }

    fun start() {
        while (true) {
            val socket = accept()
            clientList.add(ClientHandler(socket, this, randomName().toString()).build())
        }
    }

    // 获取一个五位的随机名字
    private fun randomName(): StringBuilder? {
        val builder = StringBuilder()
        for (i in 1..5) builder.append((abs(random.nextInt()) % 26 + 97).toChar())
        return builder
    }

    // 发送消息
    fun sendMessage(msg: String) {
        for (x in clientList) x.write(msg)
    }

    // 发送文件
    fun sendFile(fileName: String) {
        for (x in clientList) x.writeFile(fileName)
    }

    // Socket
    class ClientHandler(private var client: Socket, private var target: Server, var userName: String) : Thread() {
        private var reader: DataInputStream = DataInputStream(client.getInputStream())
        private var writer: DataOutputStream = DataOutputStream(client.getOutputStream())

        init {
            println("客户端 $name 已连接")
        }

        fun build(): ClientHandler {
            start()
            return this
        }

        fun write(msg: String) {
            // 0 是普通消息
            writer.writeInt(0)
            writer.writeUTF(msg)
            writer.flush()
        }

        fun writeFile(fileName: String) {
            write("开始接收文件$fileName")
            // 1 是文件消息
            writer.writeInt(1)
            writer.writeUTF(fileName)
            writer.flush()
        }

        override fun run() {
            super.run()
            try {
                var type = reader.readInt()
                // 发送消息为 -1 时退出，这个地方我没有处理
                while (type != -1) {
                    when (type) {
                        0 -> {
                            val msg = reader.readUTF()
                            println("msg: $userName, $msg")
                            target.sendMessage("$userName: $msg")
                        }
                        1 -> {
                            val msg = reader.readUTF()
                            println("file: $userName, $msg")
                            target.sendMessage("$userName 正在上传文件 $msg")
                        }
                    }
                    type = reader.readInt()
                }

            } catch (e: Exception) {
                println("连接重置")
            } finally {
                writer.close()
                reader.close()
                client.close()
                target.clientList.remove(this)
            }
        }

    }
}